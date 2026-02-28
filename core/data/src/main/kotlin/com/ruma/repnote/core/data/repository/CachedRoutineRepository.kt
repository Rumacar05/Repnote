package com.ruma.repnote.core.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.ruma.repnote.core.data.mapper.toRoutine
import com.ruma.repnote.core.data.mapper.toRoutineDocument
import com.ruma.repnote.core.data.mapper.toRoutineException
import com.ruma.repnote.core.data.model.RoutineDocument
import com.ruma.repnote.core.database.dao.RoutineDao
import com.ruma.repnote.core.database.entity.toEntity
import com.ruma.repnote.core.database.entity.toRoutine
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineException
import com.ruma.repnote.core.domain.model.RoutineResult
import com.ruma.repnote.core.domain.repository.RoutineRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "CachedRoutineRepository"
private const val ROUTINES_COLLECTION = "routines"
private const val USERS_COLLECTION = "users"

/**
 * Routine repository with local caching for improved performance.
 */
@Suppress("TooGenericExceptionCaught")
class CachedRoutineRepository(
    private val firestore: FirebaseFirestore,
    private val routineDao: RoutineDao,
) : RoutineRepository {
    override fun getUserRoutines(userId: String): Flow<RoutineResult<List<Routine>>> =
        callbackFlow {
            // First, emit any cached data immediately
            val cachedRoutines = routineDao.getUserRoutines(userId)

            launch {
                cachedRoutines.collect { entities ->
                    val routines = entities.map { it.toRoutine() }
                    trySend(RoutineResult.Success(routines))
                }
            }

            // Fetch from Firestore in background
            launch {
                try {
                    val snapshot =
                        firestore
                            .collection(USERS_COLLECTION)
                            .document(userId)
                            .collection(ROUTINES_COLLECTION)
                            .get()
                            .await()

                    val routines =
                        snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(RoutineDocument::class.java)?.toRoutine()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing routine ${doc.id}", e)
                                null
                            }
                        }

                    // Update cache
                    routineDao.insertRoutines(routines.map { it.toEntity() })
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching routines", e)
                    // Only emit error if we don't have cached data
                    val cachedCount = routineDao.getRoutineCount(userId)
                    if (cachedCount == 0) {
                        trySend(
                            RoutineResult.Error(
                                when (e) {
                                    is FirebaseFirestoreException -> e.toRoutineException()
                                    else -> RoutineException.Unknown(e.message)
                                },
                            ),
                        )
                    }
                }
            }

            awaitClose { /* No cleanup needed */ }
        }.catch { e ->
            emit(RoutineResult.Error(RoutineException.Unknown(e.message)))
        }

    override fun getRoutineById(
        userId: String,
        routineId: String,
    ): Flow<RoutineResult<Routine>> =
        flow {
            // Emit cached data first
            val cached = routineDao.getRoutineById(routineId)
            if (cached != null) {
                emit(RoutineResult.Success(cached.toRoutine()))
            }

            // Fetch from Firestore
            try {
                val doc =
                    firestore
                        .collection(USERS_COLLECTION)
                        .document(userId)
                        .collection(ROUTINES_COLLECTION)
                        .document(routineId)
                        .get()
                        .await()

                if (doc.exists()) {
                    val routineDoc = doc.toObject(RoutineDocument::class.java)
                    if (routineDoc != null) {
                        val routine = routineDoc.toRoutine()
                        // Update cache
                        routineDao.insertRoutine(routine.toEntity())
                        emit(RoutineResult.Success(routine))
                    } else {
                        emit(RoutineResult.Error(RoutineException.InvalidRoutineData))
                    }
                } else {
                    emit(RoutineResult.Error(RoutineException.RoutineNotFound))
                }
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "Firestore error fetching routine $routineId", e)
                // If we have cached data, we already emitted it
                if (cached == null) {
                    emit(RoutineResult.Error(e.toRoutineException()))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching routine $routineId", e)
                if (cached == null) {
                    emit(RoutineResult.Error(RoutineException.Unknown(e.message)))
                }
            }
        }.catch { e ->
            emit(RoutineResult.Error(RoutineException.Unknown(e.message)))
        }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun createRoutine(routine: Routine): RoutineResult<String> =
        try {
            // Generate a new ID if not provided
            val docRef =
                firestore
                    .collection(USERS_COLLECTION)
                    .document(routine.userId)
                    .collection(ROUTINES_COLLECTION)
                    .let { collection ->
                        if (routine.id.isBlank()) {
                            collection.document()
                        } else {
                            collection.document(routine.id)
                        }
                    }

            // Use the generated or provided ID
            val routineWithId = routine.copy(id = docRef.id)
            val routineDoc = routineWithId.toRoutineDocument()

            docRef.set(routineDoc).await()

            // Cache it
            routineDao.insertRoutine(routineWithId.toEntity())

            RoutineResult.Success(docRef.id)
        } catch (e: FirebaseFirestoreException) {
            RoutineResult.Error(e.toRoutineException())
        } catch (e: Exception) {
            Log.e(TAG, "Error creating routine", e)
            RoutineResult.Error(RoutineException.Unknown(e.message))
        }

    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    override suspend fun updateRoutine(routine: Routine): RoutineResult<Unit> {
        return try {
            val existingDoc =
                firestore
                    .collection(USERS_COLLECTION)
                    .document(routine.userId)
                    .collection(ROUTINES_COLLECTION)
                    .document(routine.id)
                    .get()
                    .await()

            if (!existingDoc.exists()) {
                return RoutineResult.Error(RoutineException.RoutineNotFound)
            }

            val routineDoc = routine.toRoutineDocument()

            firestore
                .collection(USERS_COLLECTION)
                .document(routine.userId)
                .collection(ROUTINES_COLLECTION)
                .document(routine.id)
                .set(routineDoc)
                .await()

            // Update cache
            routineDao.insertRoutine(routine.toEntity())

            RoutineResult.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            RoutineResult.Error(e.toRoutineException())
        } catch (e: Exception) {
            Log.e(TAG, "Error updating routine", e)
            RoutineResult.Error(RoutineException.Unknown(e.message))
        }
    }

    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    override suspend fun deleteRoutine(
        userId: String,
        routineId: String,
    ): RoutineResult<Unit> {
        return try {
            val existingDoc =
                firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(ROUTINES_COLLECTION)
                    .document(routineId)
                    .get()
                    .await()

            if (!existingDoc.exists()) {
                return RoutineResult.Error(RoutineException.RoutineNotFound)
            }

            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ROUTINES_COLLECTION)
                .document(routineId)
                .delete()
                .await()

            // Delete from cache
            routineDao.deleteRoutine(routineId)

            RoutineResult.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            RoutineResult.Error(e.toRoutineException())
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting routine", e)
            RoutineResult.Error(RoutineException.Unknown(e.message))
        }
    }
}

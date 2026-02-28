package com.ruma.repnote.core.data.di

import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import com.ruma.repnote.core.data.repository.CachedExerciseRepository
import com.ruma.repnote.core.data.repository.CachedRoutineRepository
import com.ruma.repnote.core.data.repository.LocalFirstWorkoutRepository
import com.ruma.repnote.core.data.service.CloudinaryImageStorageService
import com.ruma.repnote.core.domain.repository.ExerciseRepository
import com.ruma.repnote.core.domain.repository.RoutineRepository
import com.ruma.repnote.core.domain.repository.WorkoutRepository
import com.ruma.repnote.core.domain.service.ImageStorageService
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule =
    module {
        single<FirebaseFirestore> { FirebaseFirestore.getInstance() }
        single<WorkManager> { WorkManager.getInstance(androidContext()) }

        single<ImageStorageService> { CloudinaryImageStorageService(androidContext()) }
        singleOf(::CachedExerciseRepository) { bind<ExerciseRepository>() }
        singleOf(::CachedRoutineRepository) { bind<RoutineRepository>() }
        singleOf(::LocalFirstWorkoutRepository) { bind<WorkoutRepository>() }
    }

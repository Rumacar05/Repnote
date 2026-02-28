package com.ruma.repnote.core.data.service

import android.content.Context
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.ruma.repnote.core.domain.model.ExerciseException
import com.ruma.repnote.core.domain.model.ExerciseResult
import com.ruma.repnote.core.domain.service.ImageStorageService
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

private const val TAG = "CloudinaryImageStorageService"
private const val EXERCISE_IMAGES_FOLDER = "exercises"
private const val IMAGE_SEGMENT_INDEX = 2
private const val FILE_NAME_SEGMENT_INDEX = 3

class CloudinaryImageStorageService(
    private val context: Context,
) : ImageStorageService {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun uploadImage(
        imageId: String,
        imageBytes: ByteArray,
    ): ExerciseResult<String> =
        try {
            val publicId = "$EXERCISE_IMAGES_FOLDER/$imageId"

            val tempFile = File(context.cacheDir, "$imageId.jpg")
            FileOutputStream(tempFile).use { it.write(imageBytes) }

            val imageUrl =
                suspendCancellableCoroutine { continuation ->
                    MediaManager
                        .get()
                        .upload(tempFile.absolutePath)
                        .option("public_id", publicId)
                        .option("resource_type", "image")
                        .option("folder", EXERCISE_IMAGES_FOLDER)
                        .callback(
                            object : UploadCallback {
                                override fun onStart(requestId: String) {
                                    Log.d(TAG, "Upload started for $imageId")
                                }

                                override fun onProgress(
                                    requestId: String,
                                    bytes: Long,
                                    totalBytes: Long,
                                ) {
                                    // Progress tracking (optional)
                                }

                                override fun onSuccess(
                                    requestId: String,
                                    resultData: Map<*, *>,
                                ) {
                                    val url = resultData["secure_url"] as? String
                                    tempFile.delete()

                                    if (url != null) {
                                        continuation.resume(url)
                                    } else {
                                        Log.e(TAG, "Upload succeeded but no URL in response")
                                        continuation.resume(null)
                                    }
                                }

                                override fun onError(
                                    requestId: String,
                                    error: ErrorInfo,
                                ) {
                                    tempFile.delete()
                                    Log.e(
                                        TAG,
                                        "Upload failed for $imageId: ${error.description}",
                                    )
                                    continuation.resume(null)
                                }

                                override fun onReschedule(
                                    requestId: String,
                                    error: ErrorInfo,
                                ) {
                                    Log.w(TAG, "Upload rescheduled for $imageId")
                                }
                            },
                        ).dispatch()

                    continuation.invokeOnCancellation {
                        tempFile.delete()
                    }
                }

            if (imageUrl != null) {
                ExerciseResult.Success(imageUrl)
            } else {
                ExerciseResult.Error(ExerciseException.StorageError)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image to Cloudinary", e)
            ExerciseResult.Error(ExerciseException.StorageError)
        }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun deleteImage(imageUrl: String): ExerciseResult<Unit> =
        try {
            val publicId = extractPublicIdFromUrl(imageUrl)

            if (publicId != null) {
                suspendCancellableCoroutine { continuation ->
                    MediaManager
                        .get()
                        .cloudinary
                        .uploader()
                        .destroy(publicId, emptyMap<String, String>())
                    continuation.resume(Unit)
                }
                ExerciseResult.Success(Unit)
            } else {
                Log.w(TAG, "Could not extract public ID from URL: $imageUrl")
                ExerciseResult.Success(Unit)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error deleting image from Cloudinary: $imageUrl", e)
            ExerciseResult.Success(Unit)
        }

    @Suppress("TooGenericExceptionCaught")
    private fun extractPublicIdFromUrl(url: String): String? =
        try {
            val segments = url.split("/")
            val imageIndex = segments.indexOf("image")
            if (imageIndex != -1 && imageIndex + IMAGE_SEGMENT_INDEX < segments.size) {
                val folder = segments[imageIndex + IMAGE_SEGMENT_INDEX]
                val fileName =
                    segments
                        .getOrNull(
                            imageIndex + FILE_NAME_SEGMENT_INDEX,
                        )?.substringBeforeLast(".")
                if (fileName != null) {
                    "$folder/$fileName"
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting public ID from URL: $url", e)
            null
        }
}

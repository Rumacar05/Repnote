package com.ruma.repnote.core.data.service

import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.UploadRequest
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.ruma.repnote.core.domain.model.ExerciseException
import com.ruma.repnote.core.domain.model.ExerciseResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class CloudinaryImageStorageServiceTest {
    private lateinit var context: Context
    private lateinit var service: CloudinaryImageStorageService
    private lateinit var mockMediaManager: MediaManager
    private lateinit var mockUploadRequest: UploadRequest<*>

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        mockMediaManager = mockk(relaxed = true)
        mockUploadRequest = mockk(relaxed = true)

        every { context.cacheDir } returns File.createTempFile("test", "cache").parentFile

        mockkStatic(MediaManager::class)
        every { MediaManager.get() } returns mockMediaManager
        every { mockMediaManager.upload(any<String>()) } returns mockUploadRequest
        every { mockUploadRequest.option(any(), any()) } returns mockUploadRequest

        service = CloudinaryImageStorageService(context)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `GIVEN valid image bytes WHEN uploadImage is called THEN Success with URL is returned`() =
        runTest {
            val imageId = "test-image-123"
            val imageBytes = ByteArray(100) { it.toByte() }
            val expectedUrl = "https://res.cloudinary.com/test/image/upload/exercises/$imageId.jpg"
            val callbackSlot = slot<UploadCallback>()

            every { mockUploadRequest.callback(capture(callbackSlot)) } returns mockUploadRequest
            every { mockUploadRequest.dispatch() } answers {
                val callback = callbackSlot.captured
                callback.onStart("request-id")
                callback.onSuccess(
                    "request-id",
                    mapOf("secure_url" to expectedUrl),
                )
                "request-id"
            }

            val result = service.uploadImage(imageId, imageBytes)

            result shouldBeInstanceOf ExerciseResult.Success::class.java
            (result as ExerciseResult.Success).data shouldBeEqualTo expectedUrl
        }

    @Test
    fun `GIVEN upload fails WHEN uploadImage is called THEN Error with StorageError is returned`() =
        runTest {
            val imageId = "test-image-123"
            val imageBytes = ByteArray(100) { it.toByte() }
            val callbackSlot = slot<UploadCallback>()
            val mockErrorInfo =
                mockk<ErrorInfo> {
                    every { description } returns "Upload failed"
                }

            every { mockUploadRequest.callback(capture(callbackSlot)) } returns mockUploadRequest
            every { mockUploadRequest.dispatch() } answers {
                val callback = callbackSlot.captured
                callback.onStart("request-id")
                callback.onError("request-id", mockErrorInfo)
                "request-id"
            }

            val result = service.uploadImage(imageId, imageBytes)

            result shouldBeInstanceOf ExerciseResult.Error::class.java
            (result as ExerciseResult.Error).exception shouldBeEqualTo ExerciseException.StorageError
        }

    @Test
    fun `GIVEN upload succeeds but no URL in response WHEN uploadImage is called THEN Error with StorageError is returned`() =
        runTest {
            val imageId = "test-image-123"
            val imageBytes = ByteArray(100) { it.toByte() }
            val callbackSlot = slot<UploadCallback>()

            every { mockUploadRequest.callback(capture(callbackSlot)) } returns mockUploadRequest
            every { mockUploadRequest.dispatch() } answers {
                val callback = callbackSlot.captured
                callback.onStart("request-id")
                callback.onSuccess(
                    "request-id",
                    mapOf("some_field" to "value"),
                )
                "request-id"
            }

            val result = service.uploadImage(imageId, imageBytes)

            result shouldBeInstanceOf ExerciseResult.Error::class.java
            (result as ExerciseResult.Error).exception shouldBeEqualTo ExerciseException.StorageError
        }

    @Test
    fun `GIVEN exception during upload WHEN uploadImage is called THEN Error with StorageError is returned`() =
        runTest {
            val imageId = "test-image-123"
            val imageBytes = ByteArray(100) { it.toByte() }

            every { mockMediaManager.upload(any<String>()) } throws RuntimeException("Upload failed")

            val result = service.uploadImage(imageId, imageBytes)

            result shouldBeInstanceOf ExerciseResult.Error::class.java
            (result as ExerciseResult.Error).exception shouldBeEqualTo ExerciseException.StorageError
        }

    @Test
    fun `GIVEN valid image URL WHEN deleteImage is called THEN Success with Unit is returned`() =
        runTest {
            val imageUrl = "https://res.cloudinary.com/test/image/upload/v123/exercises/test-image.jpg"
            val mockCloudinary = mockk<com.cloudinary.Cloudinary>(relaxed = true)
            val mockUploader = mockk<com.cloudinary.Uploader>(relaxed = true)

            every { mockMediaManager.cloudinary } returns mockCloudinary
            every { mockCloudinary.uploader() } returns mockUploader
            every { mockUploader.destroy(any(), emptyMap<String, String>()) } returns emptyMap<Any, Any>()

            val result = service.deleteImage(imageUrl)

            result shouldBeInstanceOf ExerciseResult.Success::class.java
            (result as ExerciseResult.Success).data shouldBeEqualTo Unit
        }

    @Test
    fun `GIVEN invalid image URL WHEN deleteImage is called THEN Success with Unit is returned anyway`() =
        runTest {
            val imageUrl = "invalid-url"

            val result = service.deleteImage(imageUrl)

            result shouldBeInstanceOf ExerciseResult.Success::class.java
            (result as ExerciseResult.Success).data shouldBeEqualTo Unit
        }

    @Test
    fun `GIVEN exception during delete WHEN deleteImage is called THEN Success with Unit is returned`() =
        runTest {
            val imageUrl = "https://res.cloudinary.com/test/image/upload/v123/exercises/test-image.jpg"
            val mockCloudinary = mockk<com.cloudinary.Cloudinary>(relaxed = true)

            every { mockMediaManager.cloudinary } returns mockCloudinary
            every { mockCloudinary.uploader() } throws RuntimeException("Delete failed")

            val result = service.deleteImage(imageUrl)

            result shouldBeInstanceOf ExerciseResult.Success::class.java
            (result as ExerciseResult.Success).data shouldBeEqualTo Unit
        }
}

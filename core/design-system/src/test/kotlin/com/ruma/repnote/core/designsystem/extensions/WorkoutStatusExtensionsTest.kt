package com.ruma.repnote.core.designsystem.extensions

import com.ruma.repnote.core.domain.model.WorkoutStatus
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import com.ruma.repnote.core.stringresources.R as StringR

/**
 * Unit tests for WorkoutStatus extension functions.
 *
 * These tests verify that each WorkoutStatus enum value maps to the correct
 * string resource ID.
 */
class WorkoutStatusExtensionsTest {
    @Test
    fun `WHEN toStringRes is called for COMPLETED THEN correct string resource is returned`() {
        val result = WorkoutStatus.COMPLETED.toStringRes()
        result shouldBeEqualTo StringR.string.workout_status_completed
    }

    @Test
    fun `WHEN toStringRes is called for IN_PROGRESS THEN correct string resource is returned`() {
        val result = WorkoutStatus.IN_PROGRESS.toStringRes()
        result shouldBeEqualTo StringR.string.workout_status_in_progress
    }

    @Test
    fun `WHEN toStringRes is called for ABANDONED THEN correct string resource is returned`() {
        val result = WorkoutStatus.ABANDONED.toStringRes()
        result shouldBeEqualTo StringR.string.workout_status_abandoned
    }

    @Test
    fun `WHEN all WorkoutStatus values call toStringRes THEN all return unique resource IDs`() {
        val resourceIds = WorkoutStatus.entries.map { it.toStringRes() }
        val uniqueIds = resourceIds.toSet()

        uniqueIds.size shouldBeEqualTo WorkoutStatus.entries.size
    }

    @Test
    fun `WHEN all WorkoutStatus values call toStringRes THEN no resource ID is zero`() {
        WorkoutStatus.entries.forEach { status ->
            val resourceId = status.toStringRes()
            assert(resourceId != 0) { "Resource ID for $status should not be 0" }
        }
    }
}

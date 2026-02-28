package com.ruma.repnote.core.designsystem.extensions

import android.content.Context
import com.ruma.repnote.core.domain.model.MuscleGroup
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import com.ruma.repnote.core.designsystem.R as DesignR
import com.ruma.repnote.core.stringresources.R as StringR

/**
 * Unit tests for MuscleGroup extension functions.
 *
 * These tests verify that each MuscleGroup enum value maps to the correct
 * string resource ID, drawable resource ID, and display name.
 */
class MuscleGroupExtensionsTest {
    @Test
    fun `WHEN getDisplayNameResId is called for CHEST THEN correct string resource is returned`() {
        val result = MuscleGroup.CHEST.getDisplayNameResId()
        result shouldBeEqualTo StringR.string.muscle_chest
    }

    @Test
    fun `WHEN getDisplayNameResId is called for BACK THEN correct string resource is returned`() {
        val result = MuscleGroup.BACK.getDisplayNameResId()
        result shouldBeEqualTo StringR.string.muscle_back
    }

    @Test
    fun `WHEN getDisplayNameResId is called for SHOULDERS THEN correct string resource is returned`() {
        val result = MuscleGroup.SHOULDERS.getDisplayNameResId()
        result shouldBeEqualTo StringR.string.muscle_shoulders
    }

    @Test
    fun `WHEN getDisplayNameResId is called for BICEPS THEN correct string resource is returned`() {
        val result = MuscleGroup.BICEPS.getDisplayNameResId()
        result shouldBeEqualTo StringR.string.muscle_biceps
    }

    @Test
    fun `WHEN getDisplayNameResId is called for TRICEPS THEN correct string resource is returned`() {
        val result = MuscleGroup.TRICEPS.getDisplayNameResId()
        result shouldBeEqualTo StringR.string.muscle_triceps
    }

    @Test
    fun `WHEN getDisplayNameResId is called for FOREARMS THEN correct string resource is returned`() {
        val result = MuscleGroup.FOREARMS.getDisplayNameResId()
        result shouldBeEqualTo StringR.string.muscle_forearms
    }

    @Test
    fun `WHEN getDisplayNameResId is called for QUADS THEN correct string resource is returned`() {
        val result = MuscleGroup.QUADS.getDisplayNameResId()
        result shouldBeEqualTo StringR.string.muscle_quads
    }

    @Test
    fun `WHEN getDisplayNameResId is called for HAMSTRINGS THEN correct string resource is returned`() {
        val result = MuscleGroup.HAMSTRINGS.getDisplayNameResId()
        result shouldBeEqualTo StringR.string.muscle_hamstrings
    }

    @Test
    fun `WHEN getDisplayNameResId is called for GLUTES THEN correct string resource is returned`() {
        val result = MuscleGroup.GLUTES.getDisplayNameResId()
        result shouldBeEqualTo StringR.string.muscle_glutes
    }

    @Test
    fun `WHEN getDisplayNameResId is called for CALVES THEN correct string resource is returned`() {
        val result = MuscleGroup.CALVES.getDisplayNameResId()
        result shouldBeEqualTo StringR.string.muscle_calves
    }

    @Test
    fun `WHEN getDisplayNameResId is called for ABS THEN correct string resource is returned`() {
        val result = MuscleGroup.ABS.getDisplayNameResId()
        result shouldBeEqualTo StringR.string.muscle_abs
    }

    @Test
    fun `WHEN getDisplayNameResId is called for OBLIQUES THEN correct string resource is returned`() {
        val result = MuscleGroup.OBLIQUES.getDisplayNameResId()
        result shouldBeEqualTo StringR.string.muscle_obliques
    }

    @Test
    fun `WHEN getIconResId is called for CHEST THEN correct drawable resource is returned`() {
        val result = MuscleGroup.CHEST.getIconResId()
        result shouldBeEqualTo DesignR.drawable.ic_muscle_chest
    }

    @Test
    fun `WHEN getIconResId is called for BACK THEN correct drawable resource is returned`() {
        val result = MuscleGroup.BACK.getIconResId()
        result shouldBeEqualTo DesignR.drawable.ic_muscle_back
    }

    @Test
    fun `WHEN getIconResId is called for SHOULDERS THEN correct drawable resource is returned`() {
        val result = MuscleGroup.SHOULDERS.getIconResId()
        result shouldBeEqualTo DesignR.drawable.ic_muscle_shoulders
    }

    @Test
    fun `WHEN getIconResId is called for BICEPS THEN correct drawable resource is returned`() {
        val result = MuscleGroup.BICEPS.getIconResId()
        result shouldBeEqualTo DesignR.drawable.ic_muscle_biceps
    }

    @Test
    fun `WHEN getIconResId is called for TRICEPS THEN correct drawable resource is returned`() {
        val result = MuscleGroup.TRICEPS.getIconResId()
        result shouldBeEqualTo DesignR.drawable.ic_muscle_triceps
    }

    @Test
    fun `WHEN getIconResId is called for FOREARMS THEN correct drawable resource is returned`() {
        val result = MuscleGroup.FOREARMS.getIconResId()
        result shouldBeEqualTo DesignR.drawable.ic_muscle_forearms
    }

    @Test
    fun `WHEN getIconResId is called for QUADS THEN correct drawable resource is returned`() {
        val result = MuscleGroup.QUADS.getIconResId()
        result shouldBeEqualTo DesignR.drawable.ic_muscle_quads
    }

    @Test
    fun `WHEN getIconResId is called for HAMSTRINGS THEN correct drawable resource is returned`() {
        val result = MuscleGroup.HAMSTRINGS.getIconResId()
        result shouldBeEqualTo DesignR.drawable.ic_muscle_hamstrings
    }

    @Test
    fun `WHEN getIconResId is called for GLUTES THEN correct drawable resource is returned`() {
        val result = MuscleGroup.GLUTES.getIconResId()
        result shouldBeEqualTo DesignR.drawable.ic_muscle_glutes
    }

    @Test
    fun `WHEN getIconResId is called for CALVES THEN correct drawable resource is returned`() {
        val result = MuscleGroup.CALVES.getIconResId()
        result shouldBeEqualTo DesignR.drawable.ic_muscle_calves
    }

    @Test
    fun `WHEN getIconResId is called for ABS THEN correct drawable resource is returned`() {
        val result = MuscleGroup.ABS.getIconResId()
        result shouldBeEqualTo DesignR.drawable.ic_muscle_abs
    }

    @Test
    fun `WHEN getIconResId is called for OBLIQUES THEN correct drawable resource is returned`() {
        val result = MuscleGroup.OBLIQUES.getIconResId()
        result shouldBeEqualTo DesignR.drawable.ic_muscle_obliques
    }

    @Test
    fun `WHEN getDisplayName is called THEN context getString is called with correct resource ID`() {
        val context = mockk<Context>()
        val expectedDisplayName = "Chest"

        every { context.getString(StringR.string.muscle_chest) } returns expectedDisplayName

        val result = MuscleGroup.CHEST.getDisplayName(context)

        result shouldBeEqualTo expectedDisplayName
        verify { context.getString(StringR.string.muscle_chest) }
    }

    @Test
    fun `WHEN all MuscleGroup values call getDisplayNameResId THEN all return unique resource IDs`() {
        val resourceIds = MuscleGroup.entries.map { it.getDisplayNameResId() }
        val uniqueIds = resourceIds.toSet()

        uniqueIds.size shouldBeEqualTo MuscleGroup.entries.size
    }

    @Test
    fun `WHEN all MuscleGroup values call getIconResId THEN all return unique resource IDs`() {
        val resourceIds = MuscleGroup.entries.map { it.getIconResId() }
        val uniqueIds = resourceIds.toSet()

        uniqueIds.size shouldBeEqualTo MuscleGroup.entries.size
    }
}

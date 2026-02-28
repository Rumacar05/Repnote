package com.ruma.repnote.feature.routine.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ruma.repnote.feature.routine.presentation.detail.ExerciseDetail
import com.ruma.repnote.core.stringresources.R as StringRes

@Composable
internal fun RoutineExerciseDetailCard(
    exerciseDetail: ExerciseDetail,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            exerciseDetail.imageUrl?.let {
                ExerciseImage(imageUrl = it, name = exerciseDetail.name)
                Spacer(modifier = Modifier.width(16.dp))
            }
            ExerciseDetailsColumn(exerciseDetail, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ExerciseImage(
    imageUrl: String,
    name: String,
) {
    AsyncImage(
        model =
            ImageRequest
                .Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
        contentDescription = name,
        contentScale = ContentScale.Crop,
        modifier =
            Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
    )
}

@Composable
private fun ExerciseDetailsColumn(
    exerciseDetail: ExerciseDetail,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = exerciseDetail.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        SetsRepsText(exerciseDetail.sets, exerciseDetail.reps)
        exerciseDetail.restSeconds?.let { rest ->
            Spacer(modifier = Modifier.height(2.dp))
            RestTimeText(rest)
        }
        exerciseDetail.notes?.let { notes ->
            if (notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun SetsRepsText(
    sets: Int,
    reps: Int?,
) {
    val repsText = reps?.toString() ?: "—"
    val setsLabel = stringResource(StringRes.string.exercise_sets)
    val repsLabel = stringResource(StringRes.string.exercise_reps)
    Text(
        text = "$sets $setsLabel × $repsText $repsLabel",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun RestTimeText(rest: Int) {
    Text(
        text = "${stringResource(StringRes.string.exercise_rest)}: ${rest}s",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

package com.manuel.fakenewsdetector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.manuel.fakenewsdetector.domain.model.Verdict
import com.manuel.fakenewsdetector.ui.theme.VerdictDanger
import com.manuel.fakenewsdetector.ui.theme.VerdictSuccess
import com.manuel.fakenewsdetector.ui.theme.VerdictWarning

@Composable
fun ConfidenceBar(
    confidence: Double,
    verdict: Verdict? = null,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val progress = confidence.coerceIn(0.0, 1.0).toFloat()

    val color = when (verdict) {
        Verdict.FIABLE -> VerdictSuccess
        Verdict.DUDOSA -> VerdictWarning
        Verdict.FALSA -> VerdictDanger
        null -> MaterialTheme.colorScheme.primary
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (showLabel) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confianza",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun ConfidenceBarCompact(
    confidence: Double,
    verdict: Verdict? = null,
    modifier: Modifier = Modifier
) {
    val progress = confidence.coerceIn(0.0, 1.0).toFloat()

    val color = when (verdict) {
        Verdict.FIABLE -> VerdictSuccess
        Verdict.DUDOSA -> VerdictWarning
        Verdict.FALSA -> VerdictDanger
        null -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

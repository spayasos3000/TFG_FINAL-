package com.manuel.fakenewsdetector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.manuel.fakenewsdetector.domain.model.Verdict
import com.manuel.fakenewsdetector.ui.theme.VerdictDanger
import com.manuel.fakenewsdetector.ui.theme.VerdictDangerPale
import com.manuel.fakenewsdetector.ui.theme.VerdictSuccess
import com.manuel.fakenewsdetector.ui.theme.VerdictSuccessPale
import com.manuel.fakenewsdetector.ui.theme.VerdictWarning
import com.manuel.fakenewsdetector.ui.theme.VerdictWarningPale

@Composable
fun VerdictBadge(
    verdict: Verdict,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val (backgroundColor, textColor) = when (verdict) {
        Verdict.FIABLE -> VerdictSuccessPale to VerdictSuccess
        Verdict.DUDOSA -> VerdictWarningPale to VerdictWarning
        Verdict.FALSA -> VerdictDangerPale to VerdictDanger
    }

    val label = when (verdict) {
        Verdict.FIABLE -> "Fiable"
        Verdict.DUDOSA -> "Dudosa"
        Verdict.FALSA -> "Falsa"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Composable
fun VerdictBadgeCustom(
    label: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

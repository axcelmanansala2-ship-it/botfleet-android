package com.botfleet.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botfleet.android.ui.theme.*

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, borderColor) = when (status) {
        "running" -> Triple(SuccessContainer, Success, Success.copy(alpha = 0.3f))
        "stopped" -> Triple(SurfaceVariant, OnSurfaceVariant, Border)
        "crashed", "error" -> Triple(ErrorContainer, Error, Error.copy(alpha = 0.3f))
        "installing" -> Triple(WarningContainer, Warning, Warning.copy(alpha = 0.3f))
        else -> Triple(SurfaceVariant, OnSurfaceVariant, Border)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(0.5.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (status == "running") {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Success)
            )
        }
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp
        )
    }
}

package com.driveplay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveplay.ui.theme.AccentPrimary
import com.driveplay.ui.theme.SurfaceDark
import com.driveplay.ui.theme.TextPrimary
import com.driveplay.ui.theme.TextSecondary
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedBottomSheet(
    speed: Float,
    rememberSpeed: Boolean,
    onSpeedChange: (Float) -> Unit,
    onRememberSpeedToggle: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    val snapPoints = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f, 4.0f)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Playback Speed",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current speed indicator
            Text(
                text = String.format("%.2f×", speed),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = AccentPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Slider mapping
            Slider(
                value = snapPoints.indexOf(findClosestSnapPoint(speed, snapPoints)).toFloat(),
                onValueChange = { index ->
                    val idx = index.toInt().coerceIn(0, snapPoints.lastIndex)
                    onSpeedChange(snapPoints[idx])
                },
                valueRange = 0f..snapPoints.lastIndex.toFloat(),
                steps = snapPoints.size - 2,
                colors = SliderDefaults.colors(
                    thumbColor = AccentPrimary,
                    activeTrackColor = AccentPrimary,
                    inactiveTrackColor = TextSecondary.copy(alpha = 0.3f),
                    activeTickColor = AccentPrimary,
                    inactiveTickColor = TextSecondary.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Remember speed check box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberSpeed,
                    onCheckedChange = onRememberSpeedToggle,
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccentPrimary,
                        uncheckedColor = TextSecondary,
                        checkmarkColor = SurfaceDark
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Remember playback speed for all videos",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun findClosestSnapPoint(value: Float, points: List<Float>): Float {
    var minDiff = Float.MAX_VALUE
    var closest = 1.0f
    for (p in points) {
        val diff = abs(p - value)
        if (diff < minDiff) {
            minDiff = diff
            closest = p
        }
    }
    return closest
}

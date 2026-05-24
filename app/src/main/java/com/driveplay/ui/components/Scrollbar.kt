package com.driveplay.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws a clean, Material-style vertical scrollbar on a LazyColumn for fast-scrolling feedback.
 */
fun Modifier.verticalScrollbar(
    state: LazyListState,
    color: Color = Color.White.copy(alpha = 0.35f),
    width: Dp = 6.dp,
    rightPadding: Dp = 4.dp
): Modifier = this.drawWithContent {
    // Draw the actual list content first
    drawContent()

    val layoutInfo = state.layoutInfo
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    val totalItemsCount = layoutInfo.totalItemsCount

    if (totalItemsCount > 0 && visibleItemsInfo.isNotEmpty()) {
        val totalVisibleItems = visibleItemsInfo.size
        
        // Show scrollbar only if the items exceed the screen viewport height
        if (totalVisibleItems < totalItemsCount) {
            val viewportWidth = size.width
            val viewportHeight = size.height

            // Calculate scroll progress and thumb size
            val firstVisibleIndex = state.firstVisibleItemIndex
            val firstVisibleScrollOffset = state.firstVisibleItemScrollOffset

            // Approximate the total scrollable height and current scroll offset
            val avgItemHeight = visibleItemsInfo.map { it.size }.average().toFloat()
            val totalEstimatedHeight = avgItemHeight * totalItemsCount
            val currentScrollOffset = (firstVisibleIndex * avgItemHeight) + firstVisibleScrollOffset

            val thumbHeight = (viewportHeight / totalEstimatedHeight) * viewportHeight
            // Clamp thumb height to a minimum size so it remains highly visible
            val finalThumbHeight = thumbHeight.coerceAtLeast(40f).coerceAtMost(viewportHeight)

            // Calculate thumb vertical offset matching the scroll percentage
            val scrollableRange = totalEstimatedHeight - viewportHeight
            val scrollPercentage = if (scrollableRange > 0f) currentScrollOffset / scrollableRange else 0f
            val finalScrollPercentage = scrollPercentage.coerceIn(0f, 1f)
            val thumbOffset = finalScrollPercentage * (viewportHeight - finalThumbHeight)

            val widthPx = width.toPx()
            val paddingPx = rightPadding.toPx()

            drawRoundRect(
                color = color,
                topLeft = Offset(
                    x = viewportWidth - widthPx - paddingPx,
                    y = thumbOffset
                ),
                size = Size(
                    width = widthPx,
                    height = finalThumbHeight
                ),
                cornerRadius = CornerRadius(x = widthPx / 2, y = widthPx / 2)
            )
        }
    }
}

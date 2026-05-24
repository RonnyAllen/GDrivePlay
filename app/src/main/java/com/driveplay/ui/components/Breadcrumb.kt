package com.driveplay.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveplay.browser.BreadcrumbItem
import com.driveplay.ui.theme.AccentPrimary
import com.driveplay.ui.theme.DividerColor
import com.driveplay.ui.theme.TextPrimary
import com.driveplay.ui.theme.TextSecondary

@Composable
fun Breadcrumb(
    breadcrumbs: List<BreadcrumbItem>,
    onSegmentClick: (String, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(breadcrumbs) { index, item ->
            val isLast = index == breadcrumbs.lastIndex

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.folderName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    ),
                    color = if (isLast) AccentPrimary else TextSecondary,
                    modifier = Modifier.clickable {
                        onSegmentClick(item.folderId, item.folderName, index)
                    }
                )

                if (!isLast) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Separator",
                        tint = DividerColor,
                        modifier = Modifier.width(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

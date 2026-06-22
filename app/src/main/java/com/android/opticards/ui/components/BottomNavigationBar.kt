package com.android.opticards.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OptiCardsBottomNavigation(
    currentTab: Int = 0,
    onTabSelected: (Int) -> Unit = {}
) {
    Surface(
        color = Color.White,
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf("Trang chủ", "Thẻ", "Giao dịch", "Khuyến mại", "Mở rộng")
            val icons = listOf(
                Icons.Default.Home,
                Icons.Default.CreditCard,
                Icons.Default.Receipt,
                Icons.Default.LocalOffer,
                Icons.Default.Menu
            )

            items.forEachIndexed { index, title ->
                CustomBottomNavItem(
                    icon = icons[index],
                    label = title,
                    isSelected = index == currentTab,
                    onClick = {
                        onTabSelected(index)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CustomBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor = Color(0xFF157FEC)
    val unselectedColor = Color.Gray
    val indicatorColor = if (isSelected) selectedColor.copy(alpha = 0.1f) else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(indicatorColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) selectedColor else unselectedColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                color = if (isSelected) selectedColor else unselectedColor,
                maxLines = 1
            )
        }
    }
}
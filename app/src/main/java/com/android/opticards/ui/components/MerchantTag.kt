package com.android.opticards.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MerchantTag(
    text: String,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(9.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 13.sp
        )
    }
}
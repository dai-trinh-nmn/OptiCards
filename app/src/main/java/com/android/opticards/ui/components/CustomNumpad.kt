package com.android.opticards.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun CustomNumpad(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("000", "0", "BACKSPACE")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE5E5EA))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { })
            }
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in keys) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (key in row) {
                    val isActionKey = key == "BACKSPACE"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActionKey) Color(0xFFBCC0C5) else Color.White)
                            .clickable {
                                if (key == "BACKSPACE") onBackspace() else onKeyPress(key)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "BACKSPACE") {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Xóa",
                                tint = Color.Black
                            )
                        } else {
                            Text(
                                text = key,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF157FEC))
                .clickable { onDone() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Xong",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
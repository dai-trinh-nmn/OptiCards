package com.android.opticards.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun CustomPopup(
    title: String = "Thông báo",
    message: String,
    icon: ImageVector = Icons.Outlined.WarningAmber,
    iconTint: Color = Color(0xFFE56B3D),
    primaryButtonText: String = "Đóng",
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = "Warning",
                    tint = iconTint,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFECF0F1),
                        contentColor = Color.Black
                    )
                ) {
                    Text(text = primaryButtonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (secondaryButtonText != null && onSecondaryClick != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onSecondaryClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        )
                    ) {
                        Text(text = secondaryButtonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
package com.android.opticards.ui.components

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.opticards.data.model.MerchantOverview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MerchantListCard(
    merchant: MerchantOverview,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var brandColor by remember { mutableStateOf(Color(0xFF157FEC)) }
    val isLightColor = brandColor.luminance() > 0.7f
    val categoryBgColor = if (isLightColor) Color(0xFFF3F4F6) else brandColor.copy(alpha = 0.15f)
    val categoryTextColor = if (isLightColor) Color.DarkGray else brandColor
    val mccBgColor = if (isLightColor) Color(0xFFE5E7EB) else brandColor
    val mccTextColor = if (isLightColor) Color.Black else Color.White

    val imageUrl = merchant.logoUrl ?: "https://ui-avatars.com/api/?name=${merchant.name}&background=random&color=fff&size=256"
    val imageRequest = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false)
        .crossfade(true)
        .build()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 3.dp,
        border = BorderStroke(0.5.dp, Color(0xFFE5E7EB)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = merchant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF3F4F6)),
                onSuccess = { state ->
                    val bitmap = (state.result.drawable as? BitmapDrawable)?.bitmap

                    bitmap?.let {
                        Palette.from(it).generate { palette ->
                            val swatch = palette?.vibrantSwatch
                                ?: palette?.dominantSwatch
                                ?: palette?.mutedSwatch

                            swatch?.rgb?.let { colorInt ->
                                brandColor = Color(colorInt)
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = merchant.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 21.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(7.dp))

                MerchantTag(
                    text = merchant.category,
                    textColor = categoryTextColor,
                    bgColor = categoryBgColor
                )

                if (merchant.mccCodes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(7.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val maxMcc = 3
                        val displayMccs = merchant.mccCodes.take(maxMcc)
                        val extraCount = merchant.mccCodes.size - maxMcc

                        displayMccs.forEach { mcc ->
                            MerchantTag(
                                text = mcc,
                                textColor = mccTextColor,
                                bgColor = mccBgColor
                            )
                        }

                        if (extraCount > 0) {
                            MerchantTag(
                                text = "+$extraCount",
                                textColor = Color.DarkGray,
                                bgColor = Color(0xFFEBEBEB)
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onFavoriteClick
            ) {
                Icon(
                    imageVector = if (merchant.isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite Icon",
                    tint = if (merchant.isFavorited) Color(0xFFFF3B30) else Color(0xFFD1D1D6), // Đỏ tươi nếu đã tim, Xám nhạt nếu chưa
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
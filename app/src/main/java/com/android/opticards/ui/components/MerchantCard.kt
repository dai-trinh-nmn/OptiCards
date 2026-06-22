package com.android.opticards.ui.components

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import com.android.opticards.data.model.MerchantOverview
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun MerchantCard(
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

    Card(
        modifier = modifier
            .width(144.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = merchant.name,
                    modifier = Modifier.fillMaxSize(),
                    onSuccess = { state ->
                        val bitmap = (state.result.drawable as? BitmapDrawable)?.bitmap
                        bitmap?.let {
                            Palette.from(it).generate { palette ->
                                val swatch = palette?.vibrantSwatch ?: palette?.dominantSwatch ?: palette?.mutedSwatch
                                swatch?.rgb?.let { colorInt ->
                                    brandColor = Color(colorInt)
                                }
                            }
                        }
                    }
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(26.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().clickable { onFavoriteClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (merchant.isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite Icon",
                            tint = if (merchant.isFavorited) Color(0xFFFF3B30) else Color(0xFF8E8E93),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = merchant.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))
            MerchantTag(
                text = merchant.category,
                textColor = categoryTextColor,
                bgColor = categoryBgColor
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val maxMcc = 1
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
}
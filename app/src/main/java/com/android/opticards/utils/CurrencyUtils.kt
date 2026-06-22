package com.android.opticards.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat

fun getCurrencyUnit(currencyType: String): String {
    return when (currencyType) {
        "POINT" -> " Điểm"
        "SHOPEE_COIN" -> " Xu"
        else -> "đ"
    }
}
fun formatNumber(amount: Long): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(amount).replace(',', '.')
}

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val out = StringBuilder()
        val length = original.length
        for (i in 0 until length) {
            out.append(original[i])
            if ((length - i - 1) % 3 == 0 && i != length - 1) {
                out.append('.')
            }
        }
        val formatted = out.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformedCursor = 0
                var originalCursor = 0
                while (originalCursor < offset && transformedCursor < formatted.length) {
                    if (formatted[transformedCursor] != '.') {
                        originalCursor++
                    }
                    transformedCursor++
                }
                return transformedCursor
            }

            override fun transformedToOriginal(offset: Int): Int {
                var originalCursor = 0
                var transformedCursor = 0
                while (transformedCursor < offset && transformedCursor < formatted.length) {
                    if (formatted[transformedCursor] != '.') {
                        originalCursor++
                    }
                    transformedCursor++
                }
                return originalCursor
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

fun String.formatAsCurrency(): String {
    val number = this.toLongOrNull() ?: return this
    return String.format("%,d", number).replace(',', '.')
}
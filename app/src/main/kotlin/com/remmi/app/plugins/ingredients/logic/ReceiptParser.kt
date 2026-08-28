package com.remmi.app.plugins.ingredients.logic

import com.remmi.app.plugins.ingredients.models.ScannedReceiptItem
import com.remmi.app.plugins.ingredients.models.MeasurementUnit
import java.util.Locale

/**
 * RECEIPT PARSER
 *
 * Logic to extract items and quantities from raw OCR text.
 */
class ReceiptParser {

    /**
     * Parse raw OCR text into a list of potential items.
     */
    fun parse(text: String): List<ScannedReceiptItem> {
        val lines = text.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { !isExcludedLine(it) }

        return lines.mapNotNull { parseLine(it) }
    }

    private fun isExcludedLine(line: String): Boolean {
        val excluded = listOf("TOTAL", "SUBTOTAL", "EUR", "EURO", "CASH", "CHANGE", "VISA", "MASTERCARD", "TAX", "VAT", "IVA")
        return excluded.any { line.uppercase().contains(it) }
    }

    private fun parseLine(line: String): ScannedReceiptItem? {
        // Sample: "ZANAHORIA 1 KG 1.20"
        // Sample: "LECHE ENTERA 1L 1.10"
        
        val priceRegex = Regex("""(\d+[\.,]\d{2})$""")
        val priceMatch = priceRegex.find(line)
        val price = priceMatch?.value?.replace(",", ".")?.toDoubleOrNull()
        
        val content = if (priceMatch != null) line.substring(0, priceMatch.range.first).trim() else line
        
        // Detect quantity and unit
        val qtyRegex = Regex("""(\d+[\.,]?\d*)\s*(KG|G|L|ML|UNITS|U|PCS|GR|K)""", RegexOption.IGNORE_CASE)
        val qtyMatch = qtyRegex.find(content)
        
        val quantity = qtyMatch?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull()
        val unitStr = qtyMatch?.groupValues?.get(2)?.uppercase()
        
        val unit = when (unitStr) {
            "KG", "K" -> MeasurementUnit.KILOGRAMS
            "G", "GR" -> MeasurementUnit.GRAMS
            "L" -> MeasurementUnit.LITERS
            "ML" -> MeasurementUnit.MILLILITERS
            "UNITS", "U", "PCS" -> MeasurementUnit.UNITS
            else -> null
        }
        
        val name = if (qtyMatch != null) content.substring(0, qtyMatch.range.first).trim() else content
        
        if (name.length < 2) return null
        
        return ScannedReceiptItem(
            originalText = line,
            detectedName = name,
            quantity = quantity,
            unit = unit,
            price = price
        )
    }
}

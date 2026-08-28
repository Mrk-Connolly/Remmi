package com.remmi.app.plugins.ingredients.logic

import com.remmi.app.plugins.ingredients.models.*

/**
 * INGREDIENT RECEIPT MATCHER
 *
 * Matches receipt items with known ingredient metadata.
 */
class IngredientReceiptMatcher(
    private val existingMetadata: List<IngredientMetadata>
) {

    fun match(items: List<ScannedReceiptItem>): List<ReceiptItemMatch> {
        return items.map { item ->
            val match = findBestMatch(item)
            ReceiptItemMatch(
                receiptItem = item,
                matchedIngredient = match?.first,
                confidence = match?.second ?: MatchConfidence.UNKNOWN
            )
        }
    }

    private fun findBestMatch(item: ScannedReceiptItem): Pair<IngredientMetadata, MatchConfidence>? {
        val normalizedName = item.detectedName.lowercase().trim()
        
        // 1. Exact Match
        existingMetadata.find { it.name.lowercase().trim() == normalizedName }?.let {
            return it to MatchConfidence.HIGH
        }
        
        // 2. Contains Match
        existingMetadata.find { it.name.lowercase().contains(normalizedName) || normalizedName.contains(it.name.lowercase()) }?.let {
            return it to MatchConfidence.MEDIUM
        }
        
        // 3. Conservative Fuzzy (Manual logic or library)
        // For now, we'll keep it simple
        
        return null
    }
}

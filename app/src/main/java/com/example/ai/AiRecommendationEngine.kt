package com.example.ai

import com.example.data.model.AiRecommendation
import com.example.data.model.FoodDonationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AiRecommendationEngine {

    /**
     * Recommends food donations based on receiver preferences and location,
     * prioritizing items with imminent expiry to eliminate waste.
     */
    fun getSmartRecommendations(
        donations: List<FoodDonationEntity>,
        receiverLocation: String,
        preferredCategory: String? = null
    ): List<AiRecommendation> {
        val now = System.currentTimeMillis()

        return donations.map { donation ->
            var score = 50 // Base score

            // Location affinity
            val locQuery = receiverLocation.trim().lowercase()
            val donLoc = donation.pickupLocation.lowercase()
            if (locQuery.isNotBlank()) {
                val locKeywords = locQuery.split(" ", ",", "-").filter { it.length > 2 }
                val matchesLocation = locKeywords.any { donLoc.contains(it) }
                if (matchesLocation) {
                    score += 25
                }
            }

            // Category affinity
            if (!preferredCategory.isNullOrBlank() && preferredCategory != "All") {
                if (donation.category.equals(preferredCategory, ignoreCase = true)) {
                    score += 20
                }
            }

            // Expiry urgency weighting (Waste Prevention Algorithm)
            val timeRemainingMs = donation.expiryTimestamp - now
            val hoursRemaining = timeRemainingMs / (1000 * 3600)

            val urgencyLevel: String
            val urgencyBonus: Int
            val reason: String

            when {
                hoursRemaining <= 6 -> {
                    urgencyLevel = "CRITICAL"
                    urgencyBonus = 25
                    reason = "Critical Priority: Expires in ~${maxOf(1, hoursRemaining)} hrs. Rescue immediately to prevent spoilage."
                }
                hoursRemaining <= 24 -> {
                    urgencyLevel = "HIGH"
                    urgencyBonus = 15
                    reason = "High Priority: Expires in ~${hoursRemaining} hrs. Ideal for distribution today."
                }
                hoursRemaining <= 48 -> {
                    urgencyLevel = "MODERATE"
                    urgencyBonus = 10
                    reason = "Moderate Priority: Fresh for 2 days. Great match for community pantry stock."
                }
                else -> {
                    urgencyLevel = "STABLE"
                    urgencyBonus = 5
                    reason = "Extended Shelf Life: Suitable for planned storage and distribution."
                }
            }

            score = (score + urgencyBonus).coerceIn(40, 99)

            val safetyTips = getSafetyAdvice(donation.category, urgencyLevel)

            AiRecommendation(
                donationId = donation.id,
                foodName = donation.foodName,
                matchScorePercent = score,
                urgencyLevel = urgencyLevel,
                reason = reason,
                foodSafetyTips = safetyTips
            )
        }.sortedByDescending { it.matchScorePercent }
    }

    private fun getSafetyAdvice(category: String, urgency: String): String {
        return when (category) {
            "Cooked Meals" -> "Maintain temperature >60°C or refrigerate <4°C immediately. Reheat thoroughly once before consumption."
            "Dairy & Eggs" -> "Strict cold chain required (2°C–5°C). Inspect seal integrity before handing over."
            "Bakery & Bread" -> "Store in a dry, ventilated container. Keep away from humid air to preserve crispness."
            "Fresh Produce" -> "Wash thoroughly with clean water before preparing. Store root veggies separately from fruits."
            "Canned & Packaged" -> "Check for dented or bulging cans. Store in a cool, dark dry area."
            else -> "Inspect visual freshness and odor upon handover. Transport in food-safe bags."
        }
    }

    suspend fun getGeminiSmartSummary(
        donations: List<FoodDonationEntity>,
        receiverLocation: String
    ): String = withContext(Dispatchers.IO) {
        if (donations.isEmpty()) {
            return@withContext "No active food donations found to evaluate."
        }

        val donationSummaryList = donations.take(5).joinToString("\n") {
            "- ${it.foodName} (${it.category}, ${it.quantity}) at ${it.pickupLocation}, Expiry: ${it.expiryDate}"
        }

        val prompt = """
            You are the AI Assistant for the Community Food Sharing Platform.
            A receiver located at "$receiverLocation" is seeking surplus food donations.
            Here is the current available surplus food batch:
            $donationSummaryList

            Please provide a brief, encouraging 2-3 sentence recommendation summary:
            1. Recommend which item to prioritize picking up first to prevent waste.
            2. Give one practical safety or transportation tip.
            Keep the response friendly, concise, and professional.
        """.trimIndent()

        val geminiResponse = GeminiAiService.getGeminiCompletion(prompt)
        if (!geminiResponse.isNullOrBlank()) {
            return@withContext geminiResponse.trim()
        }

        // Fallback intelligent summary
        val urgentItem = donations.minByOrNull { it.expiryTimestamp }
        if (urgentItem != null) {
            "AI Recommendation: Prioritize claiming '${urgentItem.foodName}' at ${urgentItem.pickupLocation} due to upcoming expiry (${urgentItem.expiryDate}). Use insulated food carrier containers during transit."
        } else {
            "AI Recommendation: All donations are currently fresh and ready for immediate community pickup."
        }
    }
}

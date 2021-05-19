package de.swiedenfeld.springbootkotlin.data.aggregate

@JvmRecord
data class TicketPurchasesAndRating(val purchases: Int? = 0, val rating: Double? = 0.0)

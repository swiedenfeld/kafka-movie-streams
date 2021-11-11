package de.swiedenfeld.springbootkotlin.data.event

@JvmRecord
data class TicketPurchased(val visitorId: Int, val movieId: Int)

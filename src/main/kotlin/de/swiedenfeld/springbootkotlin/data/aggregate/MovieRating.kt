package de.swiedenfeld.springbootkotlin.data.aggregate

@JvmRecord
data class MovieRating(val ratingSum: Double = 0.0, val ratingCount: Int = 0)

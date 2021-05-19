package de.swiedenfeld.springbootkotlin.data.event

/**
 * A visitor rated a movie
 */
@JvmRecord
data class MovieRated(val visitorId: Int, val movieId: Int, val rating: Double)

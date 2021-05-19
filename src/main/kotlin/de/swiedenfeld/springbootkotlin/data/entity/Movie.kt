package de.swiedenfeld.springbootkotlin.data.entity

@JvmRecord
data class Movie(val id: Int, val title: String, val year: Int, val genre: String)

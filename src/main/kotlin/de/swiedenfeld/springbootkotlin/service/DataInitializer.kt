package de.swiedenfeld.springbootkotlin.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.swiedenfeld.springbootkotlin.data.entity.Movie
import de.swiedenfeld.springbootkotlin.data.entity.Visitor
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
@Profile("init")
class DataInitializer(val kafkaTemplate: KafkaTemplate<Any, Any>) : CommandLineRunner {

    override fun run(vararg args: String?) {
        for (movie in readMovies()) kafkaTemplate.send("movies", movie.id, movie)

        for (visitor in readVisitors()) kafkaTemplate.send("visitors", visitor.id, visitor)
    }

    private fun readMovies(): List<Movie> {
        return jacksonObjectMapper().readValue(this::class.java.classLoader.getResource("movies.json")!!)
    }

    private fun readVisitors(): List<Visitor> {
        return jacksonObjectMapper().readValue(this::class.java.classLoader.getResource("visitors.json")!!)
    }

}

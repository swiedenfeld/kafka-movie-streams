package de.swiedenfeld.springbootkotlin.config

import de.swiedenfeld.springbootkotlin.data.event.MovieRated
import de.swiedenfeld.springbootkotlin.data.event.TicketPurchased
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import kotlin.random.Random

@Configuration
@EnableScheduling
class ScheduledActionsConfig(val kafkaTemplate: KafkaTemplate<Int, Any>) {

    @Scheduled(fixedRate = 100)
    fun rateMovie() {
        kafkaTemplate.send("movie-ratings", 1, MovieRated((0..7).random(), (0..10).random(), Random.nextDouble(0.0, 1.0)))
    }

    @Scheduled(fixedRate = 100)
    fun purchaseTicket() {
        kafkaTemplate.send("ticket-purchases", 1, TicketPurchased((0..7).random(), (0..10).random()))
    }

}

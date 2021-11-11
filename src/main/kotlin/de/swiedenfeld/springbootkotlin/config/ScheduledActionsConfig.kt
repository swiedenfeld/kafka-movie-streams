package de.swiedenfeld.springbootkotlin.config

import de.swiedenfeld.springbootkotlin.data.event.MovieRated
import de.swiedenfeld.springbootkotlin.data.event.TicketPurchased
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

@Configuration
@EnableScheduling
class ScheduledActionsConfig(val kafkaTemplate: KafkaTemplate<Int, Any>) {

    var movieRatingsKeyCounter: AtomicInteger = AtomicInteger(0)
    var purchasesKeyCounter: AtomicInteger = AtomicInteger(0)

    @Scheduled(fixedRate = 100)
    fun rateMovie() {
        kafkaTemplate.send("movie-ratings", movieRatingsKeyCounter.getAndIncrement(), MovieRated((0..7).random(), (0..10).random(), Random.nextDouble(0.0, 1.0)))
    }

    @Scheduled(fixedRate = 100)
    fun purchaseTicket() {
        kafkaTemplate.send("ticket-purchases", purchasesKeyCounter.getAndIncrement(), TicketPurchased((0..7).random(), (0..10).random()))
    }

}

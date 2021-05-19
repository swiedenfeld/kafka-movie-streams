package de.swiedenfeld.springbootkotlin.config

import de.swiedenfeld.springbootkotlin.data.aggregate.MovieRating
import de.swiedenfeld.springbootkotlin.data.aggregate.TicketPurchases
import de.swiedenfeld.springbootkotlin.data.aggregate.TicketPurchasesAndRating
import de.swiedenfeld.springbootkotlin.data.entity.Movie
import de.swiedenfeld.springbootkotlin.data.entity.Visitor
import de.swiedenfeld.springbootkotlin.data.event.MovieRated
import de.swiedenfeld.springbootkotlin.data.event.MovieVisit
import de.swiedenfeld.springbootkotlin.data.event.TicketPurchased
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.serialization.Serdes.Integer
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.support.serializer.JsonSerde

@EnableKafka
@EnableKafkaStreams
@Configuration
class KafkaStreamsConfig {

    @Bean
    fun moviesTopic(): NewTopic = NewTopic("movies", 3, 1)

    @Bean
    fun visitorsTopic(): NewTopic = NewTopic("visitors", 3, 1)

    @Bean
    fun ticketPurchasesTopic(): NewTopic = NewTopic("ticket-purchases", 3, 1)

    @Bean
    fun moviesVisitsTopic(): NewTopic = NewTopic("movie-visits", 3, 1)

    @Bean
    fun movieRatingsTopic(): NewTopic = NewTopic("movie-ratings", 3, 1)

    @Bean
    fun ticketPurchasesAggregateTopic(): NewTopic = NewTopic("ticket-purchases-aggregate", 3, 1)

    @Bean
    fun movieRatingsAggregateTopic(): NewTopic = NewTopic("movie-ratings-aggregate", 3, 1)

    @Bean
    fun moviesTable(builder: StreamsBuilder): GlobalKTable<Int, Movie> =
        builder.globalTable("movies", Consumed.with(Integer(), JsonSerde(Movie::class.java)))

    @Bean
    fun visitorsTable(builder: StreamsBuilder): GlobalKTable<Int, Visitor> =
        builder.globalTable("visitors", Consumed.with(Integer(), JsonSerde(Visitor::class.java)))

    @Bean
    fun visitorsStream(builder: StreamsBuilder): KStream<Int, TicketPurchased> =
        builder.stream("ticket-purchases", Consumed.with(Integer(), JsonSerde(TicketPurchased::class.java)))

    @Bean
    fun ratingsStream(builder: StreamsBuilder): KStream<Int, MovieRated> =
        builder.stream("movie-ratings", Consumed.with(Integer(), JsonSerde(MovieRated::class.java)))

    @Bean
    fun movieVisitsStream(
        ticketPurchasesStream: KStream<Int, TicketPurchased>,
        moviesTable: GlobalKTable<Int, Movie>,
        visitorsTable: GlobalKTable<Int, Visitor>
    ): KStream<Int, MovieVisit> {
        val stream = ticketPurchasesStream
            .join(moviesTable, { _, movie -> movie.movieId }, { ticketPurchased, movie ->
                object {
                    val visitorId = ticketPurchased.visitorId
                    val movieTitle = movie.title
                }
            })
            .join(visitorsTable, { _, v -> v.visitorId }, { v, visitor -> MovieVisit(visitor.name, v.movieTitle) })

        stream.to("movie-visits", Produced.with(Integer(), JsonSerde(MovieVisit::class.java)))
        return stream
    }

    @Bean
    fun movieAggregateRatingsStream(
        ratingsStream: KStream<Int, MovieRated>
    ): KStream<Int, MovieRating>? {
        val stream = ratingsStream.groupBy(
            { _, movieRated -> movieRated.movieId },
            Grouped.with(Integer(), JsonSerde(MovieRated::class.java))
        )
            .aggregate(
                { MovieRating() },
                { _, movieRated, aggregateRating ->
                    MovieRating(
                        aggregateRating.ratingSum + movieRated.rating,
                        aggregateRating.ratingCount + 1
                    )
                },
                Materialized.with(Integer(), JsonSerde(MovieRating::class.java))
            ).toStream()

        stream.to(
            "movie-ratings-aggregate",
            Produced.with(Integer(), JsonSerde(MovieRating::class.java))
        )
        return stream
    }

    @Bean
    fun ticketPurchasesAggregateStream(
        ticketPurchasesStream: KStream<Int, TicketPurchased>
    ): KStream<Int, TicketPurchases>? {
        val stream = ticketPurchasesStream.groupBy(
            { _, ticketPurchased -> ticketPurchased.movieId },
            Grouped.with(Integer(), JsonSerde(TicketPurchased::class.java))
        )
            .aggregate(
                { TicketPurchases() },
                { _, _, aggregatePurchases -> TicketPurchases(aggregatePurchases.purchases + 1) },
                Materialized.with(Integer(), JsonSerde(TicketPurchases::class.java))
            ).toStream()

        stream.to(
            "ticket-purchases-aggregate",
            Produced.with(Integer(), JsonSerde(TicketPurchases::class.java))
        )
        return stream
    }

    @Bean
    fun movieViewsTable(builder: StreamsBuilder): KTable<Int, TicketPurchases> = builder.table(
        "ticket-purchases-aggregate",
        Consumed.with(Integer(), JsonSerde(TicketPurchases::class.java))
    )

    @Bean
    fun movieRatingsTable(builder: StreamsBuilder): KTable<Int, MovieRating> = builder.table(
        "movie-ratings-aggregate",
        Consumed.with(Integer(), JsonSerde(MovieRating::class.java))
    )

    @Bean
    fun movieVisitsCountAndRatingsTable(
        ticketPurchasesTable: KTable<Int, TicketPurchases>,
        movieRatingsTable: KTable<Int, MovieRating>
    ): KTable<Int, TicketPurchasesAndRating> {
        return ticketPurchasesTable.outerJoin(
            movieRatingsTable,
            { ticketPurchases: TicketPurchases?, movieRating: MovieRating? ->
                TicketPurchasesAndRating(
                    ticketPurchases?.purchases,
                    movieRating?.let { it.ratingSum / it.ratingCount })
            },
            Materialized.`as`<Int?, TicketPurchasesAndRating?, KeyValueStore<Bytes, ByteArray>?>("ticket-purchases-and-ratings")
                .withKeySerde(Integer())
                .withValueSerde(JsonSerde(TicketPurchasesAndRating::class.java))
        )
    }

}

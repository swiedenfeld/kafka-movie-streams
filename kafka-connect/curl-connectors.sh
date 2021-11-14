#!/bin/sh
echo "Register Kafka Connect connectors"
echo "Topic 'ticket-purchases-and-ratings' -> OpenSearch"
curl --location --request POST 'http://localhost:8083/connectors' --header 'Content-Type: application/json' --data @kafka-connect-elasticsearch-ticket-purchases-and-ratings.json
echo "Data generation -> Topic 'ticket-purchases'"
curl --location --request POST 'http://localhost:8083/connectors' --header 'Content-Type: application/json' --data @kafka-connect-datagen-ticket-purchases.json
echo "Data generation -> Topic 'movie-ratings'"
curl --location --request POST 'http://localhost:8083/connectors' --header 'Content-Type: application/json' --data @kafka-connect-datagen-movie-ratings.json

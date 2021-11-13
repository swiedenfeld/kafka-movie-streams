# kafka-movie-streams
Spring Boot application with Kotlin, Java16 Features (Records) and Kafka Streams

Disclaimer:<br>
**!!! THIS IS FOR DEMO USE ONLY, DO NOT USE THIS IN PRODUCTION !!!**

Follow these steps to bring everything up and running
1. `sudo echo vm.max_map_count=262144 >> /etc/sysctl.conf`
2. `sudo sysctl -p`
3. `docker-compose up -d --build`
4. `cd kafka-connect && ./curl/connectors.sh`

### Data Model:
Given some movies (`src/main/resources/movies.json`) and some people (`src/main/resources/movies.json`)

Now, people randomly visit movies and rate them afterwards on a scale between 0.0 (very bad) and 1.0 (very good). 
Those events are published to Kafka as soon as they happen via Kafka Connect

#### Ticket Purchases and Ratings
Kafka Streams aggregates movies and ratings in a topic 'ticket-purchases-and-ratings'. That topic is exported to OpenSearch via Kafka Connect. 

### Servers
* OpenSearch Dashboard http://localhost:5601
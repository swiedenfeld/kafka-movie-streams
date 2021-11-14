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

There is a list of visitors (see ``\src\main\resources\visitors.json``). and a list of movies (see ``\src\main\resources\movies.json``).

##### Ticket Purchases
Visitors purchase tickets to movies randomly. Purchase events go to topic ``ticket-purchases``.

```json
{
  "type": "record",
  "name": "movierating",
  "fields": [
    {
      "name": "id",
      "type": {
        "type": "int",
        "arg.properties": {
          "iteration": {
            "start": 1,
            "step": 1
          }
        }
      }
    },
    {
      "name": "visitorId",
      "type": {
        "type": "int",
        "arg.properties": {
          "range": {
            "min": 0,
            "max": 7
          }
        }
      }
    },
    {
      "name": "movieId",
      "type": {
        "type": "int",
        "arg.properties": {
          "range": {
            "min": 0,
            "max": 10
          }
        }
      }
    },
    {
      "name": "rating",
      "type": {
        "type": "double",
        "arg.properties": {
          "range": {
            "min": 0,
            "max": 1
          }
        }
      }
    }
  ]
}
```

### Movie Ratings

People rate movies randomly on a scale between 0.0 (very bad) and 1.0 (very good). Ratings happen independently of ticket purchases.
Movie rating events go to topic ``movie-ratings``.

```json
{
  "type": "record",
  "name": "movierating",
  "fields": [
    {
      "name": "id",
      "type": {
        "type": "int",
        "arg.properties": {
          "iteration": {
            "start": 1,
            "step": 1
          }
        }
      }
    },
    {
      "name": "visitorId",
      "type": {
        "type": "int",
        "arg.properties": {
          "range": {
            "min": 0,
            "max": 7
          }
        }
      }
    },
    {
      "name": "movieId",
      "type": {
        "type": "int",
        "arg.properties": {
          "range": {
            "min": 0,
            "max": 10
          }
        }
      }
    }
  ]
}
```

#### Aggregation and Average rating per movie
Kafka Streams app aggregates movies with their total visits and average ratings in a topic 'ticket-purchases-and-ratings'. That topic is exported to OpenSearch via Kafka Connect. 

### Servers
* OpenSearch Dashboard http://localhost:5601
* KafDrop Dashboard http://localhost:9000
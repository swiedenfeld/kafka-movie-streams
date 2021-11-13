FROM maven:3.8.2-openjdk-16@sha256:0f0c8cfc0718c32f0c8c509403d64eaea54402167251f1150fa51f8f97a9c566 as builder
WORKDIR /build
COPY pom.xml .

COPY src/ /build/src

RUN mvn -B -ntp -DskipTests package

FROM openjdk:16-jdk@sha256:bb68f084c2000c8532b1675ca7034f3922f4aa10e9c7126d29551c0ffd6dee8f

MAINTAINER sascha.wiedenfeld@gmail.com

VOLUME /tmp

WORKDIR /home/app

EXPOSE 8080

COPY --from=builder /build/target/kafka-movie-streams-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["sh", "-c", "java -jar /app.jar"]
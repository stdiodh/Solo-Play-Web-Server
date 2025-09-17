package com.example.solo_play_web_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing

@SpringBootApplication
@EnableMongoAuditing
class SoloPlayWebServerApplication

fun main(args: Array<String>) {
	runApplication<SoloPlayWebServerApplication>(*args)
}

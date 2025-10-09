package com.example.solo_play_web_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
@EnableMongoAuditing
class SoloPlayWebServerApplication

fun main(args: Array<String>) {
	runApplication<SoloPlayWebServerApplication>(*args)
}

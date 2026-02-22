package com.verobee.vergate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VergateApplication

fun main(args: Array<String>) {
    runApplication<VergateApplication>(*args)
}

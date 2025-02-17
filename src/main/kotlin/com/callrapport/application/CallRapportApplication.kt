package com.callrapport
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CallRapportApplication

fun main(args: Array<String>) {
	runApplication<CallRapportApplication>(*args)
}

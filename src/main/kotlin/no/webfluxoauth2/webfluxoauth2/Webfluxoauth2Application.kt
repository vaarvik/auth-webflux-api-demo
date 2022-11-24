package no.webfluxoauth2.webfluxoauth2

import no.webfluxoauth2.webfluxoauth2.config.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class Webfluxoauth2Application

fun main(args: Array<String>) {
	runApplication<Webfluxoauth2Application>(*args)
}

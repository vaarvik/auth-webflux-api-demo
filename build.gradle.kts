import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.5"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("plugin.jpa") version "1.6.21"
}

group = "no.webfluxoauth2"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.jsonwebtoken:jjwt:0.9.1")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.postgresql:postgresql:42.5.0")
	compileOnly("javax.servlet:javax.servlet-api")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()

	testLogging {
		// set options for log level LIFECYCLE
		events(
			org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
			org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
			org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
			org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT)
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
		showExceptions = true
		showCauses = true
		showStackTraces = true

		// set options for log level DEBUG and INFO
		debug {
			events(
				org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED,
				org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
				org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
				org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
				org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR,
				org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT)
			exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
		}
		info.events = debug.events
		info.exceptionFormat = debug.exceptionFormat

		// See https://github.com/gradle/kotlin-dsl/issues/836
		addTestListener(object : TestListener {
			override fun beforeSuite(suite: TestDescriptor) {}
			override fun beforeTest(testDescriptor: TestDescriptor) {}
			override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}

			override fun afterSuite(suite: TestDescriptor, result: TestResult) {
				if (suite.parent == null) { // root suite
					val output = "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)"
					val startItem = "|  "
					val endItem = "  |"
					val repeatLength: Int = startItem.length + output.length + endItem.length
					println("\n ${("-".repeat(repeatLength))}\n${ startItem }${ output }${ endItem }\n${ "-".repeat(repeatLength) }")
				}
			}
		})
	}
}
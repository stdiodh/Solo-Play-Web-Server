plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	runtimeOnly("com.mysql:mysql-connector-j")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// JpaRepository를 사용하기 위한 라이브러리
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	// OpenAPI 3 문서화 및 Swagger UI 제공을 위한 라이브러리
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
	// 사용자 관리를 위한 Spring security 라이브러리
	implementation("org.springframework.boot:spring-boot-starter-security")
	//단위테스트를 위한 springmockk 라이브러리
	testImplementation("com.ninja-squad:springmockk:4.0.2")
	//단위 테스트를 위한 mockk 라이브러리
	testImplementation("io.mockk:mockk:1.12.8")
	//jjwp Token을 사용하기 위한 라이브러리
	implementation("io.jsonwebtoken:jjwt-api:0.12.5")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":plugin-api"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.pf4j:pf4j-spring:0.9.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

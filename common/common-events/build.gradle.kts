plugins {
    id("service-orders.java-library")
    id("com.github.davidmc24.gradle.plugin.avro")
}

dependencies {
    api("org.apache.avro:avro:1.12.0")
}
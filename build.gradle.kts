plugins {
    id("org.jetbrains.kotlin.jvm") version("1.6.20")
}

apply {
    plugin("kotlin")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", "1.6.20"))
    implementation(group="com.google.guava", name="guava", version="31.1-jre")
    testImplementation("junit:junit:4.12")
}

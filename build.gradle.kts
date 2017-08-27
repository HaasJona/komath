plugins {
    kotlin("jvm", "1.1.4-2")
}

apply {
    plugin("kotlin")
}

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib", "1.1.4-2"))
    compile(group="com.google.guava", name="guava", version="22.0")
    testCompile("junit:junit:4.12")
}

buildscript {

    repositories {
        gradleScriptKotlin()
    }

    dependencies {
        classpath(kotlinModule("gradle-plugin"))
    }
}

plugins {
}

apply {
    plugin("kotlin")
}

repositories {
    gradleScriptKotlin()
}

dependencies {
    compile(kotlinModule("stdlib", "1.1.4"))
    compile(group="com.google.guava", name="guava",version="22.0")
    testCompile("junit:junit:4.12")
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.7.22" apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false
}

subprojects {
    configurations.all {
        resolutionStrategy.force(
            "org.jetbrains.kotlin:kotlin-stdlib:1.7.22",
            "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.7.22",
            "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22",
            "org.jetbrains.kotlin:kotlin-reflect:1.7.22"
        )
    }
}

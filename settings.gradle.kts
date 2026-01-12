rootProject.name = "testernest-sdk"

plugins {
    id("com.gradleup.nmcp.settings") version "1.4.3"
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

nmcpSettings {
    centralPortal {
        username = providers.gradleProperty("centralPortalUsername").orNull
        password = providers.gradleProperty("centralPortalPassword").orNull
        publishingType = "USER_MANAGED"
        val currentVersion = providers.gradleProperty("version").orNull ?: "0.1.0"
        publicationName = "testernest-android:$currentVersion"
    }
}

include(":testernest-core", ":testernest-android", ":sample-native")

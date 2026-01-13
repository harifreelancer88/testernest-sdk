plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("org.jetbrains.dokka")
    id("signing")
}

android {
    namespace = "com.testernest.android"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    api(project(":testernest-core"))
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
}

val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaJavadoc"))
}

val pomUrl = providers.gradleProperty("POM_URL").orNull ?: "REPLACE_ME"
val pomScmUrl = providers.gradleProperty("POM_SCM_URL").orNull ?: "REPLACE_ME"
val pomScmConnection = providers.gradleProperty("POM_SCM_CONNECTION").orNull ?: "REPLACE_ME"
val pomScmDeveloperConnection = providers.gradleProperty("POM_SCM_DEV_CONNECTION").orNull ?: "REPLACE_ME"
val pomLicenseName = providers.gradleProperty("POM_LICENSE_NAME").orNull ?: "REPLACE_ME"
val pomLicenseUrl = providers.gradleProperty("POM_LICENSE_URL").orNull ?: "REPLACE_ME"
val pomDeveloperId = providers.gradleProperty("POM_DEVELOPER_ID").orNull ?: "REPLACE_ME"
val pomDeveloperName = providers.gradleProperty("POM_DEVELOPER_NAME").orNull ?: "REPLACE_ME"
val pomDeveloperEmail = providers.gradleProperty("POM_DEVELOPER_EMAIL").orNull ?: "REPLACE_ME"
val pomName = providers.gradleProperty("POM_NAME").orNull ?: "REPLACE_ME"
val pomDescription = providers.gradleProperty("POM_DESCRIPTION").orNull ?: "REPLACE_ME"

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.testernest"
                artifactId = "testernest-android"
                version = project.version.toString()
                artifact(dokkaJavadocJar)
                pom {
                    name.set(pomName)
                    description.set(pomDescription)
                    url.set(pomUrl)
                    licenses {
                        license {
                            name.set(pomLicenseName)
                            url.set(pomLicenseUrl)
                        }
                    }
                    developers {
                        developer {
                            id.set(pomDeveloperId)
                            name.set(pomDeveloperName)
                            email.set(pomDeveloperEmail)
                        }
                    }
                    scm {
                        url.set(pomScmUrl)
                        connection.set(pomScmConnection)
                        developerConnection.set(pomScmDeveloperConnection)
                    }
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

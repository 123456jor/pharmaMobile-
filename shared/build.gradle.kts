import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import app.cash.sqldelight.gradle.DriverInitializer
import app.cash.sqldelight.gradle.VerifyMigrationTask
import app.cash.sqldelight.core.SqlDelightDatabaseProperties
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    android {
       namespace = "pe.edu.upeu.pharmamobil.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.sqldelight.android)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            // api: MainApplication (androidApp) usa androidContext() al arrancar Koin.
            api(libs.koin.android)
            implementation(libs.ktor.client.cio)
            implementation(libs.androidx.security.crypto)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            // api: androidApp llama a initKoin(), cuya firma expone KoinAppDeclaration.
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        named("androidHostTest") {
            dependencies { implementation(libs.sqldelight.jvm) }
        }
    }
}

sqldelight {
    databases {
        create("PharmaDb") {
            packageName.set("pe.edu.upeu.pharmamobil.db")
            // verifyCommonMainPharmaDbMigration compara databases/2.db + 2.sqm con el CREATE actual.
            // La generación conserva su modo previo: 1.sqm empieza en la sesión 11 y no
            // contiene el CREATE histórico de producto. No se inventa una migración 0.
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

// SQLDelight 2.1.0 / Windows: el worker aislado pierde el temporal del proceso padre.
// Su extensión pública DriverInitializer fija la ruta dentro del propio worker.
// Conserva la tarea original y la comparación de esquemas, sin privilegios de administrador.
val compilarInicializadorSqlite by tasks.registering(JavaCompile::class) {
    source(rootProject.file("gradle/sqlite-worker/src"))
    classpath = files(
        DriverInitializer::class.java.protectionDomain.codeSource.location,
        SqlDelightDatabaseProperties::class.java.protectionDomain.codeSource.location
    )
    destinationDirectory.set(layout.buildDirectory.dir("sqlite-worker/classes"))
    sourceCompatibility = "11"
    targetCompatibility = "11"
}
val inicializadorSqlite by tasks.registering(Jar::class) {
    archiveFileName.set("sqlite-worker.jar")
    destinationDirectory.set(layout.buildDirectory.dir("sqlite-worker"))
    from(compilarInicializadorSqlite.flatMap { it.destinationDirectory })
    from(rootProject.file("gradle/sqlite-worker/resources"))
}
// El plugin registra estas tareas al terminar la evaluación y fija su classpath.
// Adjuntar después evita que ese setFrom sustituya nuestro inicializador.
afterEvaluate {
    tasks.withType<VerifyMigrationTask>().configureEach {
        classpath.from(inicializadorSqlite)
        driverProperties.put("directorioTemporalSqlite",
            layout.buildDirectory.dir("tmp/sqlite-jdbc").map { it.asFile.absolutePath })
    }
}
tasks.withType<Test>().configureEach {
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
    }
}

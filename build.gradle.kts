import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.closure
import org.jetbrains.changelog.markdownToHTML

plugins {
    id("java")
//    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    id("org.jetbrains.intellij") version "0.6.3"
    id("org.jetbrains.changelog") version "0.6.2"
    id("io.gitlab.arturbosch.detekt") version "1.14.2"
}

// Import variables from gradle.properties file
// `pluginName_` variable ends with `_` because of the collision with Kotlin magic getter in the `intellij` closure.
// Read more about the issue: https://github.com/JetBrains/intellij-platform-plugin-template/issues/29
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginVerifierIdeVersions: String by project

val platformType: String by project
val platformVersion: String by project
val platformDownloadSources: String by project

group = "com.nvlad"
version = pluginVersion


val platformPluginsAssociation = hashMapOf<String, String>()
platformPluginsAssociation["2019.1.4"] = "com.jetbrains.php:191.8026.56, org.jetbrains.plugins.phpstorm-remote-interpreter:191.5849.22, com.jetbrains.twig:191.6183.95"
platformPluginsAssociation["2020.2.3"] = "com.jetbrains.php:202.7660.42, org.jetbrains.plugins.phpstorm-remote-interpreter:202.6397.59, com.jetbrains.twig:202.6397.21"
platformPluginsAssociation["2020.3.3"] = "com.jetbrains.php:203.7717.11, org.jetbrains.plugins.phpstorm-remote-interpreter:203.5981.155, com.jetbrains.twig:203.6682.75"
val bundledPlugins = "DatabaseTools, webDeployment, CSS, terminal, coverage, java-i18n, remote-run, properties"

val platformPlugins = platformPluginsAssociation[platformVersion] + ", $bundledPlugins"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.14.2")

    implementation("io.sentry:sentry:1.7.12") {
        exclude("org.slf4j", "slf4j-api");
        exclude("com.fasterxml.jackson.core", "jackson-core");
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
}

intellij {
    pluginName = pluginName_
    version = platformVersion
    type = platformType
    downloadSources = platformDownloadSources.toBoolean()
    updateSinceUntilBuild = true

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    setPlugins(*platformPlugins.split(',').map(String::trim).filter(String::isNotEmpty).toTypedArray())
}

sourceSets {
    main {
        java {
            srcDirs("src")
//            assemble "com.rollbar:rollbar-java:1.3.1"
        }
        resources {
            srcDirs("resources")
        }
    }
    /*test {
        java {
            srcDirs("tests")
        }
    }*/
}

tasks {
    // Set the compatibility versions to 1.8
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    withType<Detekt> {
        jvmTarget = "1.8"
    }

    patchPluginXml {
        version(pluginVersion)
        sinceBuild(pluginSinceBuild)
        untilBuild(null)

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription(
            closure {
                File("./DESCRIPTION.md").readText().lines().joinToString("\n").run { markdownToHTML(this) }
            }
        )

        // Get the latest available change notes from the changelog file
//        changeNotes(
//                closure {
//                    changelog.getLatest().toHTML()
//                }
//        )
    }

   // test {
//        useJUnitPlatform()
     //   reports {
       //     junitXml.isEnabled = true
        //}
   // }

   // runPluginVerifier {
     //   ideVersions(pluginVerifierIdeVersions)
    //}

    //publishPlugin {
      //  dependsOn("patchChangelog")
        //token(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html#specifying-a-release-channel
        //channels(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
    //}
}

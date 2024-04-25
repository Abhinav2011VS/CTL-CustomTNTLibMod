import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    id("idea")
    id("eclipse")
    id("maven-publish")
    id("net.minecraftforge.gradle") version("[6.0,6.2)")
    id("org.parchmentmc.librarian.forgegradle") version("1.+")
}

// gradle.properties
val forgeVersion: String by extra
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val parchmentVersionForge: String by extra

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken: String? by project

val baseArchivesName = "${modId}-${minecraftVersion}-forge"
base {
    archivesName.set(baseArchivesName)
}

sourceSets {
    named("test") {
        resources {
            //The test module has no resources
            setSrcDirs(emptyList<String>())
        }
    }
}

val dependencyProjects: List<Project> = listOf(
    project(":Common")
)

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}
project.evaluationDependsOn(":Changelog")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
    }
    withSourcesJar()
}

dependencies {
    "minecraft"(
        group = "net.minecraftforge",
        name = "forge",
        version = "${minecraftVersion}-${forgeVersion}"
    )
    dependencyProjects.forEach {
        compileOnly(it)
    }
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter-api",
        version = jUnitVersion
    )
    testRuntimeOnly(
        group = "org.junit.jupiter",
        name = "junit-jupiter-engine",
        version = jUnitVersion
    )
}

minecraft {
    mappings("parchment", parchmentVersionForge)

    copyIdeResources.set(true)

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        val client = create("client") {
            taskName("runClientDev")
            property("forge.logging.console.level", "debug")
            workingDirectory(file("run/client/Dev"))
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
        create("client_01") {
            taskName("runClientPlayer01")
            parent(client)
            workingDirectory(file("run/client/Player01"))
            args("--username", "Player01")
        }
        create("client_02") {
            taskName("runClientPlayer02")
            parent(client)
            workingDirectory(file("run/client/Player02"))
            args("--username", "Player02")
        }
        create("server") {
            taskName("Server")
            property("forge.logging.console.level", "debug")
            workingDirectory(file("run/server"))
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
        create("game_test_server") {
            taskName("runGameTestServer")
            workingDirectory(file("run"))

            property("forge.logging.markers", "REGISTRIES")

            property("forge.logging.console.level", "debug")

            property("forge.enabledGameTestNamespaces", modId)
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
        create("data") {
            taskName("runData")
            workingDirectory(file("run"))

            property("forge.logging.markers", "REGISTRIES")

            property("forge.logging.console.level", "debug")

            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            args("--mod", modId, "--all", "--output", file("src/generated/resources/"), "--existing", file("src/main/resources/"))

            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    dependencyProjects.forEach {
        source(it.sourceSets.main.get().getAllSource())
    }
}

tasks.processResources {
    dependencyProjects.forEach {
        from(it.sourceSets.main.get().getResources())
    }
}

tasks.jar {
    from(sourceSets.main.get().output)

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    finalizedBy("reobfJar")
}

val sourcesJarTask = tasks.named<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    for (p in dependencyProjects) {
        from(p.sourceSets.main.get().allJava)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set("sources")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    include("net/abhinav/ctl/test/**")
    exclude("net/abhinav/ctl/test/lib/**")
    outputs.upToDateWhen { false }
    testLogging {
        events = setOf(TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

artifacts {
    archives(tasks.jar.get())
    archives(sourcesJarTask.get())
}

publishing {
    publications {
        register<MavenPublication>("forgeJar") {
            artifactId = baseArchivesName
            artifact(tasks.jar.get())
            artifact(sourcesJarTask.get())

            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                dependencyProjects.forEach {
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", it.group)
                    dependencyNode.appendNode("artifactId", it.base.archivesName.get())
                    dependencyNode.appendNode("version", it.version)
                }
            }
        }
    }
    repositories {
        val deployDir = project.findProperty("DEPLOY_DIR")
        if (deployDir != null) {
            maven(deployDir)
        }
    }
}

idea {
    module {
        for (fileName in listOf("run", "out", "logs")) {
            excludeDirs.add(file(fileName))
        }
    }
}
// Required because FG, copied from the MDK
sourceSets.forEach {
    val outputDir = layout.buildDirectory.file("sourcesSets/${it.name}").get().getAsFile()
    it.output.setResourcesDir(outputDir)
    it.java.destinationDirectory.set(outputDir)
}
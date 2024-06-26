plugins {
    id 'fabric-loom' version '1.5-SNAPSHOT'
    id 'java'
    id 'idea'
}

base {
    archivesName = "${mod_jar_name}-${version}-fabric-${minecraft_version}"
}

repositories {
    maven { url "https://maven.terraformersmc.com/releases/" }
}

dependencies {
    compileOnly group:'org.spongepowered', name:'mixin', version:'0.8.5'

    minecraft "com.mojang:minecraft:${minecraft_version}"

    //mappings loom.officialMojangMappings()
    mappings loom.layered() {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.19.3:2023.12.31@zip")
    }

    modImplementation "net.fabricmc:fabric-loader:${fabric_loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${fabric_version}"
    implementation group: 'com.google.code.findbugs', name: 'jsr305', version: '3.0.1'
    implementation project(":Common")

    modApi("com.terraformersmc:modmenu:9.0.0-pre.1") { exclude(module: "fabric-api") }
}

loom {
    if (project(":Common").file("src/main/resources/${mod_id}.accesswidener").exists()) {
        accessWidenerPath.set(project(":Common").file("src/main/resources/${mod_id}.accesswidener"))
    }

    mixin {
        defaultRefmapName.set("${mod_id}_fabric.refmap.json")
    }

    runs {
        client {
            client()
            setConfigName("Fabric Client")
            ideConfigGenerated(true)
            runDir("run")
        }
        server {
            server()
            setConfigName("Fabric Server")
            ideConfigGenerated(true)
            runDir("run")
        }
    }
}

tasks.named('compileJava', JavaCompile).configure {
    source(project(":Common").sourceSets.main.allSource)
}
tasks.withType(Javadoc).configureEach {
    source(project(":Common").sourceSets.main.allJava)
}
tasks.named("sourcesJar", Jar) {
    from(project(":Common").sourceSets.main.allSource)
}

processResources {
    from project(":Common").sourceSets.main.resources
}
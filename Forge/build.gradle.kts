plugins {
    id 'idea'
    id 'maven-publish'
    id 'net.minecraftforge.gradle' version '[6.0.16,6.2)'
    id 'org.parchmentmc.librarian.forgegradle' version '1.+'
    id 'org.spongepowered.mixin' version "0.7-SNAPSHOT" // mixin
}

base {
    archivesName = "${mod_jar_name}-${version}-forge-${minecraft_version}"
}

minecraft {
    //mappings channel: 'official', version: minecraft_version
    mappings channel: mapping_channel, version: "${parchment_version_forge}-${minecraft_version}"

    copyIdeResources = true //Calls processResources when in dev

    // Automatically enable forge AccessTransformers if the file exists
    // This location is hardcoded in Forge and can not be changed.
    // https://github.com/MinecraftForge/MinecraftForge/blob/be1698bb1554f9c8fa2f58e32b9ab70bc4385e60/fmlloader/src/main/java/net/minecraftforge/fml/loading/moddiscovery/ModFile.java#L123
    if (file('src/main/resources/META-INF/accesstransformer.cfg').exists()) {
        accessTransformer = file('src/main/resources/META-INF/accesstransformer.cfg')
    }

    runs {
        client {
            workingDirectory project.file('run')

            ideaModule "${rootProject.name}.${project.name}.main"
            taskName 'Client'
            property 'mixin.env.remapRefMap', 'true'
            property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
            mods {
                modClientRun {
                    source sourceSets.main
                            source project(":Common").sourceSets.main
                }
            }
        }

        server {
            workingDirectory project.file('run')

            ideaModule "${rootProject.name}.${project.name}.main"
            taskName 'Server'
            property 'mixin.env.remapRefMap', 'true'
            property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
            mods {
                modServerRun {
                    source sourceSets.main
                            source project(":Common").sourceSets.main
                }
            }
        }

        data {
            workingDirectory project.file('run')

            ideaModule "${rootProject.name}.${project.name}.main"
            args '--mod', mod_id, '--all', '--output', file('src/generated/resources/'), '--existing', file('src/main/resources/')
            taskName 'Data'
            property 'mixin.env.remapRefMap', 'true'
            property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
            mods {
                modDataRun {
                    source sourceSets.main
                            source project(":Common").sourceSets.main
                }
            }
        }
    }
}

//sourceSets.main.resources.srcDir 'src/generated/resources'
sourceSets.main.resources { srcDir 'src/generated/resources' }

dependencies {
    compileOnly group:'org.spongepowered', name:'mixin', version:'0.8.5'

    minecraft "net.minecraftforge:forge:${minecraft_version}-${forge_version}"
    compileOnly project(":Common")
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT:processor")
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

sourceSets.each {
    def dir = layout.buildDirectory.dir("sourcesSets/$it.name")
    it.output.resourcesDir = dir
    it.java.destinationDirectory = dir
}

processResources {
    from project(":Common").sourceSets.main.resources
}

jar.finalizedBy('reobfJar')

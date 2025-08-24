plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta11"
}

val git : String = versionBanner()
val builder : String = builder()
ext["git_version"] = git
ext["builder"] = builder

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")

    tasks.processResources {
        filteringCharset = "UTF-8"

        filesMatching(arrayListOf("custom-crops.properties")) {
            expand(rootProject.properties)
        }

        filesMatching(arrayListOf("*.yml", "*/*.yml")) {
            expand(
                Pair("project_version", rootProject.properties["project_version"]),
                Pair("config_version", rootProject.properties["config_version"])
            )
        }
    }
}

fun versionBanner(): String {
    return try {
        project.providers.exec {
            commandLine("git", "rev-parse", "--short=8", "HEAD")
        }.standardOutput.asText.map { it.trim() }.getOrElse("Unknown")
    } catch (e: Exception) {
        // 在 GitHub Actions 或其他 CI 环境中，使用环境变量或默认值
        System.getenv("GITHUB_SHA")?.take(8) ?: "Unknown"
    }
}

fun builder(): String {
    return try {
        project.providers.exec {
            commandLine("git", "config", "user.name")
        }.standardOutput.asText.map { it.trim() }.getOrElse("Unknown")
    } catch (e: Exception) {
        // 在 GitHub Actions 或其他 CI 环境中，使用环境变量或默认值
        System.getenv("GITHUB_ACTOR") ?: "GitHub Actions"
    }
}

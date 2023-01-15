package dev.timatifey.divkit.idea.assembler

sealed interface Dependency {
    data class Local(
            val artifactPath: String
    ) : Dependency

    data class Remote(
            val groupId: String,
            val artifactId: String,
            val version: String
    ) : Dependency
}

sealed interface DivKitDependency {
    object Local : DivKitDependency
    object Remote : DivKitDependency
}

fun Dependency.implementation(): String = when (this) {
    is Dependency.Local -> "implementation files('$artifactPath')"
    is Dependency.Remote -> "implementation '$groupId:$artifactId:$version'"
}

val requiredDivKitDependencies = arrayOf("div-core", "div", "div-json")
const val BASE_GROUP_ID = "com.yandex.div"
const val BASE_VERSION = "24.3.0"

fun defaultRemoteDivKitDependencies(version: String): MutableMap<String, Dependency> =
        requiredDivKitDependencies.associateWith { artifactId ->
            Dependency.Remote(
                    groupId = BASE_GROUP_ID,
                    artifactId = artifactId,
                    version = version
            )
        }.toMutableMap()

import com.yiwenliu.tmdb.configureBuildConfigSecrets
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.create

abstract class BuildConfigSecretsExtension {
    abstract val keys: ListProperty<String>
}

class BuildConfigSecretsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val secrets = extensions.create<BuildConfigSecretsExtension>("buildConfigSecrets")
                .apply { keys.convention(emptyList()) }
            configureBuildConfigSecrets(secrets.keys)
        }
    }
}

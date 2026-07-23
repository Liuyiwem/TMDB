import com.yiwenliu.tmdb.configureSpotlessForRootProject
import org.gradle.api.Plugin
import org.gradle.api.Project

class RootConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        require(target.path == ":")
        target.configureSpotlessForRootProject()
    }
}

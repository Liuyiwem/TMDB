import com.yiwenliu.tmdb.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "tmdb.android.library")
            apply(plugin = "tmdb.android.library.compose")
            apply(plugin = "tmdb.android.hilt")

            dependencies {
                "implementation"(project(":core:common"))
                "implementation"(project(":core:ui"))
                "implementation"(project(":domain"))

                "implementation"(libs.findLibrary("androidx-hilt-navigation-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                "implementation"(libs.findLibrary("androidx-navigation3-runtime").get())

                "testImplementation"(project(":core:testing"))

                "androidTestImplementation"(libs.findLibrary("androidx-junit").get())
                "androidTestImplementation"(libs.findLibrary("androidx-espresso-core").get())
                "androidTestImplementation"(libs.findLibrary("androidx-ui-test-junit4").get())
            }
        }
    }
}

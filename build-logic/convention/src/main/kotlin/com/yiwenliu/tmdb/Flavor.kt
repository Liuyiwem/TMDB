package com.yiwenliu.tmdb

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ProductFlavor

@Suppress("EnumEntryName")
enum class FlavorDimension {
    contentType,
}

@Suppress("EnumEntryName")
enum class TMDBFlavor(
    val dimension: FlavorDimension,
    val applicationIdSuffix: String? = null,
) {
    mock(FlavorDimension.contentType, applicationIdSuffix = ".mock"),
    prod(FlavorDimension.contentType),
}

fun configureFlavors(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    flavorConfigurationBlock: ProductFlavor.(flavor: TMDBFlavor) -> Unit = {},
) {
    commonExtension.apply {
        FlavorDimension.values().forEach { flavorDimensions += it.name }

        productFlavors {
            TMDBFlavor.values().forEach { tmdbFlavor ->
                register(tmdbFlavor.name) {
                    dimension = tmdbFlavor.dimension.name
                    flavorConfigurationBlock(this, tmdbFlavor)
                    if (this@apply is ApplicationExtension && this is ApplicationProductFlavor) {
                        tmdbFlavor.applicationIdSuffix?.let { applicationIdSuffix = it }
                    }
                }
            }
        }
    }
}

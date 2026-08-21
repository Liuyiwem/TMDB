package com.yiwenliu.tmdb

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.kotlin.dsl.invoke

private data class DeviceConfig(val device: String, val apiLevel: Int, val systemImageSource: String) {
    val taskName = buildString {
        append(device.lowercase().replace(" ", ""))
        append("api")
        append(apiLevel)
        append(systemImageSource.replace("-", ""))
    }
}

private val deviceConfigs = listOf(
    DeviceConfig("Pixel 6", 34, "aosp-atd"),
)

internal fun configureGradleManagedDevices(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.testOptions {
        managedDevices {
            allDevices {
                deviceConfigs.forEach { deviceConfig ->
                    maybeCreate(deviceConfig.taskName, ManagedVirtualDevice::class.java).apply {
                        device = deviceConfig.device
                        apiLevel = deviceConfig.apiLevel
                        systemImageSource = deviceConfig.systemImageSource
                    }
                }
            }
            groups {
                maybeCreate("ci").apply {
                    targetDevices.addAll(allDevices)
                }
            }
        }
    }
}

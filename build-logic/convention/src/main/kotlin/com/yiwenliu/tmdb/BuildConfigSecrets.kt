package com.yiwenliu.tmdb

import com.android.build.api.variant.BuildConfigField
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.kotlin.dsl.getByType
import java.util.Properties

internal fun Project.configureBuildConfigSecrets(keys: ListProperty<String>) {
    extensions.getByType<LibraryAndroidComponentsExtension>().apply {
        finalizeDsl { library -> library.buildFeatures.buildConfig = true }
        onVariants { variant ->
            keys.get().forEach { key ->
                // 在 configuration 期就解析並驗證。ValueSource 與 environmentVariable 的
                // 讀取都會被 configuration cache 追蹤，所以 local.properties 或環境變數
                // 改變時 cache 會正確失效。
                //
                // 刻意【不】用 .orElse(provider { error(...) }) 把缺漏往後延：Gradle 在
                // 序列化 configuration cache 時會走完整條 provider 鏈、連 fallback 也評估，
                // 於是即使主要值存在也會拋錯（實測訊息是
                // "Configuration cache state could not be cached: ... DefaultMapProperty"）。
                val value = requiredSecret(key)
                variant.buildConfigFields?.put(
                    key,
                    provider { BuildConfigField("String", "\"${value.escapedForJavaLiteral()}\"", null) },
                )
            }
        }
    }
}

/**
 * 解析一個建置密鑰，缺少時讓建置失敗。
 *
 * 舊版是 `.orElse("")`，缺漏會變成空字串，而空字串的後果在執行期才炸、訊息完全不相干：
 * 空的 `BASE_URL` 讓 `Retrofit.Builder().baseUrl("")` 在 Hilt 的 `@Provides` 裡拋例外
 * （開 app 就是一個被 Dagger 包住的崩潰）；空的 `API_TOKEN` 送出 `Bearer ` 得到 401，
 * 使用者看到「Client error」；空的 `IMAGE_URL` 讓所有海報靜默空白。
 *
 * 對一個會被 clone 的 repo 來說，這是最糟的失敗模式 —— 建置成功、安裝成功、然後爆在
 * 一個跟成因無關的地方。寧可在建置就停下來，訊息還能告訴人要去改哪裡。
 */
private fun Project.requiredSecret(key: String): String = providers.of(LocalPropertyValueSource::class.java) {
    parameters.localProperties.set(
        rootProject.layout.projectDirectory.file("local.properties"),
    )
    parameters.key.set(key)
}
    .orElse(
        // 環境變數要套用跟 LocalPropertyValueSource 同一條「空字串視同沒設定」的規則。
        // CI 上一個忘了在 repo settings 加的 secret，會被 GitHub Actions 展開成空字串
        // 而不是報錯——那在 Gradle 眼中是「present，值為空」而不是「absent」，
        // 沒有這個 filter 就會安靜地通過下面的缺漏檢查，送出 `Authorization: Bearer `。
        providers.environmentVariable(key).map { it.trim() }.filter { it.isNotEmpty() },
    )
    .orNull
    ?: error(
        "Missing build secret `$key`. Add `$key=<value>` to local.properties, " +
            "or export $key as an environment variable.",
    )

/**
 * 產生的是 Java 原始碼，值必須是合法的字串字面值。
 *
 * CR/LF 也一定要跳脫:JLS 裡能提前終止字串字面值的就是 `"` `\` CR LF 四個字元。
 * 而且真的碰得到——Properties.load 會處理跳脫序列，所以 local.properties 寫
 * `KEY=a\nb` 讀出來是含真正換行的三字元字串，而 .trim() 只清頭尾、清不掉中間那個。
 * 沒跳脫的話產生的 BuildConfig.java 會出現未閉合的字串字面值，
 * javac 的錯誤訊息指向一個產生出來的檔案，完全連不回 local.properties。
 *
 * 反斜線必須【第一個】換：先換 \n 的話，補進去的那個反斜線會被後面的反斜線規則
 * 再跳脫一次，變成字面的 \n 兩個字元而不是換行。
 */
private fun String.escapedForJavaLiteral(): String = replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "\\r")

abstract class LocalPropertyValueSource : ValueSource<String, LocalPropertyValueSource.Params> {
    interface Params : ValueSourceParameters {
        val localProperties: RegularFileProperty
        val key: Property<String>
    }

    override fun obtain(): String? {
        val file = parameters.localProperties.asFile.get()
        if (!file.exists()) return null
        return Properties()
            .apply { file.inputStream().use { load(it) } }
            .getProperty(parameters.key.get())
            ?.trim()
            // `KEY=`（有 key、值為空）會回傳 ""（不是 null），provider 因此「有值」，
            // 下游的 environmentVariable fallback 與缺漏檢查就永遠碰不到。
            // 空字串視同沒設定。
            ?.takeIf { it.isNotEmpty() }
    }
}

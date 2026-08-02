package com.yiwenliu.core.network.mock

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import com.yiwenliu.core.common.network.Dispatcher
import com.yiwenliu.core.common.network.TMDBDispatchers.IO
import com.yiwenliu.core.network.api.TMDBApiService
import com.yiwenliu.core.network.model.MovieResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.BufferedReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 必須是 @Singleton。
 *
 * mock flavor 的 FlavoredNetworkModule 是 @Binds @Singleton，但它只 scope 了
 * TMDBApiService 這個【綁定 key】。FakeMovieRepository 注入的是【具體型別】
 * MockTMDBApiService，而 TestDataModule.bindMovieRepository 也沒有 scope——所以每個注入點
 * 都會拿到一份全新的實例，測試設定的 [errorToThrow] 永遠影響不到 app 正在用的那一份。
 *
 * 代價：[errorToThrow] 是 var，變成真 singleton 之後就是跨整個 app graph 的共享可變狀態。
 * 之後寫錯誤 E2E 時，每個測試都必須在 @After 把它設回 null，否則會污染同進程的後續測試。
 */
@Singleton
class MockTMDBApiService
@Inject
constructor(
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
    private val assets: MockAssetManager = JvmUnitTestDemoAssetManager,
) : TMDBApiService {
    var errorToThrow: Exception? = null

    override suspend fun getMoviesByCategory(
        category: String,
        page: Int,
    ): MovieResponse {
        errorToThrow?.let { throw it }
        return getDataFromJsonFile("${category}_movies.json")
    }

    override suspend fun searchMovies(
        queryString: String,
        page: Int,
    ): MovieResponse {
        errorToThrow?.let { throw it }
        return getDataFromJsonFile(
            if (queryString == EMPTY_RESULT_QUERY) SEARCH_MOVIES_EMPTY_ASSET else SEARCH_MOVIES_ASSET,
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend inline fun <reified T> getDataFromJsonFile(fileName: String): T = withContext(ioDispatcher) {
        assets.open(fileName).use { inputStream ->
            if (SDK_INT <= M) {
                inputStream
                    .bufferedReader()
                    .use(BufferedReader::readText)
                    .let(networkJson::decodeFromString)
            } else {
                networkJson.decodeFromStream(inputStream)
            }
        }
    }

    companion object {
        /**
         * 這個查詢字串會讓假物件回傳空結果，供「找不到電影」的測試使用。
         *
         * 之所以用一個約定的查詢字串而不是額外的旗標，是因為 mock flavor 是真的裝在裝置上跑的
         * app，E2E 測試唯一能操作它的介面就是輸入框。
         */
        // public 而非 internal：:app 的 E2E 測試需要它，而 internal 是模組層級的。
        // 這是 mock 對測試公開的契約，不是實作細節。
        const val EMPTY_RESULT_QUERY = "zzzznoresults"

        internal const val SEARCH_MOVIES_ASSET = "search_movies.json"

        internal const val SEARCH_MOVIES_EMPTY_ASSET = "search_movies_empty.json"
    }
}

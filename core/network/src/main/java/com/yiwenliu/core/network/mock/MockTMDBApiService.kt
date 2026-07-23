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
        query: String,
        page: Int,
    ): MovieResponse = getDataFromJsonFile(SEARCH_MOVIES_ASSET)

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
        private const val SEARCH_MOVIES_ASSET = "search_movies.json"
    }
}

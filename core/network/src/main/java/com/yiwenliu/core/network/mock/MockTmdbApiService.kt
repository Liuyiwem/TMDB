package com.yiwenliu.core.network.mock

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import androidx.annotation.VisibleForTesting
import com.yiwenliu.core.common.di.Dispatcher
import com.yiwenliu.core.common.di.TmdbDispatchers.IO
import com.yiwenliu.core.network.api.TmdbApiService
import com.yiwenliu.core.network.model.CreditsResponse
import com.yiwenliu.core.network.model.MovieDetailResponse
import com.yiwenliu.core.network.model.MovieResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.BufferedReader
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockTmdbApiService
@Inject
constructor(
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
    private val assets: MockAssetManager = JvmUnitTestDemoAssetManager,
) : TmdbApiService {
    @VisibleForTesting
    var errorToThrow: Exception? = null

    override suspend fun getMoviesByCategory(category: String, page: Int): MovieResponse {
        errorToThrow?.let { throw it }
        return getDataFromJsonFile("${category}_movies.json")
    }

    override suspend fun searchMovies(queryString: String, page: Int): MovieResponse {
        errorToThrow?.let { throw it }
        return getDataFromJsonFile(
            if (queryString == EMPTY_RESULT_QUERY) SEARCH_MOVIES_EMPTY_ASSET else SEARCH_MOVIES_ASSET,
        )
    }

    override suspend fun getMovieDetail(movieId: Int): MovieDetailResponse {
        errorToThrow?.let { throw it }
        return getDataFromJsonFileOrFallback(
            fileName = "movie_detail_$movieId.json",
            fallbackFileName = "movie_detail_$FALLBACK_MOVIE_ID.json",
        )
    }

    override suspend fun getMovieCredits(movieId: Int): CreditsResponse {
        errorToThrow?.let { throw it }
        return getDataFromJsonFileOrFallback(
            fileName = "movie_credits_$movieId.json",
            fallbackFileName = "movie_credits_$FALLBACK_MOVIE_ID.json",
        )
    }

    override suspend fun getMovieRecommendations(movieId: Int, page: Int): MovieResponse {
        errorToThrow?.let { throw it }
        return getDataFromJsonFile(MOVIE_RECOMMENDATIONS_ASSET)
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

    private suspend inline fun <reified T> getDataFromJsonFileOrFallback(
        fileName: String,
        fallbackFileName: String,
    ): T = try {
        getDataFromJsonFile(fileName)
    } catch (_: IOException) {
        getDataFromJsonFile(fallbackFileName)
    }

    companion object {
        const val EMPTY_RESULT_QUERY = "zzzznoresults"

        internal const val SEARCH_MOVIES_ASSET = "search_movies.json"

        internal const val SEARCH_MOVIES_EMPTY_ASSET = "search_movies_empty.json"

        internal const val MOVIE_RECOMMENDATIONS_ASSET = "movie_recommendations.json"

        internal const val FALLBACK_MOVIE_ID = 533535
    }
}

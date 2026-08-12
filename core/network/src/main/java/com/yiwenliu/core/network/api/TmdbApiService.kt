package com.yiwenliu.core.network.api

import com.yiwenliu.core.network.model.CreditsResponse
import com.yiwenliu.core.network.model.MovieDetailResponse
import com.yiwenliu.core.network.model.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {
    @GET("movie/{category}")
    suspend fun getMoviesByCategory(@Path("category") category: String, @Query("page") page: Int = 1): MovieResponse

    @GET("search/movie")
    suspend fun searchMovies(@Query("query") queryString: String, @Query("page") page: Int = 1): MovieResponse

    @GET("movie/{movieId}")
    suspend fun getMovieDetail(@Path("movieId") movieId: Int): MovieDetailResponse

    @GET("movie/{movieId}/credits")
    suspend fun getMovieCredits(@Path("movieId") movieId: Int): CreditsResponse

    @GET("movie/{movieId}/recommendations")
    suspend fun getMovieRecommendations(@Path("movieId") movieId: Int, @Query("page") page: Int = 1): MovieResponse
}

package com.yiwenliu.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieResponse(
    @SerialName("page") val page: Int = 1,
    // results 與 total_pages 刻意【不】給預設值。
    //
    // NetworkModule 的 Json 設了 coerceInputValues = true——有預設值的欄位收到 null 時會被
    // 悄悄換成預設值。對這兩個欄位來說那是災難：results 變空清單、total_pages 變 0
    // （nextPageKeyOf 直接回 null），使用者看到一片空白 grid 而且分頁靜默停止，
    // 畫面上不會有任何錯誤。
    //
    // 沒有預設值時，同樣的回應會拋 SerializationException，被 safeCall 映成
    // NetworkError.SERIALIZATION，螢幕顯示「Data parsing error」。API 契約壞掉就該大聲。
    @SerialName("results") val results: List<MovieResult>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int = 0,
)

@Serializable
data class MovieResult(
    // id 刻意【不】給預設值。UI 用 itemKey { it.id } 當 LazyVerticalGrid 的 key，
    // MoviePagingSource 的 seenIds 也用它去重。給 0 當預設會讓多筆缺 id 的資料撞成
    // 同一個 key——第一筆之後全被 seenIds 無聲濾掉，是靜默資料遺失。
    // 沒有 id 的項目本來就沒救，寧可讓這一頁失敗。
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String = "",
    @SerialName("overview") val overview: String = "",
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("poster_path") val posterPath: String = "",
    @SerialName("backdrop_path") val backdropPath: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("popularity") val popularity: Double = 0.0,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
)

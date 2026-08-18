package com.yiwenliu.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_remote_keys")
data class MovieRemoteKeyEntity(@PrimaryKey val category: String, val nextPage: Int?, val lastUpdated: Long)

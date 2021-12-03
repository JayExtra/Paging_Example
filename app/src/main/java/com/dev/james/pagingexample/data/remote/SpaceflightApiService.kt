package com.dev.james.pagingexample.data.remote

import com.dev.james.pagingexample.model.Article
import retrofit2.http.GET
import retrofit2.http.Query

interface SpaceflightApiService {

    //fetching all articles from the server
    @GET("articles")
    suspend fun getArticles(
        @Query("title_contains") query : String?,
        @Query("_start") start : Int,
        @Query("_limit") limit : Int
    ) : List<Article>
}
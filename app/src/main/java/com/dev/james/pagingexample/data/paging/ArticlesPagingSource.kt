package com.dev.james.pagingexample.data.paging


import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dev.james.pagingexample.data.remote.SpaceflightApiService
import com.dev.james.pagingexample.model.Article
import com.dev.james.pagingexample.utilities.NETWORK_PAGE_SIZE
import com.dev.james.pagingexample.utilities.STARTING_INDEX
import java.io.IOException


class ArticlesPagingSource(
    private val queryString : String?,
    private val spaceflightApiService: SpaceflightApiService
): PagingSource<Int , Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val start = params.key ?: STARTING_INDEX
        val limit = params.loadSize

        return try {
            val articles = spaceflightApiService.getArticles(queryString ,start , limit)
            LoadResult.Page(
                data = articles,
                prevKey = if(start == STARTING_INDEX) null else start - limit,
                nextKey = if(articles.isEmpty()) null else start + limit
            )

        }catch (t : Throwable){
            var exception = t
            if(t is IOException){
                exception = IOException("check your internet connection")
            }
            LoadResult.Error(exception)
        }
    }
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
       return state.anchorPosition
    }
}
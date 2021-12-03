package com.dev.james.pagingexample.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dev.james.pagingexample.data.local.ArticlesDatabase
import com.dev.james.pagingexample.data.paging.ArticlesPagingSource
import com.dev.james.pagingexample.data.paging.ArticlesRemoteMediator
import com.dev.james.pagingexample.data.remote.SpaceflightApiService
import com.dev.james.pagingexample.model.Article
import com.dev.james.pagingexample.utilities.NETWORK_PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class Repository @Inject constructor(
    private val api : SpaceflightApiService,
    private val database : ArticlesDatabase
) {
    /**
     * returns list of articles
    from the paging source
     **/


    @ExperimentalPagingApi
    fun getResultStream(query : String) : Flow<PagingData<Article>> {
        val dQuery = "%${query.replace(' ', '%')}%"
        val pagingSourceFactory = {database.getDao().articleByName(dQuery)}

        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            remoteMediator = ArticlesRemoteMediator(
                query,
                api,
                database
            ),
            pagingSourceFactory = pagingSourceFactory

        ).flow
    }

}
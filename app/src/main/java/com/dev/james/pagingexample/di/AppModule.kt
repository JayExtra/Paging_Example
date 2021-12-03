package com.dev.james.pagingexample.di

import android.app.Application
import androidx.room.Room
import com.dev.james.pagingexample.data.local.ArticlesDao
import com.dev.james.pagingexample.data.local.ArticlesDatabase
import com.dev.james.pagingexample.data.local.RemoteKeysDao
import com.dev.james.pagingexample.data.remote.SpaceflightApiService
import com.dev.james.pagingexample.utilities.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    //setup okhttp interceptor + client
    private val loggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()


    //tell hilt how to provide an instance of retrofit when
    // injected somewhere
    @Provides
    @Singleton
    fun provideRetrofit() : Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    //provide articles api
    @Provides
    @Singleton
    fun provideArticleApi(retrofit : Retrofit) : SpaceflightApiService =
        retrofit.create(SpaceflightApiService::class.java)


    //room setup
    @Singleton
    @Provides
    fun provideDatabase(app : Application) : ArticlesDatabase =
        Room.databaseBuilder(app , ArticlesDatabase::class.java , "articles_database")
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideDao(db : ArticlesDatabase) : ArticlesDao {
        return db.getDao()
    }

    @Singleton
    @Provides
    fun provideRemoteKeyDao(db : ArticlesDatabase) : RemoteKeysDao{
        return db.getRemoteKeysDao()
    }

}
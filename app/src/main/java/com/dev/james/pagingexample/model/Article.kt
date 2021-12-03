package com.dev.james.pagingexample.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "articles_table")
data class Article(
    @PrimaryKey(autoGenerate = false)
    val id : Int,
    val title : String,
    val url : String,
    val imageUrl : String,
    @SerializedName("newsSite")
    val site : String,
    @SerializedName("publishedAt")
    val date : String,
    val featured : Boolean,
    val summary : String
){
    private val dateFormat : SimpleDateFormat
        get() =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    private val newDate : Date get() = dateFormat.parse(date)
    val createdDateFormatted : String
        get() = dateFormat.format(newDate)
}

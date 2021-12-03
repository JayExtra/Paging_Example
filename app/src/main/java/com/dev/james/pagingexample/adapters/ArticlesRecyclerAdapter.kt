package com.dev.james.pagingexample.adapters

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev.james.pagingexample.model.Article
import com.dev.james.pagingexample.ui.activies.R
import com.dev.james.pagingexample.ui.activies.databinding.SingleNewsItemBinding

class ArticlesRecyclerAdapter : PagingDataAdapter<Article,ArticlesRecyclerAdapter.ArticlesViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlesViewHolder {
        val binding = SingleNewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticlesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticlesViewHolder, position: Int) {
        val currentItem = getItem(position)

        Log.i("RecyclerViewAdapter", "onBindViewHolder: Binding item at position ${position.toString()}")

        if (currentItem != null) {
            holder.binding(currentItem)
        }

    }

    inner class ArticlesViewHolder(private val binding : SingleNewsItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun binding(article : Article){

            binding.apply {
                setUpImage(article , binding)
                titleTxt.text = article.title
                dateTxt.text = article.createdDateFormatted
            }
        }


        private fun setUpImage(article: Article, binding: SingleNewsItemBinding) {
            binding.apply {
                Glide.with(binding.root)
                    .load(article.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_image_24)
                    .error(R.drawable.ic_baseline_broken_image_24)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                           progressBar.isInvisible = true
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.isVisible = false
                            return false
                        }

                    })
                    .into(newsImage)
            }

        }
    }


    class DiffCallback : DiffUtil.ItemCallback<Article>(){
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
            oldItem == newItem

    }


}
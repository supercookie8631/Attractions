package com.example.attractions.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.attractions.R

class GalleryAdapter(
    private val context: Context,
    private val images: ArrayList<String>,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(imageUrl: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_gallery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = images[position]
        Glide.with(context)
            .load(imageUrl)
            .override(holder.imageView.width, holder.imageView.height)
            .placeholder(R.drawable.loading_icon)
            .fitCenter()
            .into(holder.imageView)
        // 設置項目點擊監聽器
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(imageUrl)
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}

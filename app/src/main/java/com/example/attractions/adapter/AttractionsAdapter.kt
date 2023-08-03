package com.example.attractions.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.attractions.R
import com.example.attractions.domain.AttractionsDataItem
import com.example.attractions.fragment.DetailFragment
import com.example.attractions.fragment.MainFragmentDirections
import com.example.attractions.ui.fadeThrough
import com.google.android.material.card.MaterialCardView

class AttractionsAdapter(private val listener: OnWebsiteButtonClickListener) :
    RecyclerView.Adapter<AttractionsAdapter.InnerHolder>() {
    private val mContentList = arrayListOf<AttractionsDataItem>()

    private val cardStates = mutableMapOf<Int, Boolean>()

    class InnerHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): InnerHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_item_attractions, parent, false)
        return InnerHolder(itemView)
    }

    override fun onBindViewHolder(holder: InnerHolder, position: Int) {
        holder.itemView.apply {
            val itemCardView = findViewById<MaterialCardView>(R.id.card)
            val small_card = findViewById<ConstraintLayout>(R.id.small_card)
            val big_card = findViewById<ConstraintLayout>(R.id.big_card)

            val small_itemCardView = findViewById<ImageView>(R.id.small_itemCardView)
            val small_itemTitleTv = findViewById<TextView>(R.id.small_itemTitleTv)

            val itemTitleTv = findViewById<TextView>(R.id.itemTitleTv)
            val itemIntroductionTv = findViewById<TextView>(R.id.itemIntroductionTv)
            val itemImageView = findViewById<ImageView>(R.id.itemImageView)
            val btn_detail = findViewById<Button>(R.id.btn_detail)
            val btn_wedsite = findViewById<Button>(R.id.btn_wedsite)

            with(mContentList[position])
            {
                small_itemTitleTv.text = name

                itemTitleTv.text = name
                itemIntroductionTv.text = introduction
                if (images.isNotEmpty() && images[0].src != "") {
                    Glide.with(context)
                        .load(images[0].src)
                        .override(itemImageView.width, itemImageView.height)
                        .placeholder(R.drawable.loading_icon)
                        .fitCenter()
                        .into(itemImageView)
                    Glide.with(context)
                        .load(images[0].src)
                        .transform(CircleCrop())
                        .placeholder(R.drawable.loading_icon)
                        .into(small_itemCardView)
                } else {
                    Glide.with(context)
                        .load(R.drawable.noresult)
                        .into(itemImageView)
                    Glide.with(context)
                        .load(R.drawable.noresult)
                        .transform(CircleCrop())
                        .into(small_itemCardView)
                }

                //恢復保存的卡片狀態
                val isBigCard = cardStates[position] ?: false
                small_card.isVisible = !isBigCard
                big_card.isVisible = isBigCard

                itemCardView.setOnClickListener {
                    val fadeThrough = fadeThrough()
                    if (small_card.isVisible) {
                        TransitionManager.beginDelayedTransition(
                            itemCardView,
                            fadeThrough.setDuration(250L)
                        )
                        small_card.isVisible = false
                        big_card.isVisible = true
                        cardStates[position] = true //保存當前卡片為大卡片
                    } else {
                        TransitionManager.beginDelayedTransition(
                            itemCardView,
                            fadeThrough.setDuration(200L)
                        )
                        small_card.isVisible = true
                        big_card.isVisible = false
                        cardStates[position] = false //保存當前卡片為小卡片
                    }
                }

                btn_detail.setOnClickListener {
                    ViewCompat.setTransitionName(itemCardView, "card")
                    ViewGroupCompat.setTransitionGroup(big_card, true)

                    findNavController().navigate(
                        MainFragmentDirections.actionArticle(position),
                        FragmentNavigatorExtras(
                            itemCardView to DetailFragment.TRANSITION_NAME_BACKGROUND,
                        )
                    )
                }

                btn_wedsite.setOnClickListener {
                    val url = mContentList[position].url
                    listener.onWebsiteButtonClick(url)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mContentList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(it: List<AttractionsDataItem>) {
        mContentList.clear()
        mContentList.addAll(it)
        notifyDataSetChanged()
    }

    open interface OnWebsiteButtonClickListener {
        fun onWebsiteButtonClick(url: String)
    }
}
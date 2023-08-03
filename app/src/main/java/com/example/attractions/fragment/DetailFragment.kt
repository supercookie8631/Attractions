package com.example.attractions.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.attractions.R
import com.example.attractions.adapter.GalleryAdapter
import com.example.attractions.databinding.FragmentDetailBinding
import com.example.attractions.model.AttractionsViewModel
import com.example.attractions.ui.EdgeToEdge
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.transition.MaterialContainerTransform

class DetailFragment : Fragment() {
    private val mAttractionsViewModel: AttractionsViewModel by lazy {
        ViewModelProvider(requireActivity())[AttractionsViewModel::class.java]
    }
    private lateinit var binding: FragmentDetailBinding

    private val args: DetailFragmentArgs by navArgs()

    companion object {
        const val TRANSITION_NAME_BACKGROUND = "background"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform(requireContext(), true)
        sharedElementReturnTransition = MaterialContainerTransform(requireContext(), false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewCompat(view)
        initView()
        setupBackPressedCallback()
    }

    private fun initView() {
//        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
//        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { v ->
            v.findNavController().popBackStack()
        }

        val position = args.position
        var imageUrlList = ArrayList<String>()

        if (mAttractionsViewModel.contentList.value?.get(position)?.images!!.isNotEmpty() && mAttractionsViewModel.contentList.value?.get(
                position
            )?.images?.get(0)!!.src != ""
        ) {
            for (item in mAttractionsViewModel.contentList.value?.get(position)?.images!!) {
                imageUrlList.add(item.src)
            }
        } else {
            imageUrlList = ArrayList()
        }

        // 加載圖片並開始共享元素過渡動畫
        postponeEnterTransition()
        val imageUrl = imageUrlList?.getOrNull(0)
        val placeholderImage = if (imageUrl.isNullOrEmpty()) R.drawable.noresult else imageUrl

        Glide.with(this).load(placeholderImage)
            .override(binding.itemImageView.width, binding.itemImageView.height)
            .placeholder(R.drawable.loading_icon).fitCenter()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }
            }).into(binding.itemImageView)

//        binding.toolbar.title =
//            position?.let { mAttractionsViewModel.contentList.value?.get(it)?.name }

        binding.itemTitleTv.text =
            position?.let { mAttractionsViewModel.contentList.value?.get(it)?.name }
        binding.itemIntroductionTv.text = position?.let {
            mAttractionsViewModel.contentList.value?.get(it)?.introduction
        }
        binding.itemUrl.text = position?.let {
            mAttractionsViewModel.contentList.value?.get(it)?.url
        }

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = imageUrlList?.let {
            GalleryAdapter(requireContext(), it, object : GalleryAdapter.OnItemClickListener {
                override fun onItemClick(imageUrl: String) {
                    // 點擊 RecyclerView 中的項目時，更改 binding.itemImageView 的內容
                    Glide.with(this@DetailFragment).load(imageUrl)
                        .override(binding.itemImageView.width, binding.itemImageView.height)
                        .placeholder(R.drawable.loading_icon)
                        .fitCenter()
                        .into(binding.itemImageView)
                }
            })
        }
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
    }

    private fun setViewCompat(view: View) {
        ViewCompat.setTransitionName(binding.frameLayout, TRANSITION_NAME_BACKGROUND)
        ViewGroupCompat.setTransitionGroup(binding.coordinator, true)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updateLayoutParams<CollapsingToolbarLayout.LayoutParams> {
                topMargin = systemBars.top
            }

            binding.scroll.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = systemBars.top
            }
            binding.content.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }
    }

    private fun setupBackPressedCallback() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 在這裡處理返回鍵按下事件的邏輯
                findNavController().popBackStack() // 返回上一個 Fragment
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, onBackPressedCallback
        )
    }
}
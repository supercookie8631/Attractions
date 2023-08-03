package com.example.attractions.fragment

import android.animation.TimeInterpolator
import android.app.ProgressDialog
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toolbar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import com.example.attractions.R
import com.example.attractions.adapter.AttractionsAdapter
import com.example.attractions.base.LoadState
import com.example.attractions.databinding.FragmentMainBinding
import com.example.attractions.model.AttractionsViewModel
import com.example.attractions.utils.SizeUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class MainFragment : Fragment(), AttractionsAdapter.OnWebsiteButtonClickListener {

    private val mAttractionsViewModel: AttractionsViewModel by lazy {
        ViewModelProvider(requireActivity())[AttractionsViewModel::class.java]
    }
    private lateinit var binding: FragmentMainBinding
    private val mAdapter by lazy { AttractionsAdapter(this) }
    private val mSnackbar by lazy {
        Snackbar.make(
            binding.contentListRv, getString(R.string.wait_loading), Snackbar.LENGTH_SHORT
        )
    }
    private val progressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.web_loading))
            setCancelable(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Fade(Fade.OUT).apply {
            duration = 300L / 2
            interpolator = PathInterpolatorCompat.create(0.4f, 0f, 1f, 1f)
        }
        reenterTransition = Fade(Fade.IN).apply {
            duration = 250L / 2
            startDelay = 250L / 2
            interpolator = PathInterpolatorCompat.create(0f, 0f, 0.2f, 1f)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        //加載保存的語言
        val selectedLanguage = loadLanguagePreference()
        mAttractionsViewModel.changeLanguage(selectedLanguage)
        getChangeLanguage(selectedLanguage)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setViewCompat(view)
        initObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initView() {
        binding.toolbar.title = getString(R.string.main_title)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        binding.data = mAttractionsViewModel
        binding.contentListRv.run {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
                ) {
                    outRect.apply {
                        val padding = SizeUtils.dip2px(requireContext(), 4.0f)
                        top = padding
                        left = padding
                        bottom = padding
                        right = padding
                    }
                }
            })
        }

        binding.contentListRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                val threshold = 2

                if (firstVisibleItemPosition < 1) binding.fabTop.visibility =
                    View.GONE else binding.fabTop.visibility = View.VISIBLE

                if (visibleItemCount + firstVisibleItemPosition + threshold >= totalItemCount) {
                    if (!mAttractionsViewModel.isLoading) {
                        mAttractionsViewModel.isLoading = true
                        mSnackbar.show()
                        mAttractionsViewModel.loadMoreData()
                    }
                }
            }
        })

        binding.fabTop.setOnClickListener {
            binding.contentListRv.smoothScrollToPosition(0)
        }

        binding.webview.apply {
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = true
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    progressDialog.show()
                }

                override fun onPageFinished(view: WebView, url: String) {
                    progressDialog.dismiss()
                    binding.webview.visibility = View.VISIBLE
                }
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!onBackPressed()) {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun setViewCompat(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view.parent as View) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navigationBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemGestures())
            binding.toolbar.updateLayoutParams<AppBarLayout.LayoutParams> {
                topMargin = systemBars.top
            }
            binding.fabTop.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = navigationBarInsets.bottom + 20
            }
            insets
        }
    }

    private fun initObservers() {
        mAttractionsViewModel.apply {
            contentList.observe(viewLifecycleOwner) {
                mAdapter.setData(it)
                isLoading = false
                mSnackbar.dismiss()
            }

            loadState.observe(viewLifecycleOwner) {
                hideAll()
                when (it) {
                    LoadState.SUCCESS -> binding.swipeRefreshLayout.visibility = View.VISIBLE
                    LoadState.LOADING -> binding.loadingView.visibility = View.VISIBLE
                    LoadState.EMPTY -> binding.emptyView.visibility = View.VISIBLE
                    LoadState.ERROR -> binding.errorView.visibility = View.VISIBLE
                    LoadState.NOMOREDATA -> {
                        binding.swipeRefreshLayout.visibility = View.VISIBLE
                        Snackbar.make(
                            binding.contentListRv,
                            getString(R.string.no_more_information),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }

            binding.errorView.setOnClickListener {
                loadMoreData()
            }

            // 設置下拉刷新監聽器
            binding.swipeRefreshLayout.setOnRefreshListener {
                loadContent()
                binding.swipeRefreshLayout.isRefreshing = false
            }

            if (contentList.value == null) {
                loadContent()
            }
        }
    }

    private fun hideAll() {
        binding.swipeRefreshLayout.visibility = View.GONE
        binding.loadingView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.fabTop.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_language_icon -> {
                showLanguageSelectionDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //彈出選擇語言的對話框
    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            "繁體中文" to "zh-TW", //繁體中文
            "简体中文" to "zh-CN", //簡體中文
            "English" to "en", //英文
            "にほんご" to "ja", //日文
            "한국인" to "ko", //韓文
            "español" to "es", //西班牙文
            "bahasa Indonesia" to "id", //印尼文
            "แบบไทย" to "th", //泰文
            "Tiếng Việt" to "vi" //越南文
        )

        val languageNames = languages.map { it.first }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_language))
            .setItems(languageNames) { _, which ->
                val selectedLanguageCode = languages[which].second
                getChangeLanguage(selectedLanguageCode)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun getChangeLanguage(languageCode: String) {
        //獲取當前的語言設置
        val currentLocale = requireContext().resources.configuration.locales[0].toLanguageTag()

        //檢查當前語言是否與選定的語言相同
        if (currentLocale != languageCode) {
            //創建新的 Configuration 對象並設置新的語言
            val locale = Locale.forLanguageTag(languageCode)
            Locale.setDefault(locale)
            val config = Configuration(requireContext().resources.configuration)
            config.setLocale(locale)

            //保存語言偏好
            saveLanguagePreference(languageCode)

            mAttractionsViewModel.changeLanguage(languageCode)

            //重新抓取新語言列表
            mAttractionsViewModel.loadContent()

            //更新資源和 Activity
            requireContext().resources.updateConfiguration(
                config,
                requireContext().resources.displayMetrics
            )
            requireActivity().recreate()
        }
    }

    fun saveLanguagePreference(languageCode: String) {
        val sharedPreferences =
            requireContext().getSharedPreferences("language_settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("selected_language", languageCode)
        editor.apply()
    }

    fun loadLanguagePreference(): String {
        val sharedPreferences =
            requireContext().getSharedPreferences("language_settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("selected_language", "zh-TW") ?: "zh-TW"
    }

    override fun onWebsiteButtonClick(url: String) {
        progressDialog.show()
        binding.webview.apply {
            loadUrl(url)
        }
    }

    fun onBackPressed(): Boolean {
        return if (binding.webview.isVisible) {
            binding.webview.visibility = View.GONE
            true
        } else {
            false
        }
    }
}

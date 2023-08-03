package com.example.attractions.model

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.attractions.Repository.AttractionsRepository
import com.example.attractions.base.LoadState
import com.example.attractions.domain.AttractionsDataItem
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AttractionsViewModel : ViewModel() {
    private val attractionsRepository by lazy {
        AttractionsRepository()
    }

    companion object {
        //默認初始頁數
        const val DEFAULT_PAGE = 1
    }

    //當前頁數
    private var mCurrentPage = DEFAULT_PAGE

    //當前頁面狀態
    var loadState = MutableLiveData<LoadState>()

    //是否底層加載頁面資料
    var isLoading = false

    //RecyclerView資料集
    val contentList = MutableLiveData<MutableList<AttractionsDataItem>>()

    /**
     * 加載更多內容
     */
    fun loadMoreData() {
        this.listContentByPage(mCurrentPage)
    }

    /**
     * 加載首頁內容
     */
    fun loadContent() {
        mCurrentPage = DEFAULT_PAGE
        loadState.value = LoadState.LOADING
        this.listContentByPage(mCurrentPage)
    }

    private fun listContentByPage(page: Int) {
        viewModelScope.launch {
            try {
                val attractionsDataList =
                    attractionsRepository.getAttractionsListData(
                        page,
                        languageChangeRequest.toLowerCase()
                    )
                if (mCurrentPage == DEFAULT_PAGE) {
                    contentList.value = attractionsDataList
                } else {
                    val oldValue = contentList.value ?: mutableListOf()
                    oldValue.addAll(attractionsDataList)
                    contentList.value = oldValue
                }
                if (attractionsDataList.isEmpty()) {
                    loadState.value = LoadState.EMPTY
                } else {
                    mCurrentPage++
                    loadState.value = LoadState.SUCCESS
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (e.message.equals("401")) {
                    //沒有更多數據的時候
                    loadState.value = LoadState.NOMOREDATA
                } else {
                    loadState.value = LoadState.ERROR
                }
                if (e is HttpException) {
                    val code = e.code()
                    if (code == 400) {
                        // 印尼語沒有資料
                        loadState.value = LoadState.EMPTY
                    }
                }
            }
        }
    }

    private var languageChangeRequest: String = ""

    fun changeLanguage(languageCode: String) {
        languageChangeRequest = "$languageCode/"
    }
}
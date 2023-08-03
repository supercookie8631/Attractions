package com.example.attractions.Repository

import com.example.attractions.api.RetrofitClient

class AttractionsRepository {
    suspend fun getAttractionsListData(page: Int, languageCode: String) =
        RetrofitClient(languageCode).apiService.getAttractionsList(page).apiData()
}
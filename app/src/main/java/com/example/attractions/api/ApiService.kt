package com.example.attractions.api

import com.example.attractions.domain.AttractionsData
import com.example.attractions.domain.ResultData
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiService {
    companion object {
        //台北open data
        const val BASE_URL = "https://www.travel.taipei/open-api/"
    }

    @GET("Attractions/All")
    @Headers("accept: application/json")
    suspend fun getAttractionsList(@Query("page") page: Int): ResultData<AttractionsData>
}
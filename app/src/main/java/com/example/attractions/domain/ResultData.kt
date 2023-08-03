package com.example.attractions.domain

import com.example.attractions.api.ApiException

data class ResultData<T>(
    val total: Int,
    val data: T
) {
    fun apiData(): T {
        if (total == 0) {
            //隨意定義"查無資料"的code為400
            throw ApiException("401")
        } else {
            return data
        }
    }
}

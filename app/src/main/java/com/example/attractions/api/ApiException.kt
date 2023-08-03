package com.example.attractions.api

data class ApiException(override val message: String?) : RuntimeException()

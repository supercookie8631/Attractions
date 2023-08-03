package com.example.attractions.domain

class AttractionsData : ArrayList<AttractionsDataItem>()

data class AttractionsDataItem(
    val address: String,
    val category: List<Category>,
    val distric: String,
    val elong: Double,
    val email: String,
    val facebook: String,
    val fax: String,
    val files: List<Any>,
    val friendly: List<Friendly>,
    val id: Int,
    val images: List<Image>,
    val introduction: String,
    val links: List<Link>,
    val modified: String,
    val months: String,
    val name: String,
    val name_zh: Any,
    val nlat: Double,
    val official_site: String,
    val open_status: Int,
    val open_time: String,
    val remind: String,
    val service: List<Service>,
    val staytime: String,
    val target: List<Target>,
    val tel: String,
    val ticket: String,
    val url: String,
    val zipcode: String
)

data class Category(
    val id: Int,
    val name: String
)

data class Friendly(
    val id: Int,
    val name: String
)

data class Image(
    val ext: String,
    val src: String,
    val subject: String
)

data class Link(
    val src: String,
    val subject: String
)

data class Service(
    val id: Int,
    val name: String
)

data class Target(
    val id: Int,
    val name: String
)
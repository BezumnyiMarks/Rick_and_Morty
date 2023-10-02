package com.example.rickandmorty.Data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Characters (
    @Json(name = "info")
    val info: Info? = null,

    @Json(name = "results")
    val results: List<PersonalData>? = null
)

@JsonClass(generateAdapter = true)
data class Info (
    @Json(name = "count")
    val count: Long? = null,

    @Json(name = "pages")
    val pages: Long? = null,

    @Json(name = "next")
    val next: String? = null,

    @Json(name = "prev")
    val prev: String? = null
)

@JsonClass(generateAdapter = true)
data class PersonalData (
    @Json(name = "id")
    val id: Long? = null,

    @Json(name = "name")
    val name: String? = null,

    @Json(name = "status")
    val status: String? = null,

    @Json(name = "species")
    val species: String? = null,

    @Json(name = "type")
    val type: String? = null,

    @Json(name = "gender")
    val gender: String? = null,

    @Json(name = "origin")
    val origin: Location? = null,

    @Json(name = "location")
    val location: Location? = null,

    @Json(name = "image")
    val image: String? = null,

    @Json(name = "episode")
    val episode: List<String>? = null,

    @Json(name = "url")
    val url: String? = null,

    @Json(name = "created")
    val created: String? = null
)

@JsonClass(generateAdapter = true)
data class Location (
    @Json(name = "name")
    val name: String? = null,

    @Json(name = "url")
    val url: String? = null
)

@JsonClass(generateAdapter = true)
data class Episode (
    @Json(name = "id")
    val id: Long? = null,

    @Json(name = "name")
    val name: String? = null,

    @Json(name = "air_date")
    val airDate: String? = null,

    @Json(name = "episode")
    val episode: String? = null,

    @Json(name = "characters")
    val characters: List<String>? = null,

    @Json(name = "url")
    val url: String? = null,

    @Json(name = "created")
    val created: String? = null
)

@JsonClass(generateAdapter = true)
data class Locations (
    @Json(name = "info")
    val info: Info? = null,

    @Json(name = "results")
    val results: List<LocationData>? = null
)

@JsonClass(generateAdapter = true)
data class LocationData (
    @Json(name = "id")
    val id: Long? = null,

    @Json(name = "name")
    val name: String? = null,

    @Json(name = "type")
    val type: String? = null,

    @Json(name = "dimension")
    val dimension: String? = null,

    @Json(name = "residents")
    val residents: List<String>? = null,

    @Json(name = "url")
    val url: String? = null,

    @Json(name = "created")
    val created: String? = null,
)

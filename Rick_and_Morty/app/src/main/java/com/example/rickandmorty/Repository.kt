package com.example.rickandmorty

import com.example.rickandmorty.Data.Characters
import com.example.rickandmorty.Data.Episode
import com.example.rickandmorty.Data.Locations
import com.example.rickandmorty.Data.PersonalData
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://rickandmortyapi.com/api/"

class Repository() {
     object RetrofitInstance{
          private val retrofit = Retrofit.Builder()
               .baseUrl(BASE_URL)
               .addConverterFactory(MoshiConverterFactory.create())
               .build()

          val searchCharacters = retrofit.create(SearchCharacters::class.java)
     }
}

interface SearchCharacters{
     @GET("character")
     suspend fun getCharacters(@Query("page") page: Int): Characters

     @GET("character/{id}")
     suspend fun getCharacterDetails(@Path("id") id: Int): PersonalData

     @GET("character/{idList}")
     suspend fun getPersonalDataList(@Path("idList") idList: List<Int>): List<PersonalData>

     @GET("episode/{idList}")
     suspend fun getEpisodesList(@Path("idList") idList: List<Int>): List<Episode>

     @GET("location")
     suspend fun getLocations(@Query("page") page: Int): Locations
}
package com.example.rickandmorty.PagingSources

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.rickandmorty.Data.LocationData
import com.example.rickandmorty.Repository

class LocationPagingSource(): PagingSource<Int, LocationData>() {

    override fun getRefreshKey(state: PagingState<Int, LocationData>): Int = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LocationData> {
        var maxPages = 0
        val page = params.key ?: 1

        return kotlin.runCatching {
            val responce = Repository.RetrofitInstance.searchCharacters.getLocations(page)
            maxPages = responce.info?.pages?.toInt() ?: 0
            responce.results
        }.fold(
            onSuccess = {
                LoadResult.Page(
                    data = it!!,
                    prevKey = null,
                    nextKey = if (page == maxPages) null else page + 1,
                )
            },
            onFailure = {
                Log.d("Locations", it.message.toString())
                LoadResult.Error(it)
            }
        )
    }

    companion object{
        fun getPager() = Pager(
            config = PagingConfig(20),
            pagingSourceFactory = { LocationPagingSource() }
        )
    }
}
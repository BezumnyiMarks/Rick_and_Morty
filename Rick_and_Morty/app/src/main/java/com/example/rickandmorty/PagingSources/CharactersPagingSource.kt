package com.example.rickandmorty.PagingSources

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.rickandmorty.Data.PersonalData
import com.example.rickandmorty.Repository

class CharactersPagingSource(): PagingSource<Int, PersonalData>() {

    override fun getRefreshKey(state: PagingState<Int, PersonalData>): Int = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PersonalData> {
        var maxPages = 0
        val page = params.key ?: 1

        return kotlin.runCatching {
            val responce = Repository.RetrofitInstance.searchCharacters.getCharacters(page)
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
                Log.d("Characters", it.message.toString())
                LoadResult.Error(it)
            }
        )
    }

    companion object{
        fun getPager() = Pager(
            config = PagingConfig(20),
            pagingSourceFactory = { CharactersPagingSource() }
        )
    }
}
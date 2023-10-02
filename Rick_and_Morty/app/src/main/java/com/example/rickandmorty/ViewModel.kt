package com.example.rickandmorty

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmorty.Data.Episode
import com.example.rickandmorty.Data.PersonalData
import com.example.rickandmorty.Data.PersonalDataStates
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ViewModel(application: Application) : AndroidViewModel(application) {
    val personalData = MutableStateFlow(PersonalData(null, null, null, null, null, null, null, null, null, null, null))
    val personalDataLoading = MutableStateFlow(true)
    val personalDataError = MutableStateFlow(false)

    val episodes = MutableStateFlow(listOf<Episode>())
    val episodesLoading = MutableStateFlow(true)
    val episodesError = MutableStateFlow(false)

    val personalDataStates = mutableListOf<PersonalDataStates>()

    val showGladValakas = MutableStateFlow(true)

    fun loadCharacterDetails(id: Int){
        personalDataLoading.value = true
        personalDataError.value = false
        viewModelScope.launch {
            kotlin.runCatching {
                Repository.RetrofitInstance.searchCharacters.getCharacterDetails(id)
            }.fold(
                onSuccess = {
                    personalData.value = it
                    personalDataLoading.value = false
                    personalDataError.value = false
                },
                onFailure = {
                    Log.d("LoadCharacterDetails", it.message ?:"")
                    personalDataLoading.value = false
                    personalDataError.value = true
                }
            )
        }
    }

    fun loadEpisodes(idList: List<Int>){
        episodesLoading.value = true
        episodesError.value = false
        viewModelScope.launch {
            val delayTime = (10..50).random() * 10
            delay(delayTime.toLong())
            kotlin.runCatching {
                Repository.RetrofitInstance.searchCharacters.getEpisodesList(idList)
            }.fold(
                onSuccess = {
                    episodes.value = it
                    episodesLoading.value = false
                    episodesError.value = false
                },
                onFailure = {
                    Log.d("LoadEpisodes", it.message ?:"")
                    episodesLoading.value = false
                    episodesError.value = true
                }
            )
        }
    }

    fun loadPersonalDataList(idList: List<Int>, locationID: Int){
        removeDataStates(personalDataStates, locationID)
        val dataStates = getPersonalDataStates(locationID)
        personalDataStates.add(dataStates)
        dataStates.personalDataListLoading.value = true
        dataStates.personalDataListError.value = false
        viewModelScope.launch {
            kotlin.runCatching {
                Repository.RetrofitInstance.searchCharacters.getPersonalDataList(idList)
            }.fold(
                onSuccess = {
                    dataStates.personalDataList.value = it
                    dataStates.personalDataListLoading.value = false
                    dataStates.personalDataListError.value = false
                },
                onFailure = {
                    Log.d("LoadEpisodes", it.message ?:"")
                    dataStates.personalDataListLoading.value = false
                    dataStates.personalDataListError.value = true
                }
            )
        }
    }

    companion object{
        fun getPersonalDataStates(locationID: Int) = PersonalDataStates(
            personalDataList = MutableStateFlow(listOf()),
            personalDataListLoading = MutableStateFlow(false),
            personalDataListError = MutableStateFlow(false),
            locationID = locationID
        )

        fun removeDataStates(personalDataStates: MutableList<PersonalDataStates>, locationID: Int){
            var dataStates = PersonalDataStates(MutableStateFlow(listOf()), MutableStateFlow(false), MutableStateFlow(false), 0)
            personalDataStates.forEach {
                if (it.locationID == locationID)
                    dataStates = it
            }
            personalDataStates.remove(dataStates)
        }
    }
}
package com.example.rickandmorty.Data

import com.example.rickandmorty.Data.PersonalData
import kotlinx.coroutines.flow.MutableStateFlow

data class PersonalDataStates(
    var personalDataList: MutableStateFlow<List<PersonalData>>,
    var personalDataListLoading: MutableStateFlow<Boolean>,
    var personalDataListError: MutableStateFlow<Boolean>,
    var locationID: Int
)

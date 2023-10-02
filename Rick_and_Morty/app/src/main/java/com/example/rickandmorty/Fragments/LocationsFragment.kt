package com.example.rickandmorty.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController


import androidx.paging.LoadState
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.rickandmorty.Data.LocationData
import com.example.rickandmorty.PagingSources.LocationPagingSource
import com.example.rickandmorty.Data.PersonalData
import com.example.rickandmorty.Data.PersonalDataStates
import com.example.rickandmorty.R
import com.example.rickandmorty.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private const val CHARACTER_ID = "CHARACTER_ID"
class LocationsFragment : Fragment() {
    private val viewModel: ViewModel by viewModels()
    private val locationsPagingData by lazy {
        LocationPagingSource.getPager().flow.cachedIn(
            lifecycleScope
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        view.setContent {
            ShowLocations()
        }
        return view
    }

    @Composable
    fun ShowLocations() {
        val locations: LazyPagingItems<LocationData> =
            locationsPagingData.collectAsLazyPagingItems()
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .background(color = colorResource(id = R.color.light_black))
        ) {
            HeaderPartDetailsView(header = "Locations", navController = findNavController())
            LazyColumn(
                modifier = Modifier
                    .background(color = colorResource(id = R.color.light_black))
                    .padding(8.dp)
            ) {
                itemsIndexed(
                    locations.itemSnapshotList
                ) { index, item ->
                    item?.let {
                        if (!locations.get(index)!!.residents.isNullOrEmpty())
                            viewModel.loadPersonalDataList(getCharactersIDList(it), it.id!!.toInt())
                        LocationView(locations.get(index)!!, lifecycleScope, viewModel, findNavController())
                    } ?: Text(text = "Ничерта нет!")
                }

                locations.apply {
                    when {
                        loadState.refresh is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        loadState.append is LoadState.Loading -> {
                            item {
                                CircularProgressIndicator()
                            }
                        }

                        loadState.refresh is LoadState.Error -> {
                            val e = locations.loadState.refresh as LoadState.Error
                            item {
                                Column(
                                    modifier = Modifier.fillParentMaxSize()
                                ) {
                                    e.error.localizedMessage?.let { Text(text = it) }
                                    Button(onClick = { retry() }) {
                                        Text(text = "Давай ещё разок!")
                                    }
                                }
                            }
                        }

                       loadState.append is LoadState.Error -> {
                           val e = locations.loadState.append as LoadState.Error
                           item {
                               Column(
                                   modifier = Modifier.fillParentMaxSize(),
                                   verticalArrangement = Arrangement.Center
                               ) {
                                   e.error.localizedMessage?.let { Text(text = it) }
                                   Button(onClick = { retry() }) {
                                       Text(text = "Давай ещё разок!")
                                   }
                               }
                           }
                       }
                    }
                }
            }
        }
    }
}

@SuppressLint("SuspiciousIndentation", "CoroutineCreationDuringComposition")
@Composable
fun LocationView(location: LocationData, lifecycleScope: LifecycleCoroutineScope, viewModel: ViewModel, navController: NavController){
    val locationIDList = mutableListOf<Int>()
    viewModel.personalDataStates.forEach {
        locationIDList.add(it.locationID)
    }
    if (!locationIDList.contains(location.id!!.toInt()))
        viewModel.personalDataStates.add(ViewModel.getPersonalDataStates(location.id.toInt()))

    var dataStates = PersonalDataStates(MutableStateFlow(listOf()), MutableStateFlow(false), MutableStateFlow(false), 0)
    viewModel.personalDataStates.forEach {
        if (it.locationID == location.id.toInt())
            dataStates = it
    }
    val personalDataList = dataStates.personalDataList.collectAsState()
    val personalDataListLoading = dataStates.personalDataListLoading.collectAsState()
    val personalDataListError = dataStates.personalDataListError.collectAsState()

    if (personalDataList.value.isEmpty() && !location.residents.isNullOrEmpty() && !personalDataListLoading.value)
        viewModel.loadPersonalDataList(getCharactersIDList(location), location.id.toInt())

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(8.dp)
            .background(color = colorResource(id = R.color.light_black))
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .background(color = colorResource(id = R.color.dark_gray))
        ) {
            Text(
                text = "Location:",
                fontSize = 16.sp,
                color = colorResource(id = R.color.gray),
                modifier = Modifier.padding(top = 2.dp, start = 8.dp, bottom = 2.dp)
            )
            Text(
                text = "${location.name} - ${location.type}",
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 16.dp),
                color = colorResource(id = R.color.white)
            )
            Text(
                text = "Dimension:",
                fontSize = 16.sp,
                color = colorResource(id = R.color.gray),
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
            Text(
                text = location.dimension ?: "",
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 16.dp),
                color = colorResource(id = R.color.white)
            )
            Text(
                text = "Residents:",
                fontSize = 16.sp,
                color = colorResource(id = R.color.gray),
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )

            if (personalDataListError.value)
                Button(onClick = { viewModel.loadPersonalDataList(getCharactersIDList(location), location.id.toInt()) }) {
                    Text(text = "Давай ещё разок!")
                }

            if (personalDataListLoading.value)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            else if (!location.residents.isNullOrEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                ) {
                    lifecycleScope.launch {
                        //delay(200)
                        items(personalDataList.value) {
                            ResidentView(
                                personalData = it,
                                lifecycleScope = lifecycleScope,
                                viewModel = viewModel,
                                navController = navController
                            )
                        }
                    }
                }
            }
            else
                Text(
                    text = "Nobody lives here",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                    color = colorResource(id = R.color.white)
                )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ResidentView(personalData: PersonalData, lifecycleScope: LifecycleCoroutineScope, viewModel: ViewModel, navController: NavController){
    Surface(
        modifier = Modifier.padding(end = 8.dp),
        onClick = {
            val bundle = bundleOf(CHARACTER_ID to (personalData.id?.toInt() ?: 0))
            navController.navigate(R.id.personDetailsFragment, bundle)
        }
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .width(100.dp)
                .background(color = colorResource(id = R.color.dark_gray)),
        ) {
            SetImage(
                image = personalData.image,
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(10.dp),
                contentScale = ContentScale.Crop,
                lifecycleScope = lifecycleScope,
                viewModel = viewModel
            )
            Text(
                text = personalData.name ?: "",
                fontSize = 16.sp,
                color = colorResource(id = R.color.white),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun getCharactersIDList(locationData: LocationData): List<Int>{
    val idList = mutableListOf<Int>()
    locationData.residents?.forEach {
        val list = it.split("/")
        idList.add(list[list.lastIndex].toInt())
    }
    return idList
}
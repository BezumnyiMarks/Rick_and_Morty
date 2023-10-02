package com.example.rickandmorty.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.rickandmorty.PagingSources.CharactersPagingSource
import com.example.rickandmorty.Data.PersonalData
import com.example.rickandmorty.R
import com.example.rickandmorty.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CHARACTER_ID = "CHARACTER_ID"
class MainFragment : Fragment() {
    private val viewModel: ViewModel by viewModels()
    private val charactersPagingData by lazy {
        CharactersPagingSource.getPager().flow.cachedIn(
            lifecycleScope
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        view.setContent {
            ShowCharacters()
        }
        return view
    }

    @Composable
    fun ShowCharacters() {
        val characters: LazyPagingItems<PersonalData> =
            charactersPagingData.collectAsLazyPagingItems()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(color = colorResource(id = R.color.light_black))
        ) {
            Button(
                onClick = {
                    findNavController().navigate(R.id.action_mainFragment_to_locationsFragment)
                },
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            ) {
                Text(text = "Доступные локации")
            }

            LazyColumn(
                modifier = Modifier
                    .background(color = colorResource(id = R.color.light_black))
                    .padding(8.dp)
            ) {
                itemsIndexed(
                    characters.itemSnapshotList
                ) {index, item ->
                    item?.let {
                        CharacterView(
                            personalData = characters.get(index)!!,
                            navController = findNavController(),
                            lifecycleScope = lifecycleScope,
                            viewModel = viewModel
                        )
                    } ?: Text(text = "Ничерта нет!")
                }

                characters.apply {
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
                            val e = characters.loadState.refresh as LoadState.Error
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
                            val e = characters.loadState.append as LoadState.Error
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
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CharacterView(
    personalData: PersonalData,
    navController: NavController,
    lifecycleScope: LifecycleCoroutineScope,
    viewModel: ViewModel
){
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(
            top = 8.dp,
            bottom = 12.dp,
            start = 8.dp,
            end = 8.dp
        ),
        onClick = {
            val bundle = bundleOf(CHARACTER_ID to (personalData.id?.toInt() ?: 0))
            navController.navigate(R.id.personDetailsFragment, bundle)
        }
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .background(color = colorResource(id = R.color.dark_gray))
                .fillMaxSize()
        ) {
            SetImage(
                image = personalData.image,
                modifier = Modifier.size(150.dp),
                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp),
                contentScale = ContentScale.Fit,
                lifecycleScope = lifecycleScope,
                viewModel = viewModel
            )
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)
            ) {
                Text(
                    text = personalData.name ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = colorResource(id = R.color.white)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    StatusMark(personalData)
                    Text(
                        text = "${personalData.status} - ${personalData.species}",
                        color = colorResource(id = R.color.white)
                    )
                }
                Text(
                    text = "Last known location:",
                    color = colorResource(id = R.color.light_gray),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = personalData.location?.name ?: "",
                    color = colorResource(id = R.color.white)
                )
            }
        }
    }
}

@Composable
fun StatusMark(personalData: PersonalData){
    Card(
        shape = RoundedCornerShape(1000.dp),
        modifier = Modifier.padding(end = 6.dp)
    ) {
        Spacer(
            Modifier
                .background(
                    color = when (personalData.status) {
                        "Alive" -> colorResource(id = R.color.green)
                        "Dead" -> colorResource(id = R.color.red)
                        else -> colorResource(id = R.color.yellow)
                    }
                )
                .size(8.dp)
        )
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SetImage(
    image: Any?,
    modifier: Modifier,
    shape: RoundedCornerShape,
    contentScale: ContentScale,
    lifecycleScope: LifecycleCoroutineScope,
    viewModel: ViewModel
){
    val showGladValakas = viewModel.showGladValakas.collectAsState()
    var imageData: Any? = null

    if (showGladValakas.value){
        lifecycleScope.launch {
            delay(2000)
            viewModel.showGladValakas.value = false
        }
    }
    else imageData = image

    Card(
        shape = shape,
        modifier = Modifier.background(colorResource(id = R.color.dark_gray))
    ) {
        if (imageData == null)
            Image(
                painter = painterResource(id = R.drawable.glad),
                contentDescription = null,
                modifier = modifier,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit
            )
        else
            GlideImage(
                model = imageData,
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale
            )
    }
}

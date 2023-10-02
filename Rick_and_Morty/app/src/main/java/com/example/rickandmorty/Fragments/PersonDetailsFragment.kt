package com.example.rickandmorty.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.rickandmorty.Data.Episode
import com.example.rickandmorty.Data.PersonalData
import com.example.rickandmorty.R
import com.example.rickandmorty.ViewModel

private const val CHARACTER_ID = "CHARACTER_ID"
class PersonDetailsFragment : Fragment() {
    private val viewModel: ViewModel by viewModels()

    private var characterID: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            characterID = it.getInt(CHARACTER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        view.setContent {
            viewModel.loadCharacterDetails(characterID!!)
            PersonDetailsView()
        }
        return view
    }

    @Composable
    fun PersonDetailsView(){
        val personalData = viewModel.personalData.collectAsState()
        val personalDataLoading = viewModel.personalDataLoading.collectAsState()
        val personalDataError = viewModel.personalDataError.collectAsState()

        val episodes = viewModel.episodes.collectAsState()
        val episodesLoading = viewModel.episodesLoading.collectAsState()
        val episodesError = viewModel.episodesError.collectAsState()

        if (personalData.value.id != null && episodes.value.isEmpty()){
            viewModel.loadEpisodes(getEpisodesIDList(personalData.value))
        }

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .background(color = colorResource(id = R.color.light_black))
                .fillMaxSize()
        ) {
            HeaderPartDetailsView(header = "Person details", navController = findNavController())
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ){
                if (personalDataError.value)
                    Button(onClick = { viewModel.loadCharacterDetails(characterID!!.toInt()) }) {
                        Text(text = "Давай ещё разок!")
                    }

                if (episodesError.value && personalData.value.id != null){
                    Button(onClick = { viewModel.loadEpisodes(getEpisodesIDList(personalData.value)) }) {
                        Text(text = "Давай ещё разок!")
                    }
                }

                if (personalDataLoading.value || episodesLoading.value)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                else{
                    SetImage(
                        image = personalData.value.image,
                        modifier = Modifier
                            .height(220.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        contentScale = ContentScale.FillWidth,
                        lifecycleScope = lifecycleScope,
                        viewModel = viewModel
                    )
                    TextPartDetailsView(personalData = personalData.value, episodes = episodes.value)
                }
            }
        }
    }

    private fun getEpisodesIDList(personalData: PersonalData): List<Int>{
        val idList = mutableListOf<Int>()
        personalData.episode?.forEach {
            val list = it.split("/")
            idList.add(list[list.lastIndex].toInt())
        }
        return idList
    }
}

@Composable
fun HeaderPartDetailsView(header: String, navController: NavController){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 8.dp, top = 8.dp, bottom = 16.dp)
    ) {
        IconButton(onClick = {navController.navigateUp()}, modifier = Modifier.padding(end = 60.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.icon_back),
                contentDescription = null,
                tint = colorResource(id = R.color.white)
            )
        }
        Text(
            text = header,
            fontSize = 24.sp,
            color = colorResource(id = R.color.white)
        )
    }
}

@Composable
fun TextPartDetailsView(personalData: PersonalData, episodes: List<Episode>){
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = personalData.name ?: "",
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 16.dp),
            color = colorResource(id = R.color.white)
        )
        Text(
            text = "Live status",
            fontSize = 16.sp,
            color = colorResource(id = R.color.gray),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            StatusMark(personalData = personalData)
            Text(
                text = personalData.status ?: "",
                fontSize = 20.sp,
                color = colorResource(id = R.color.white),
            )
        }
        TextData(header = "Species and gender:", "${personalData.species}(${personalData.gender})")
        TextData(header = "Last known location:", personalData.location?.name)
        TextData(header = "First seen in:", dataStr = episodes[0].name)
        Text(
            text = "Episodes",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp),
            color = colorResource(id = R.color.white)
        )
    }
    episodes.forEach {
        EpisodeView(episode = it)
    }
}

@Composable
fun EpisodeView(episode: Episode){
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .background(color = colorResource(id = R.color.dark_gray))
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(start = 20.dp, top = 8.dp, bottom = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = episode.name ?: "",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .width(200.dp),
                    color = colorResource(id = R.color.white)
                )
                Text(
                    text = episode.airDate ?: "",
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.white),
                )
            }
            Text(
                text = episode.episode ?: "",
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 8.dp, top = 8.dp),
                color = colorResource(id = R.color.gray)
            )
        }
    }
}

@Composable
fun TextData(header: String, dataStr: String?){
    Text(
        text = header,
        fontSize = 16.sp,
        color = colorResource(id = R.color.gray),
        modifier = Modifier.padding(bottom = 2.dp)
    )
    Text(
        text = dataStr ?: "",
        fontSize = 20.sp,
        modifier = Modifier.padding(bottom = 16.dp),
        color = colorResource(id = R.color.white)
    )
}

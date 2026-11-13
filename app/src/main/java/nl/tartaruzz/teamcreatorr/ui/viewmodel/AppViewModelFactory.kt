package nl.tartaruzz.teamcreatorr.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import nl.tartaruzz.teamcreatorr.TeamCreatorrApp
import nl.tartaruzz.teamcreatorr.data.Repository

object AppViewModelFactory {
    fun factory(app: Application): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = (app as TeamCreatorrApp).repository
                return when {
                    modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                        HomeViewModel(repo) as T
                    modelClass.isAssignableFrom(PlayerListViewModel::class.java) ->
                        PlayerListViewModel(repo) as T
                    modelClass.isAssignableFrom(PlayerProfileViewModel::class.java) ->
                        PlayerProfileViewModel(repo) as T
                    modelClass.isAssignableFrom(NewMatchViewModel::class.java) ->
                        NewMatchViewModel(repo) as T
                    modelClass.isAssignableFrom(MatchDetailViewModel::class.java) ->
                        MatchDetailViewModel(repo) as T
                    modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                        HistoryViewModel(repo) as T
                    modelClass.isAssignableFrom(RankingViewModel::class.java) ->
                        RankingViewModel(repo) as T
                    modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                        SettingsViewModel(repo) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
                }
            }
        }
}

@Composable
inline fun <reified VM : ViewModel> appViewModel(): VM {
    val app = LocalContext.current.applicationContext as TeamCreatorrApp
    return viewModel(factory = AppViewModelFactory.factory(app))
}

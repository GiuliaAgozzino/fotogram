package view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import repository.ApiRepository
import view.locationDialog.MapScreen
import viewModel.*
import viewModel.AppScreen.Feed

@Composable
fun MainScreen(
    currentUserId: Int,
    sessionId: String,
    apiRepository: ApiRepository
) {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val currentScreen by mainScreenViewModel.currentScreen

    BackHandler(enabled = currentScreen !is AppScreen.Feed) {
        mainScreenViewModel.navigateTo(AppScreen.Feed)
    }

    // ViewModel persistenti
    val feedViewModel: FeedViewModel = viewModel(
        factory = FeedViewModelFactory(sessionId, apiRepository)
    )
    val createPostViewModel: CreatePostViewModel = viewModel(
        factory = CreatePostViewModelFactory(sessionId, apiRepository)
    )
    val myUserProfileViewModel: MyUserProfileViewModel = viewModel(
        factory = MyUserProfileViewModelFactory(currentUserId, sessionId, apiRepository)
    )

    Scaffold(
        bottomBar = {
            if (mainScreenViewModel.shouldShowBottomBar()) {
                NavBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> mainScreenViewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->

        when (val screen = currentScreen) {

            is AppScreen.Feed -> {
                FeedScreen(
                    modifier = Modifier.padding(innerPadding),
                    feedViewModel = feedViewModel,
                    currentUserId = currentUserId,
                    onNavigateToProfile = { userId ->
                        if (userId == currentUserId) {
                            mainScreenViewModel.navigateTo(AppScreen.MyProfile)
                        } else {
                            mainScreenViewModel.navigateTo(AppScreen.UserProfile(userId))
                        }
                    }
                )
            }

            is AppScreen.CreatePost -> {
                CreatePostScreen(
                    createPostViewModel = createPostViewModel,
                    modifier = Modifier.padding(innerPadding),
                    onBackToFeed = { mainScreenViewModel.navigateTo(AppScreen.Feed) },
                    onPostCreated = {
                        myUserProfileViewModel.refresh()
                        feedViewModel.refresh()
                    }
                )
            }

            is AppScreen.MyProfile -> {
                MyUserProfileScreen(
                    modifier = Modifier.padding(innerPadding),
                    userProfileViewModel = myUserProfileViewModel
                )
            }

            is AppScreen.UserProfile -> {
                val userProfileViewModel: UserProfileViewModel = viewModel(
                    key = "user_${screen.userId}",
                    factory = UserProfileViewModelFactory(
                        targetUserId = screen.userId,
                        sessionId = sessionId,
                        currentUserId = currentUserId,
                        apiRepository = apiRepository
                    )
                )

                UserProfileScreen(
                    modifier = Modifier.padding(innerPadding),
                    userProfileViewModel = userProfileViewModel,
                    onBackClick = {
                        if (userProfileViewModel.followChanged) {
                            feedViewModel.refresh()
                            myUserProfileViewModel.refresh()
                            userProfileViewModel.resetFollowChanged()
                        }
                        mainScreenViewModel.navigateTo(AppScreen.Feed)
                    },
                    onCurrentUserChanged = {
                        myUserProfileViewModel.refresh()
                    }

                )
            }
            //Screen per aprire una mappa, in una nuova schemarta, a cui passi un point generico
            is AppScreen.Location -> {
                MapScreen(
                    targetLocation = screen.point,
                    targetLabel = screen.label,
                    markerLabel = screen.markerLabel,
                    maxDistanceKm = screen.maxDistanceKm,
                    showDistanceInfo = screen.showDistanceInfo,
                    onDismiss = { mainScreenViewModel.navigateTo(Feed) }
                )
            }

            //esempio di utilizzo generico
            /*
            data class LocationData(
                 val latitude: Double,
                 val longitude: Double,
                 val label: String? = null
            )

            ricodo di controllare  if (post.location?.latitude != null && post.location.longitude != null)
            prima di usare LocationData
             onClick = {
                  if (generic.location?.latitude != null && generic.location.longitude != null) {
                     val locationData = LocationData(
                         latitude = generic.location.latitude,
                         longitude = generic.location.longitude,
                         label = "Mario Rossi"
                       )
              onLocationClick(locationData)
            }
         }

           onLocationClick = { locationData ->
            mainScreenViewModel.navigateTo(
                AppScreen.Location(
                    point = Point.fromLngLat(
                        locationData.longitude,
                        locationData.latitude
                    ),
                    label = "Posizione  ${locationData.label ?: ""}",
                    markerLabel = locationData.label,
                    maxDistanceKm = 5.0,
                    showDistanceInfo = true
                )
            )
        }
             */
        }
    }
}
package view

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import viewModel.MainTab


@Composable
fun NavBar(
    currentTab: MainTab,
    onNavigate: (MainTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentTab == MainTab.FEED,
            onClick = { onNavigate(MainTab.FEED) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Feed") },
            label = { Text("Feed") }
        )
        NavigationBarItem(
            selected = currentTab == MainTab.CREATEPOST,
            onClick = { onNavigate(MainTab.CREATEPOST) },
            icon = { Icon(Icons.Filled.Add, contentDescription = "Create") },
            label = { Text("Create") }
        )
        NavigationBarItem(
            selected = currentTab == MainTab.USERPROFILE,
            onClick = { onNavigate(MainTab.USERPROFILE) },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}


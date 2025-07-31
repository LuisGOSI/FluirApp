package com.example.fluirapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fluirapp.R
import com.example.fluirapp.screens.FluirSplashScreen
import com.example.fluirapp.screens.home.FluirHomeScreen
import com.example.fluirapp.screens.login.FluirLoginScreen
import com.example.fluirapp.screens.tank.TankDetailScreen

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun FluirNavigation(){
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            // Solo mostrar la barra de navegación en ciertas pantallas
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute in listOf(
                    FluirScreens.HomeScreen.name,
                    FluirScreens.StatsScreen.name,
                    FluirScreens.HistoryScreen.name,
                    FluirScreens.SettingsScreen.name
                )) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = FluirScreens.SplashScreen.name,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(FluirScreens.SplashScreen.name){
                FluirSplashScreen(navController = navController)
            }
            composable(FluirScreens.LoginScreen.name){
                FluirLoginScreen(navController = navController)
            }
            composable(FluirScreens.HomeScreen.name){
                FluirHomeScreen(navController = navController)
            }
            composable(FluirScreens.StatsScreen.name) {
                // Aquí puedes agregar la pantalla de estadísticas
                Text(text = "Estadísticas", modifier = Modifier.padding(16.dp))
            }
            composable(FluirScreens.HistoryScreen.name) {
                // Aquí puedes agregar la pantalla de historial
                Text(text = "Historial", modifier = Modifier.padding(16.dp))
            }
            composable(FluirScreens.SettingsScreen.name) {
                // Aquí puedes agregar la pantalla de configuración
                Text(text = "Configuración", modifier = Modifier.padding(16.dp))
            }
            composable("tank_detail/{tankId}") { backStackEntry ->
                val tankId = backStackEntry.arguments?.getString("tankId") ?: ""
                TankDetailScreen(navController = navController, tankId = tankId)
            }
        }
    }
}

data class BottomNavItem(
    val title: String,
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val route: String,
    val badgeCount: Int? = null
)

@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
        BottomNavItem(
            title = "Inicio",
            selectedIcon = R.drawable.baseline_home_filled_24,
            unselectedIcon = R.drawable.outline_home_24,
            route = FluirScreens.HomeScreen.name
        ),
        BottomNavItem(
            title = "Estadísticas",
            selectedIcon = R.drawable.outline_bar_chart_blue,
            unselectedIcon = R.drawable.outline_bar_chart_24,
            route = FluirScreens.StatsScreen.name
        ),
        BottomNavItem(
            title = "Historial",
            selectedIcon = R.drawable.outline_history_blue,
            unselectedIcon = R.drawable.outline_history_24,
            route = FluirScreens.HistoryScreen.name
        ),
        BottomNavItem(
            title = "Configuración",
            selectedIcon = R.drawable.baseline_settings_24,
            unselectedIcon = R.drawable.outline_settings_24,
            route = FluirScreens.SettingsScreen.name
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8ECFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationItems.forEach { item ->
                val isSelected = currentRoute == item.route

                BottomNavItemView(
                    item = item,
                    isSelected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                // Evitar múltiples copias de la misma pantalla
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Evitar múltiples copias de la misma pantalla
                                launchSingleTop = true
                                // Restaurar estado cuando se regrese a la pantalla
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF64B5F6) else Color.Black.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 200),
        label = "color"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF2075C6).copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "background"
    )

    Column(
        modifier = Modifier
            .scale(animatedScale)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            Icon(
                painter = painterResource(
                    id = if (isSelected) item.selectedIcon else item.unselectedIcon
                ),
                contentDescription = item.title,
                tint = animatedColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.title,
            fontSize = 12.sp,
            color = animatedColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
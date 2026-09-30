package com.example.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.di.AppContainer
import com.example.presentation.screens.*
import com.example.presentation.viewmodel.*

@Composable
fun SomiGoApp(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val homeViewModel = remember { HomeViewModel(appContainer.foodRepository) }
    val cartViewModel = remember { CartViewModel(appContainer.cartRepository, appContainer.orderRepository) }
    val orderViewModel = remember { OrderViewModel(appContainer.orderRepository) }
    val adminViewModel = remember { AdminViewModel(appContainer.adminRepository) }
    val supportViewModel = remember { SupportViewModel() }
    val authViewModel = remember { AuthViewModel() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Orders.route,
        Screen.Favorites.route,
        Screen.Profile.route
    )

    val cartItems by cartViewModel.cartItems.collectAsStateWithLifecycle()
    val totalCartCount = cartItems.sumOf { it.quantity }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar && (totalCartCount > 0 || cartViewModel.isGroupOrder.value) && currentRoute != Screen.Cart.route) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.Cart.route) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                    text = { Text(if (cartViewModel.isGroupOrder.value) "Group Order • View Cart" else "$totalCartCount Items • View Cart") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(bottom = 60.dp)
                        .testTag("floating_cart_fab")
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToRestaurant = { id ->
                        navController.navigate(Screen.RestaurantDetail.createRoute(id))
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = homeViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRestaurant = { id ->
                        navController.navigate(Screen.RestaurantDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Orders.route) {
                OrdersHistoryScreen(
                    viewModel = orderViewModel,
                    cartViewModel = cartViewModel,
                    supportViewModel = supportViewModel,
                    onTrackOrder = { order ->
                        orderViewModel.setActiveOrder(order)
                        navController.navigate(Screen.OrderTracking.route)
                    },
                    onNavigateToCart = {
                        navController.navigate(Screen.Cart.route)
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = homeViewModel,
                    foodRepository = appContainer.foodRepository,
                    onNavigateToRestaurant = { id ->
                        navController.navigate(Screen.RestaurantDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Auth.route) {
                AuthScreen(
                    viewModel = authViewModel,
                    onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    supportViewModel = supportViewModel,
                    onNavigateToOrders = {
                        navController.navigate(Screen.Orders.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToFavorites = {
                        navController.navigate(Screen.Favorites.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Auth.route)
                    }
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    viewModel = cartViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onOrderPlaced = { placedOrder ->
                        orderViewModel.setActiveOrder(placedOrder)
                        navController.navigate(Screen.OrderTracking.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(Screen.OrderTracking.route) {
                OrderTrackingScreen(
                    viewModel = orderViewModel,
                    onNavigateBack = {
                        navController.navigate(Screen.Orders.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    viewModel = adminViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.RestaurantDetail.route,
                arguments = listOf(navArgument("restaurantId") { type = NavType.IntType })
            ) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getInt("restaurantId") ?: 1
                RestaurantDetailScreen(
                    restaurantId = restaurantId,
                    foodRepository = appContainer.foodRepository,
                    cartViewModel = cartViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) }
                )
            }
        }
    }
}

// Backward compatibility alias
@Composable
fun BiteDashApp(appContainer: AppContainer, navController: NavHostController = rememberNavController()) =
    SomiGoApp(appContainer, navController)

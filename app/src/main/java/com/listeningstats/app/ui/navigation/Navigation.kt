package com.listeningstats.app.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object TopItems : Screen("top_items")
    data object RecentTracks : Screen("recent_tracks")
    data object Activity : Screen("activity")
    data object ShareCards : Screen("share_cards")
    data object Settings : Screen("settings")
}

package com.akshar.dyslexiareader.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.akshar.dyslexiareader.domain.repository.WordMasteryRepository
import com.akshar.dyslexiareader.ui.auth.AuthScreen
import com.akshar.dyslexiareader.ui.auth.AuthViewModel
import com.akshar.dyslexiareader.ui.camera.CameraCaptureScreen
import com.akshar.dyslexiareader.ui.camera.CameraViewModel
import com.akshar.dyslexiareader.ui.camera.ImageCropScreen
import com.akshar.dyslexiareader.ui.consent.ParentalConsentScreen
import com.akshar.dyslexiareader.ui.library.LibraryScreen
import com.akshar.dyslexiareader.ui.library.LibraryViewModel
import com.akshar.dyslexiareader.ui.practice.PracticeModeScreen
import com.akshar.dyslexiareader.ui.practice.PracticeViewModel
import com.akshar.dyslexiareader.ui.practice.WordMasteryScreen
import com.akshar.dyslexiareader.ui.profiles.ProfileSelectionScreen
import com.akshar.dyslexiareader.ui.profiles.ProfilesViewModel
import com.akshar.dyslexiareader.ui.reader.ReaderScreen
import com.akshar.dyslexiareader.ui.reader.ReaderViewModel
import com.akshar.dyslexiareader.ui.reader.TextToSpeechManager
import com.akshar.dyslexiareader.ui.onboarding.FirstRunPersonalizationScreen
import com.akshar.dyslexiareader.ui.settings.PrivacyDataScreen
import com.akshar.dyslexiareader.ui.stats.ParentTeacherStatsScreen
import com.akshar.dyslexiareader.ui.stats.StatsViewModel

object NavDestinations {
    const val CONSENT = "consent"
    const val ONBOARDING = "onboarding"
    const val AUTH = "auth"
    const val PROFILES = "profiles"
    const val LIBRARY = "library"
    const val CAMERA = "camera"
    const val CROP = "crop"
    const val READER = "reader/{docId}"
    const val PRACTICE = "practice/{docId}?word={word}"
    const val MASTERY = "mastery/{profileId}"
    const val STATS = "stats?profileId={profileId}"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    profilesViewModel: ProfilesViewModel = hiltViewModel(),
    cameraViewModel: CameraViewModel = hiltViewModel(),
    readerViewModel: ReaderViewModel = hiltViewModel(),
    practiceViewModel: PracticeViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    statsViewModel: StatsViewModel = hiltViewModel(),
    wordMasteryRepository: WordMasteryRepository,
    ttsManager: TextToSpeechManager
) {
    val userSettings by authViewModel.userSettings.collectAsState()

    val startDestination = when {
        !userSettings.parentalConsentGiven -> NavDestinations.CONSENT
        !userSettings.firstRunCompleted -> NavDestinations.ONBOARDING
        userSettings.parentAccountId == null -> NavDestinations.AUTH
        userSettings.activeProfileId == null -> NavDestinations.PROFILES
        else -> NavDestinations.LIBRARY
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1. Parental Consent Screen (DPDP Act 2023)
        composable(NavDestinations.CONSENT) {
            ParentalConsentScreen(
                onConsentAccepted = {
                    authViewModel.acceptParentalConsent()
                    navController.navigate(NavDestinations.ONBOARDING) {
                        popUpTo(NavDestinations.CONSENT) { inclusive = true }
                    }
                }
            )
        }

        // 1b. First-Run Personalization & 3-Screen Tutorial
        composable(NavDestinations.ONBOARDING) {
            FirstRunPersonalizationScreen(
                onComplete = { theme, fontSize, lineSpacing, letterSpacing, fontFamily, rulerEnabled ->
                    authViewModel.completeFirstRunPersonalization(
                        theme = theme,
                        fontSize = fontSize,
                        lineSpacing = lineSpacing,
                        letterSpacing = letterSpacing,
                        fontFamily = fontFamily,
                        rulerEnabled = rulerEnabled
                    )
                    navController.navigate(NavDestinations.AUTH) {
                        popUpTo(NavDestinations.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // 2. Auth Screen
        composable(NavDestinations.AUTH) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(NavDestinations.PROFILES) {
                        popUpTo(NavDestinations.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // 3. Child Profiles & PIN Lock
        composable(NavDestinations.PROFILES) {
            ProfileSelectionScreen(
                viewModel = profilesViewModel,
                onProfileSelected = { _ ->
                    navController.navigate(NavDestinations.LIBRARY) {
                        popUpTo(NavDestinations.PROFILES) { inclusive = true }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(NavDestinations.SETTINGS)
                },
                onNavigateToParentStats = {
                    navController.navigate("stats")
                }
            )
        }

        // 4. Library Screen
        composable(NavDestinations.LIBRARY) {
            LibraryScreen(
                viewModel = libraryViewModel,
                onDocumentSelected = { docId ->
                    navController.navigate("reader/$docId")
                },
                onNavigateToScan = {
                    cameraViewModel.resetState()
                    navController.navigate(NavDestinations.CAMERA)
                },
                onNavigateToProfiles = {
                    navController.navigate(NavDestinations.PROFILES)
                },
                onNavigateToStats = {
                    val activeId = userSettings.activeProfileId ?: ""
                    if (activeId.isNotBlank()) {
                        navController.navigate("stats?profileId=$activeId")
                    } else {
                        navController.navigate("stats")
                    }
                }
            )
        }

        // 5. Camera Scan Screen
        composable(NavDestinations.CAMERA) {
            CameraCaptureScreen(
                viewModel = cameraViewModel,
                onImageReadyForCrop = {
                    navController.navigate(NavDestinations.CROP)
                }
            )
        }

        // 6. Image Crop & Enhance Screen
        composable(NavDestinations.CROP) {
            ImageCropScreen(
                viewModel = cameraViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOcrCompleted = { docId ->
                    navController.navigate("reader/$docId") {
                        popUpTo(NavDestinations.CAMERA) { inclusive = true }
                    }
                }
            )
        }

        // 7. Dyslexia Reader Screen
        composable(
            route = NavDestinations.READER,
            arguments = listOf(navArgument("docId") { type = NavType.StringType })
        ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            ReaderScreen(
                documentId = docId,
                viewModel = readerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPractice = { dId, targetWord ->
                    val id = dId.ifBlank { docId }
                    if (!targetWord.isNullOrBlank()) {
                        navController.navigate("practice/$id?word=$targetWord")
                    } else {
                        navController.navigate("practice/$id")
                    }
                }
            )
        }

        // 8. Practice Mode ("Read-Along")
        composable(
            route = NavDestinations.PRACTICE,
            arguments = listOf(
                navArgument("docId") { type = NavType.StringType },
                navArgument("word") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            val word = backStackEntry.arguments?.getString("word")
            val activeProfile = userSettings.activeProfileId?.ifBlank { "default" } ?: "default"

            PracticeModeScreen(
                documentId = docId,
                targetWord = word,
                viewModel = practiceViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWordMastery = {
                    navController.navigate("mastery/$activeProfile")
                }
            )
        }

        // 9. Word Mastery Bank
        composable(
            route = NavDestinations.MASTERY,
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
            WordMasteryScreen(
                profileId = profileId,
                wordMasteryRepository = wordMasteryRepository,
                ttsManager = ttsManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 10. Parent/Teacher Stats & Progress Screen
        composable(
            route = NavDestinations.STATS,
            arguments = listOf(
                navArgument("profileId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId")
            ParentTeacherStatsScreen(
                profileId = profileId,
                viewModel = statsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 11. Settings & DPDP Privacy Data Screen
        composable(NavDestinations.SETTINGS) {
            PrivacyDataScreen(
                authViewModel = authViewModel,
                profilesViewModel = profilesViewModel,
                onNavigateBack = { navController.popBackStack() },
                onAccountDeleted = {
                    navController.navigate(NavDestinations.CONSENT) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

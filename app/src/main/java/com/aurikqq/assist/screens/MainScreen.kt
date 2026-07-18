package com.aurikqq.assist.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aurikqq.assist.ACTION_REQUEST_COMMAND_EXECUTING
import com.aurikqq.assist.ACTION_REQUEST_SCREENSHOT
import com.aurikqq.assist.CategoriesScreens
import com.aurikqq.assist.IS_ALWAYS_LISTENING_ENABLED
import com.aurikqq.assist.IS_ALWAYS_LISTENING_FAB_ENABLED
import com.aurikqq.assist.MainScreens
import com.aurikqq.assist.PREFERENCES_NAME
import com.aurikqq.assist.R
import com.aurikqq.assist.commands.MusicHandler
import com.aurikqq.assist.commands.captureScreenshot
import com.aurikqq.assist.screens.mainScreenCategories.AlwaysListeningCategoryScreen
import com.aurikqq.assist.screens.mainScreenCategories.GeneralCategoryScreen
import com.aurikqq.assist.screens.mainScreenCategories.MusicControlCategoryScreen
import com.aurikqq.assist.screens.mainScreenCategories.ScreenshotsCategoryScreen
import com.aurikqq.assist.templates.AlwaysListeningCategoryCardPicture
import com.aurikqq.assist.templates.AlwaysListeningScreenTopBar
import com.aurikqq.assist.templates.CategoryCard
import com.aurikqq.assist.templates.GeneralCategoryCardPicture
import com.aurikqq.assist.templates.GeneralScreenTopBar
import com.aurikqq.assist.templates.MainScreenTopBar
import com.aurikqq.assist.templates.MusicControlCategoryCardPicture
import com.aurikqq.assist.templates.MusicControlScreenTopBar
import com.aurikqq.assist.templates.ScreenshotScreenTopBar
import com.aurikqq.assist.templates.ScreenshotsCategoryCardPicture
import com.aurikqq.assist.ui.theme.NothingTheme
import com.aurikqq.assist.voice.WakeWordService
import com.aurikqq.assist.voice.WakeWordService.Vosk
import com.aurikqq.assist.voice.executeCommand

data class MainScreenData(
    val isAlwaysListeningFabEnabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(/*viewModel: MainScreenViewModel*/) {
    val navController = rememberNavController()
    var topBar: @Composable () -> Unit by remember { mutableStateOf( { MainScreenTopBar() }) }

    Scaffold(
        topBar = topBar,
        bottomBar = { BottomBar() },
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainScreens.Main.name,
            modifier = Modifier
                .fillMaxSize()
                //.verticalScroll(rememberScrollState())
                .padding(
                    PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateRightPadding(LayoutDirection.Ltr),
                        bottom = 0.dp
                    )
                )
        ) {
            composable(route = MainScreens.Main.name) {
                ActionsScreen(
                    navController,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NothingTheme.colors.background)
                )
                topBar = { MainScreenTopBar() }
            }
            composable(route = MainScreens.Settings.name) {
                // Settings screen
            }
            composable(route = CategoriesScreens.General.name) {
                GeneralCategoryScreen()
                topBar = { GeneralScreenTopBar() }
            }
            composable(route = CategoriesScreens.MusicControl.name) {
                MusicControlCategoryScreen()
                topBar = { MusicControlScreenTopBar() }
            }
            composable(route = CategoriesScreens.Screenshots.name) {
                ScreenshotsCategoryScreen()
                topBar = { ScreenshotScreenTopBar() }
            }
            composable(route = CategoriesScreens.AlwaysListening.name) {
                AlwaysListeningCategoryScreen()
                topBar = { AlwaysListeningScreenTopBar() }
            }
        }
    }
}


@Composable
private fun MainScreenFab() {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE)
    val intent = Intent(context, WakeWordService::class.java)

    val isAlwaysListeningFabEnabled by rememberSaveable {
        mutableStateOf(sharedPreferences.getBoolean(
            IS_ALWAYS_LISTENING_FAB_ENABLED, true)) }

    var assistFabIcon by remember { mutableStateOf(Icons.Default.PlayArrow) }
    var alwaysListeningFabIcon by remember { mutableStateOf(Icons.Default.RecordVoiceOver) }

    var isSmallFabOpened by remember { mutableStateOf(false) }

    alwaysListeningFabIcon = if (!Vosk.isAlwaysListeningEnabled) Icons.Default.Mic
        else Icons.Default.RecordVoiceOver

    Column(horizontalAlignment = Alignment.End) {
        if (isAlwaysListeningFabEnabled) {
            SmallFloatingActionButton(
                onClick = {
                    isSmallFabOpened = true
                },
                containerColor = if (isSmallFabOpened) Color.Transparent
                    else FloatingActionButtonDefaults.containerColor
            ) {
                if (!isSmallFabOpened) {
                    Icon(alwaysListeningFabIcon, null)
                }
                else {
                    Column(modifier = Modifier.animateContentSize()) {
                        Button(
                            onClick = {
                                Vosk.isAlwaysListeningEnabled = true
                                alwaysListeningFabIcon = Icons.Default.RecordVoiceOver

                                sharedPreferences.edit { putBoolean(IS_ALWAYS_LISTENING_ENABLED,
                                    Vosk.isAlwaysListeningEnabled) }
                                isSmallFabOpened = false
                            },
                            modifier = Modifier.width(192.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Mic, null)
                                Spacer(Modifier.size(16.dp))
                                Text("Only Commands")
                            }
                        }

                        Button(
                            onClick = {
                                Vosk.isAlwaysListeningEnabled = false
                                alwaysListeningFabIcon = Icons.Default.Mic

                                sharedPreferences.edit { putBoolean(IS_ALWAYS_LISTENING_ENABLED,
                                    Vosk.isAlwaysListeningEnabled) }
                                isSmallFabOpened = false
                            },
                            modifier = Modifier.width(192.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.RecordVoiceOver, null)
                                Spacer(Modifier.size(16.dp))
                                Text("With Name")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(16.dp))
        }

        LargeFloatingActionButton(
            onClick = {
                if (!Vosk.isRunning) context.startService(intent)
                else context.stopService(intent)

                Vosk.isRunning = !Vosk.isRunning
                assistFabIcon = if (!Vosk.isRunning) Icons.Default.PlayArrow
                    else Icons.Default.Stop
            }
        ) {
            Icon(assistFabIcon,
                null,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun BottomBar() {
    Column {
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .size(148.dp, 60.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NothingTheme.colors.red)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        "START",
                        fontFamily = FontFamily(Font(R.font.ndot_57_aligned)),
                        fontSize = 24.sp,
                        color = NothingTheme.colors.primary
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NothingTheme.colors.surfaceHigh)
                    .size(60.dp)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.RecordVoiceOver,
                    null,
                    Modifier.fillMaxSize(),
                    NothingTheme.colors.primary
                )
            }
        }
        Spacer(Modifier.size(28.dp))
    }
}

@SuppressLint("NewApi")
@Composable
fun ActionsScreen(navController: NavController, modifier: Modifier) {
    var isDebugEnabled by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = LocalActivity.current
    val musicHandler = remember { MusicHandler.getInstance(context.applicationContext) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ACTION_REQUEST_SCREENSHOT) {
                    captureScreenshot(activity as Activity) { file ->
                        Log.d("Screenshot", "Screenshot saved")
                        Toast.makeText(context, "Screenshot saved!", Toast.LENGTH_LONG).show()
                    }
                }
                else if (intent?.action == ACTION_REQUEST_COMMAND_EXECUTING) {
                    val command = intent.getStringExtra("command") as String
                    Log.d("Command Execution", "Command executing requested: $command")
                    executeCommand(command, activity as Activity)
                }
            }
        }
        val broadcastManager = LocalBroadcastManager.getInstance(context)
        broadcastManager.registerReceiver(receiver, IntentFilter().apply {
            addAction(ACTION_REQUEST_SCREENSHOT)
            addAction(ACTION_REQUEST_COMMAND_EXECUTING)
        })
        onDispose { broadcastManager.unregisterReceiver(receiver) }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
        modifier = modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 0.dp, bottomStart = 0.dp))
    ) {
        item {
            CategoryCard(
                picture = { GeneralCategoryCardPicture() },
                title = R.string.general_category_title,
                description = R.string.general_category_description,
                onCardClicked = { navController.navigate(CategoriesScreens.General.name) }
            )
        }

        item{
            Spacer(modifier = Modifier.height(16.dp))

            CategoryCard(
                picture = { MusicControlCategoryCardPicture() },
                title = R.string.music_control_category_title,
                description = R.string.music_control_category_description,
                onCardClicked = { navController.navigate(CategoriesScreens.MusicControl.name) }
            )
        }

        item{
            Spacer(modifier = Modifier.height(16.dp))

            CategoryCard(
                picture = { ScreenshotsCategoryCardPicture() },
                title = R.string.screenshots_category_title,
                description = R.string.screenshots_category_description,
                onCardClicked = { navController.navigate(CategoriesScreens.Screenshots.name) }
            )
        }

        item{
            Spacer(modifier = Modifier.height(16.dp))

            CategoryCard(
                picture = { AlwaysListeningCategoryCardPicture() },
                title = R.string.always_listening_category_title,
                description = R.string.always_listening_category_description,
                onCardClicked = { navController.navigate(CategoriesScreens.Screenshots.name) }
            )
        }

        item {
            Spacer(modifier = Modifier
                .height(32.dp)
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = { isDebugEnabled = !isDebugEnabled }
                )
            )

            if(isDebugEnabled) {
                Spacer(modifier = Modifier.size(32.dp))

                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            captureScreenshot(activity as Activity) { file ->
                                Log.d("Screenshot", "Screenshot saved")
                                Toast.makeText(context, "Screenshot saved!",
                                    Toast.LENGTH_LONG).show()
                            }
                        }) {
                            Icon(
                                Icons.Filled.Screenshot,
                                contentDescription = null,
                            )
                        }
                        Button(onClick = {
                            musicHandler.play()
                        }) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = null,
                            )
                        }
                        Button(onClick = {
                            musicHandler.pause()
                        }) {
                            Icon(
                                Icons.Filled.Pause,
                                contentDescription = null,
                            )
                        }
                        Button(onClick = {
                            executeCommand("дальше", activity as Activity)
                            //musicHandler.next()
                        }) {
                            Icon(
                                Icons.Filled.SkipNext,
                                contentDescription = null,
                            )
                        }
                        Button(onClick = {
                            musicHandler.prev()
                        }) {
                            Icon(
                                Icons.Filled.SkipPrevious,
                                contentDescription = null,
                            )
                        }
                    }
                    Spacer(Modifier.size(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            musicHandler.shuffle_on()
                        }) {
                            Icon(
                                Icons.Filled.Shuffle,
                                contentDescription = null,
                            )
                        }
                        Button(onClick = {
                            musicHandler.shuffle_off()
                        }) {
                            Icon(
                                Icons.Filled.ShuffleOn,
                                contentDescription = null,
                            )
                        }
                        Button(onClick = {
                            musicHandler.repeat_all()
                        }) {
                            Icon(
                                Icons.Filled.Repeat,
                                contentDescription = null,
                            )
                        }
                        Button(onClick = {
                            musicHandler.repeat_one()
                        }) {
                            Icon(
                                Icons.Filled.RepeatOne,
                                contentDescription = null,
                            )
                        }
                        Button(onClick = {
                            musicHandler.repeat_off()
                        }) {
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                            )
                        }
                    }
                }

                Text("permissions: microphone, notification access, storage (api 28-)")
                // it would be nice to ask all there permissions, but not now
            }
        }
    }
}

// TODO
// asking for permissions
// DONE fix topbar stuck when going to another screen
// DONE make screen and settings for always-listening
// DONE make always-listening

// damn im tired
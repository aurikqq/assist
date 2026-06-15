package com.aurikqq.assist.screens.mainScreenCategories

import android.content.Context.MODE_PRIVATE
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.aurikqq.assist.ALWAYS_LISTENING_OFF_COMMANDS
import com.aurikqq.assist.ALWAYS_LISTENING_ON_COMMANDS
import com.aurikqq.assist.IS_ALWAYS_LISTENING_FAB_ENABLED
import com.aurikqq.assist.PREFERENCES_NAME
import com.aurikqq.assist.ui.theme.AssistBasicTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlwaysListeningCategoryScreen() {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        AlwaysListeningScreen(modifier = Modifier.padding(innerPadding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlwaysListeningScreen(modifier: Modifier) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)

    var enablingCommands = sharedPreferences.getString(ALWAYS_LISTENING_ON_COMMANDS, "слушай, прослушка, слушать")
    var disablingCommands = sharedPreferences.getString(ALWAYS_LISTENING_OFF_COMMANDS, "хватит, не слушай, обратно")
    var isEnablingCommandsPickerDialogShown by remember { mutableStateOf(false) }
    var isDisablingCommandsPickerDialogShown by remember { mutableStateOf(false) }
    var isAlwaysListeningWarningDialogShown by remember { mutableStateOf(false) }
    var isAlwaysListeningFabEnabled = sharedPreferences.getBoolean(IS_ALWAYS_LISTENING_FAB_ENABLED, true)

    LazyColumn(modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
            Text(
                "Always-Listening is an Assist's mode which allows you to " +
                    "say commands without its name, e.g. \"Pause\" instead of \"Assist, pause\". " +
                    "It's convenient for fast work, but can lead to accidental command recognition " +
                    "in usual talk.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Always-Listening Floating Button") },
                supportingContent = { Text("Displays floating button in main menu for quick enabling Always-Listening") },
                leadingContent = {
                    Icon(
                        Icons.Default.RadioButtonChecked,
                        null
                    )
                },
                trailingContent = { Switch(checked = isAlwaysListeningFabEnabled, onCheckedChange = {
                    isAlwaysListeningFabEnabled = it
                    sharedPreferences.edit { putBoolean(IS_ALWAYS_LISTENING_FAB_ENABLED, it) }
                }) }
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Enabling Commands") },
                supportingContent = { Text("слушай, слушать, прослушка") },
                leadingContent = {
                    Icon(
                        Icons.Default.KeyboardVoice,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isEnablingCommandsPickerDialogShown = true }),
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Disabling Commands") },
                supportingContent = { Text("хватит, не слушай, обратно") },
                leadingContent = { Spacer(Modifier.width(24.dp)) },
                modifier = Modifier.clickable(onClick = { isEnablingCommandsPickerDialogShown = true }),
            )
        }
        // here also must be info about how i use the collected speech and what can alw-listen lead to (battery drain etc.),
        // as well as warning about android being able to kill process
        item {
            TextButton(
                onClick = { isAlwaysListeningWarningDialogShown = true },
                Modifier.padding(16.dp)
            ) {
                Text("A few words about this function")
            }
        }
    }

    if (isEnablingCommandsPickerDialogShown) {
        BasicAlertDialog(onDismissRequest = { isEnablingCommandsPickerDialogShown = false }) {
            var tempEnablingCommands by remember { mutableStateOf(enablingCommands) }

            Surface(Modifier
                .background(MaterialTheme.colorScheme.onBackground)
            ) {
                Column {
                    Text("Choose Commands")

                    OutlinedTextField(
                        value = tempEnablingCommands ?: "",
                        onValueChange = { tempEnablingCommands = it }
                    )

                    Row {
                        ElevatedButton(onClick = {
                            isEnablingCommandsPickerDialogShown = false
                            enablingCommands = tempEnablingCommands
                            sharedPreferences.edit { putString(ALWAYS_LISTENING_ON_COMMANDS, tempEnablingCommands) }
                        }) {
                            Text("Save")
                        }
                        Button(onClick = { isEnablingCommandsPickerDialogShown = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    if (isDisablingCommandsPickerDialogShown) {
        BasicAlertDialog(onDismissRequest = { isDisablingCommandsPickerDialogShown = false }) {
            var tempDisablingCommands by remember { mutableStateOf(disablingCommands) }

            Surface(Modifier
                .background(MaterialTheme.colorScheme.onBackground)
            ) {
                Column {
                    Text("Choose Commands")

                    OutlinedTextField(
                        value = tempDisablingCommands ?: "",
                        onValueChange = { tempDisablingCommands = it }
                    )

                    Row {
                        ElevatedButton(onClick = {
                            isDisablingCommandsPickerDialogShown = false
                            disablingCommands = tempDisablingCommands
                            sharedPreferences.edit { putString(ALWAYS_LISTENING_OFF_COMMANDS, tempDisablingCommands) }
                        }) {
                            Text("Save")
                        }
                        Button(onClick = { isDisablingCommandsPickerDialogShown = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    if (isAlwaysListeningWarningDialogShown) {
        BasicAlertDialog(
            onDismissRequest = { isAlwaysListeningWarningDialogShown = false }
        ) {
            Surface(Modifier.background(MaterialTheme.colorScheme.onBackground)) {
                Column {
                    LazyColumn {
                        item {
                            Text(
                                "How does voice recognition work?",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text("All of heard speech is transforming to text via Vosk, an offline model, " +
                                    "built in the app. For now it's the only way your words are recognized.")
                            Spacer(modifier = Modifier.padding(16.dp))
                        }

                        item {
                            Text(
                                "Are my records collecting?",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text("As said, Vosk is an offline model, so it doesn't send any data " +
                                    "to the server or something (at least, I guess so). As for me, I'm overmore" +
                                    "not needed in your microphone records. Why would I listen to some unknown people? " +
                                    "So, all that you said stays here, on your device... no, it isn't even saved.")
                            Spacer(modifier = Modifier.padding(16.dp))
                        }

                        item {
                            Text(
                                "Does Assist always listen for commands?",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text("Yeah, when it's possible. Main trouble is Android's restrictions - " +
                                    "it can close the app (kill the process) even if it's in background. " +
                                    "That's why you see a permanent notification when Assist is not on foreground, " +
                                    "but even though this app can be closed by system. There's nothing I can do.\n\n" +
                                    "Also, another pain is different ROMs - brand's OS. Some of them are yet more " +
                                    "aggressive to apps, and can close it more often - that's MIUI/HyperOS, One UI, EMUI, etc." +
                                    "And there are only few of more calm systems - Pixel/Nothing OS and maybe something other.")
                            Spacer(modifier = Modifier.padding(16.dp))
                        }
                        item {
                            Text(
                                "Is my battery drained by Assist?",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text("Constant listening is consuming battery charge, but I do my best to reduce it. " +
                                    "As I tested on Samsung, it takes about 1%/hour. Not so much, I guess?\n" +
                                    "After all, it's your choice.")
                            Spacer(modifier = Modifier.padding(16.dp))
                        }
                    }

                    Row {
                        ElevatedButton(onClick = { isDisablingCommandsPickerDialogShown = false }) {
                            Text("Save")
                        }
                        Button(onClick = { isDisablingCommandsPickerDialogShown = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    AssistBasicTheme {
        AlwaysListeningCategoryScreen()
    }
}

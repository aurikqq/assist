package com.aurikqq.assist.screens.mainScreenCategories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.aurikqq.assist.ui.theme.AssistBasicTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicControlCategoryScreen() {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        MusicControlScreen(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun MusicControlScreen(modifier: Modifier) {
    var isPlayCommandPickerDialogShown by remember { mutableStateOf(false) }
    var isPauseCommandPickerDialogShown by remember { mutableStateOf(false) }
    var isNextCommandPickerDialogShown by remember { mutableStateOf(false) }
    var isPrevCommandPickerDialogShown by remember { mutableStateOf(false) }
    var isShuffleOnCommandPickerDialogShown by remember { mutableStateOf(false) }
    var isShuffleOffCommandPickerDialogShown by remember { mutableStateOf(false) }
    var isRepeatAllCommandPickerDialogShown by remember { mutableStateOf(false) }
    var isRepeatOneCommandPickerDialogShown by remember { mutableStateOf(false) }
    var isRepeatOffCommandPickerDialogShown by remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            ListItem(
                headlineContent = { Text("Play Commands") },
                supportingContent = { Text("пуск, включай") },
                leadingContent = {
                    Icon(
                        Icons.Default.PlayArrow,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isPlayCommandPickerDialogShown = true }),
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Pause Commands") },
                supportingContent = { Text("пауза, стоп") },
                leadingContent = {
                    Icon(
                        Icons.Default.Pause,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isPauseCommandPickerDialogShown = true }),
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Next Commands") },
                supportingContent = { Text("дальше, далее") },
                leadingContent = {
                    Icon(
                        Icons.Default.SkipNext,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isNextCommandPickerDialogShown = true }),
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Previous Commands") },
                supportingContent = { Text("назад") },
                leadingContent = {
                    Icon(
                        Icons.Default.SkipPrevious,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isPrevCommandPickerDialogShown = true }),
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Shuffle On Commands") },
                supportingContent = { Text("вперемешку, перемешай, перемешивание") },
                leadingContent = {
                    Icon(
                        Icons.Default.Shuffle,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isShuffleOnCommandPickerDialogShown = true }),
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Shuffle Off Commands") },
                supportingContent = { Text("по порядку, выключи перемешивание, без перемешивания") },
                leadingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.PlaylistPlay,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isShuffleOffCommandPickerDialogShown = true }),
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Repeat All Commands") },
                supportingContent = { Text("репит всего, повтор всего") },
                leadingContent = {
                    Icon(
                        Icons.Default.Repeat,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isRepeatAllCommandPickerDialogShown = true }),
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Repeat One Commands") },
                supportingContent = { Text("репит трека, повтор трека") },
                leadingContent = {
                    Icon(
                        Icons.Default.RepeatOne,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isRepeatOneCommandPickerDialogShown = true }),
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Repeat Off Commands") },
                supportingContent = { Text("без повтора, без репита, выключи повтор, выключи репит") },
                leadingContent = {
                    Icon(
                        Icons.Default.RepeatOn,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isRepeatOffCommandPickerDialogShown = true }),
            )
        }
    }

    if (isPlayCommandPickerDialogShown) {
        Dialog(onDismissRequest = { isPlayCommandPickerDialogShown = false }) {
            Surface(Modifier
                .background(MaterialTheme.colorScheme.onBackground)
            ) {
                Column {
                    Text("Choose Play commands")

                    Dialog(onDismissRequest = { isPlayCommandPickerDialogShown = false }) {
                        Surface(Modifier
                            .background(MaterialTheme.colorScheme.onBackground)
                        ) {
                            Column {
                                Text("Choose Assist's name")
                                OutlinedTextField(value = "", onValueChange = {})
                                Row {
                                    ElevatedButton(onClick = { isPlayCommandPickerDialogShown = false }) {
                                        Text("Save")
                                    }
                                    Button(onClick = { isPlayCommandPickerDialogShown = false }) {
                                        Text("Cancel")
                                    }
                                }
                            }
                        }
                    }

                    Row {
                        ElevatedButton(onClick = { isPlayCommandPickerDialogShown = false }) {
                            Text("Save")
                        }
                        Button(onClick = { isPlayCommandPickerDialogShown = false }) {
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
        MusicControlCategoryScreen()
    }
}

// TODO
// different screens for every command
// enable/disable switches and commands choose
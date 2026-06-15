package com.aurikqq.assist.screens.mainScreenCategories

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import com.aurikqq.assist.ASSISTANT_NAME
import com.aurikqq.assist.IS_SOUND_SIGNALS_ENABLED
import com.aurikqq.assist.PREFERENCES_NAME
import com.aurikqq.assist.ui.theme.AssistBasicTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralCategoryScreen() {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        GeneralScreen(modifier = Modifier.padding(innerPadding))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralScreen(modifier: Modifier) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    var assistantName by rememberSaveable {
        mutableStateOf(sharedPreferences.getString(ASSISTANT_NAME, "ассист") ?: "")
    }
    var isSoundSignalsEnabled by rememberSaveable {
        mutableStateOf(sharedPreferences.getBoolean(IS_SOUND_SIGNALS_ENABLED, true))
    }

    var isNamePickerDialogShown by remember { mutableStateOf(false) }
    var tempAssistantName = rememberSaveable { mutableStateOf(assistantName) }

    LazyColumn(modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            ListItem(
                headlineContent = { Text("Assist's Name") },
                supportingContent = { Text(assistantName) },
                leadingContent = {
                    Icon(
                        Icons.Default.RecordVoiceOver,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isNamePickerDialogShown = true }),
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Sound Signals") },
                trailingContent = {
                    Switch(checked = isSoundSignalsEnabled, onCheckedChange = {
                        sharedPreferences.edit {
                            putBoolean(IS_SOUND_SIGNALS_ENABLED, !isSoundSignalsEnabled)
                        }
                        isSoundSignalsEnabled = !isSoundSignalsEnabled
                    })
                },
                leadingContent = {
                    Icon(
                        Icons.Default.MusicNote,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { /*TODO*/ }),
            )
        }

        item {
            ListItem(
                headlineContent = { Text(
                    "Configures sound signals on command executing. " +
                        "Changes apply to all commands, but you can set it individually " +
                        "for every of them in its settings",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                ) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        contentDescription = null,
                    )
                },
            )
        }
    }

    if (isNamePickerDialogShown) {
        BasicAlertDialog(
            onDismissRequest = { isNamePickerDialogShown = false },
            modifier = Modifier,
            properties = DialogProperties(),
            content = {
                Card(
                    elevation = CardDefaults.cardElevation(16.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Choose Assist's name", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.size(24.dp))

                        OutlinedTextField(value = tempAssistantName.value, onValueChange = {tempAssistantName.value = it})
                        Spacer(Modifier.size(24.dp))

                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            ElevatedButton(onClick = {
                                tempAssistantName.value = assistantName
                                isNamePickerDialogShown = false
                            }) {
                                Text("Cancel")
                            }
                            Spacer(Modifier.size(16.dp))
                            Button(onClick = {
                                sharedPreferences.edit {
                                    putString(ASSISTANT_NAME, tempAssistantName.value)
                                }
                                assistantName = tempAssistantName.value
                                isNamePickerDialogShown = false
                            }) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    AssistBasicTheme {
        GeneralCategoryScreen()
    }
}

// TODO
// change the info background color
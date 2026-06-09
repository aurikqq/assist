package com.aurikqq.assistbasic.screens.mainScreenCategories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.TypeSpecimen
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import com.aurikqq.assistbasic.ui.theme.AssistBasicTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotsCategoryScreen() {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        ScreenshotsScreen(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun ScreenshotsScreen(modifier: Modifier) {
    var isTypePickerDialogShown by remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            ListItem(
                headlineContent = { Text("Screenshots Directory") },
                supportingContent = { Text("DCIM/Screenshots") },
                leadingContent = {
                    Icon(
                        Icons.Default.Folder,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { /*TODO*/ }),
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Screenshots Type") },
                supportingContent = { Text("PNG") },
                leadingContent = {
                    Icon(
                        Icons.Default.TypeSpecimen,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isTypePickerDialogShown = true }),
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Commands") },
                supportingContent = { Text("скрин, снимок, скриншот, фото") },
                leadingContent = {
                    Icon(
                        Icons.Default.KeyboardVoice,
                        null
                    )
                },
                modifier = Modifier.clickable(onClick = { isTypePickerDialogShown = true }),
            )
        }
    }

    if (isTypePickerDialogShown) {
        Dialog(onDismissRequest = { isTypePickerDialogShown = false }) {
            Surface(Modifier
                .background(MaterialTheme.colorScheme.onBackground)
            ) {
                Column {
                    Text("Choose screenshots type")

                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("JPEG") },
                        leadingContent = { RadioButton(onClick = {}, selected = false) },
                    )
                    ListItem(
                        headlineContent = { Text("PNG") },
                        leadingContent = { RadioButton(onClick = {}, selected = false) }
                    )
                    HorizontalDivider()

                    Row {
                        ElevatedButton(onClick = { isTypePickerDialogShown = false }) {
                            Text("Save")
                        }
                        Button(onClick = { isTypePickerDialogShown = false }) {
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
        ScreenshotsCategoryScreen()
    }
}

// TODO
// listitems functionality
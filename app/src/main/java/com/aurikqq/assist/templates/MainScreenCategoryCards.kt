package com.aurikqq.assist.templates

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aurikqq.assist.R
import com.aurikqq.assist.ui.theme.NothingTheme

@Composable
fun CategoryCard(
    picture: @Composable () -> Unit?,
    @StringRes title: Int,
    @StringRes description: Int,
    onCardClicked: () -> Unit
) {
    Card(
        colors = CardColors(
            containerColor = NothingTheme.colors.surfaceHigh,
            contentColor = Color.Unspecified,
            disabledContentColor = Color.Unspecified,
            disabledContainerColor = Color.Unspecified),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        modifier = Modifier
            .height(240.dp)
            .fillMaxWidth()
            .clickable(onClick = onCardClicked)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NothingTheme.colors.surfaceLow)
                    .zIndex(0f)
            ) { picture() }

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 19.sp,
                            color = NothingTheme.colors.primary
                        )
                    ) {
                        append(stringResource(title) + "\n")
                    }
                    withStyle(SpanStyle(
                        fontSize = 14.sp,
                        color = NothingTheme.colors.secondary)
                    ) {
                        append(stringResource(description))
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .background(NothingTheme.colors.surfaceHigh)
                    .padding(12.dp)
                    .height(80.dp)
                    .zIndex(1f)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}


@Composable
fun GeneralCategoryCardPicture() {
    Icon(
        imageVector = Icons.Default.Build,
        contentDescription = null,
        tint = NothingTheme.colors.surfaceHigh,
        modifier = Modifier
            .size(100.dp)
            .offset(210.dp, 50.dp)
            .rotate(15f)
    )
    Icon(
        imageVector = Icons.Default.Person,
        contentDescription = null,
        tint = NothingTheme.colors.surfaceHigh,
        modifier = Modifier
            .size(200.dp)
            .offset(20.dp, (-20).dp)
    )
}

@Composable
fun MusicControlCategoryCardPicture() {
    Icon(
        imageVector = Icons.Default.PlayArrow,
        contentDescription = null,
        tint = NothingTheme.colors.surfaceHigh,
        modifier = Modifier
            .size(240.dp)
            .offset((-40).dp, (-10).dp)
    )
    Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = null,
        tint = NothingTheme.colors.surfaceHigh,
        modifier = Modifier
            .size(140.dp)
            .offset(200.dp, 60.dp)
            .rotate(10f)
    )
    Text(
        text = "PAUSE",
        color = NothingTheme.colors.surfaceHigh,
        fontFamily = FontFamily(Font(R.font.ndot_57_aligned)),
        fontSize = 45.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.offset(150.dp, 32.dp)
    )
}

@Composable
fun ScreenshotsCategoryCardPicture() {
    Icon(
        imageVector = Icons.Default.PhoneAndroid,
        contentDescription = null,
        tint = NothingTheme.colors.surfaceHigh,
        modifier = Modifier
            .size(200.dp)
            .offset(0.dp, 30.dp)
    )
    Icon(
        imageVector = Icons.Default.CameraAlt,
        contentDescription = null,
        tint = NothingTheme.colors.surfaceHigh,
        modifier = Modifier
            .size(80.dp)
            .offset(60.dp, 80.dp)
    )
    Icon(
        imageVector = Icons.Default.PhotoLibrary,
        contentDescription = null,
        tint = NothingTheme.colors.surfaceHigh,
        modifier = Modifier
            .size(100.dp)
            .offset(210.dp, 16.dp)
    )
}

@Composable
fun AlwaysListeningCategoryCardPicture() {
    Icon(
        imageVector = Icons.Default.RecordVoiceOver,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.45f),
        modifier = Modifier
            .size(180.dp)
            .offset((-24).dp, (-10).dp)
    )
    Icon(
        imageVector = Icons.Default.Mic,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.9f),
        modifier = Modifier
            .size(100.dp)
            .offset(152.dp, 52.dp)
    )
    Icon(
        imageVector = Icons.Default.AccessTime,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.9f),
        modifier = Modifier
            .size(80.dp)
            .offset(252.dp, 8.dp)
    )
}

@Composable
private fun PreviewScreen() {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
    ) {
        item {
            CategoryCard(
                picture = { MusicControlCategoryCardPicture() },
                title = R.string.music_control_category_title,
                description = R.string.music_control_category_description,
                onCardClicked = { }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            CategoryCard(
                picture = { AlwaysListeningCategoryCardPicture() },
                title = R.string.wip_category,
                description = R.string.wip_category_description,
                onCardClicked = { }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            CategoryCard(
                picture = { ScreenshotsCategoryCardPicture() },
                title = R.string.screenshots_category_title,
                description = R.string.screenshots_category_description,
                onCardClicked = { }
            )
        }
    }
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PreviewPicture() {
    NothingTheme {
        PreviewScreen()
    }
}
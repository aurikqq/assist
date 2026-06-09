package com.aurikqq.assistbasic

import androidx.annotation.StringRes


enum class MainScreens(@StringRes val title: Int) {
    Main(title = R.string.app_name),
    Notes(title = R.string.notes_screen_title),
    Settings(title = R.string.settings_screen_title)
}

enum class CategoriesScreens(@StringRes val title: Int) {
    General(title = R.string.general_category_title),
    MusicControl(title = R.string.music_control_category_title),
    Screenshots(title = R.string.screenshots_category_title),
    AlwaysListening(title = R.string.always_listening_category_title)
}


// sharedPreferences' keys

const val PREFERENCES_NAME = "com.aurikqq.assistbasic.SharedPreferences"
const val ASSISTANT_NAME = "assistant_name"
const val IS_SOUND_SIGNALS_ENABLED = "is_sound_signals_enabled"
const val ALWAYS_LISTENING_ON_COMMANDS = "always_listening_on_commands"
const val ALWAYS_LISTENING_OFF_COMMANDS = "always_listening_off_commands"
const val IS_ALWAYS_LISTENING_FAB_ENABLED = "is_always_listening_fab_enabled"
const val IS_ALWAYS_LISTENING_ENABLED = "is_always_listening_enabled"
const val MUSIC_CONTROL_PLAY_COMMANDS = "music_control_play_commands"
const val MUSIC_CONTROL_PAUSE_COMMANDS = "music_control_pause_commands"
const val MUSIC_CONTROL_NEXT_COMMANDS = "music_control_next_commands"
const val MUSIC_CONTROL_PREV_COMMANDS = "music_control_prev_commands"
const val MUSIC_CONTROL_SHUFFLE_ON_COMMANDS = "music_control_shuffle_on_commands"
const val MUSIC_CONTROL_SHUFFLE_OFF_COMMANDS = "music_control_shuffle_off_commands"
const val MUSIC_CONTROL_REPEAT_ALL_COMMANDS = "music_control_repeat_all_commands"
const val MUSIC_CONTROL_REPEAT_ONE_COMMANDS = "music_control_repeat_one_commands"
const val MUSIC_CONTROL_REPEAT_OFF_COMMANDS = "music_control_repeat_off_commands"
const val SCREENSHOTS_COMMANDS = "screenshots_commands"
const val SCREENSHOTS_DIRECTORY = "screenshots_directory"
const val SCREENSHOTS_TYPE = "screenshots_type"


// InAppRecognition.kt - intents

const val ACTION_RECOGNITION_RESULT = "com.aurikqq.assistbasic.ACTION_RECOGNITION_RESULT"
const val ACTION_REQUEST_SCREENSHOT = "com.aurikqq.assistbasic.ACTION_REQUEST_SCREENSHOT"
const val ACTION_REQUEST_COMMAND_EXECUTING = "com.aurikqq.assistbasic.ACTION_COMMAND_EXECUTING"
const val ACTION_UPDATE_FOREGROUND_RECOGNIZER = "com.aurikqq.assistbasic.ACTION_UPDATE_FOREGROUND_RECOGNIZER"
const val ACTION_STOP_FOREGROUND_RECOGNIZER = "com.aurikqq.assistbasic.ACTION_STOP_FOREGROUND_RECOGNIZER"
const val EXTRA_RECOGNIZED_TEXT = "com.aurikqq.assistbasic.EXTRA_RECOGNIZED_TEXT"


// Lists of UI- and nonUI-commands

val commandsList = listOf(
    "дальше", "далее",
    "назад",
    "стоп", "пауза",
    "пуск", "включай",
    "вперемешку", "перемешай", "перемешивание",
    "по порядку", "выключи перемешивание", "без перемешивания",
    "репит всего", "повтор всего",
    "репит трека", "повтор трека",
    "без повтора", "без репита", "выключи повтор", "выключи репит",
    "тише", "убавь",
    "громче", "увеличь",
    "звук", "громкость",
    "время", "который час"
)
val screenshotCommands = listOf("скрин", "скриншот", "фото", "снимок")
val musicNextKeywords = listOf("дальше", "далее")
val musicPrevKeywords = listOf("назад")
val musicPauseKeywords = listOf("стоп", "пауза")
val musicPlayKeywords = listOf("пуск", "включай")
val musicShuffleOnKeywords = listOf("вперемешку", "перемешай", "перемешивание")
val musicShuffleOffKeywords = listOf("по порядку", "выключи перемешивание", "без перемешивания")
val musicRepeatAllKeywords = listOf("репит всего", "повтор всего")
val musicRepeatOneKeywords = listOf("репит трека", "повтор трека")
val musicRepeatOffKeywords = listOf("без повтора", "без репита", "выключи повтор", "выключи репит")
val alwaysListeningOnKeywords = listOf("слушай", "прослушка", "слушать")
val alwaysListeningOffKeywords = listOf("не слушай", "обратно", "хватит")
val lowerMusicVolume = listOf("тише", "убавь")
val raiseMusicVolume = listOf("громче", "увеличь")
val setMusicVolume = listOf("звук", "громкость")
val sayTime = listOf("время", "который час")

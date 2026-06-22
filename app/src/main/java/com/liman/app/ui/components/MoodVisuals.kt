package com.liman.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.FilterDrama
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector
import com.liman.app.data.model.EmotionalWeather
import com.liman.app.data.model.MoodFace

/** Ruh hali yüzü → Material ikonu (emoji yerine). */
fun moodIcon(face: MoodFace): ImageVector = when (face) {
    MoodFace.VERY_LOW -> Icons.Rounded.SentimentVeryDissatisfied
    MoodFace.LOW -> Icons.Rounded.SentimentDissatisfied
    MoodFace.NEUTRAL -> Icons.Rounded.SentimentNeutral
    MoodFace.GOOD -> Icons.Rounded.SentimentSatisfied
    MoodFace.GREAT -> Icons.Rounded.SentimentVerySatisfied
}

/** Duygusal hava → Material ikonu (emoji yerine). */
fun weatherIcon(weather: EmotionalWeather): ImageVector = when (weather) {
    EmotionalWeather.SUNNY -> Icons.Rounded.WbSunny
    EmotionalWeather.WARM -> Icons.Rounded.WbTwilight
    EmotionalWeather.CALM -> Icons.Rounded.FilterDrama
    EmotionalWeather.CLOUDY -> Icons.Rounded.Cloud
    EmotionalWeather.STORMY -> Icons.Rounded.Thunderstorm
}

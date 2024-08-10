package com.example.products.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun AnimatedTextCounter(count: Int, animDuration: Int, modifier: Modifier) {
    var oldCount by remember {
        mutableIntStateOf(count)
    }

    SideEffect { oldCount = count }

    Row(modifier) {
        for (i in count.toString().indices) {
            val oldChar = oldCount.toString().getOrNull(i)
            val newChar = count.toString()[i]

            val char = if (oldChar == newChar) oldChar else newChar

            val toIncrement = oldCount < count

            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    if (toIncrement) slideInVertically(animationSpec = tween(durationMillis = animDuration)) { -it } togetherWith slideOutVertically(
                        animationSpec = tween(durationMillis = animDuration)
                    ) { it }
                    else slideInVertically(animationSpec = tween(durationMillis = animDuration)) { it } togetherWith slideOutVertically(
                        animationSpec = tween(durationMillis = animDuration)
                    ) { -it }
                },
                label = "animatedTextCounter"
            ) { targetChar ->
                Text(text = targetChar.toString())
            }
        }
    }
}
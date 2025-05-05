package com.example.products.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.products.R

@Composable
fun LoadingDialog() {
    Dialog(onDismissRequest = {}) {
        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.importing_data))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            isPlaying = true,
            iterations = LottieConstants.IterateForever
        )

        LottieAnimation(
            progress = progress,
            composition = composition,
            modifier = Modifier.size(150.dp)
        )
    }
}
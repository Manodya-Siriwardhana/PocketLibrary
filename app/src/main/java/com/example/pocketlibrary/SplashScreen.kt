package com.example.pocketlibrary


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.*

//displays  a splash animation
@Composable
fun SplashScreen(onAnimationFinished: ()->Unit){
    //load lottie composition from raw resource
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash))
    //animate the composition once
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 1,  //play animation only once
        isPlaying = true,
        speed = 1.0f,
        restartOnPlay = false,
        clipSpec = LottieClipSpec.Progress(0f,1f)
    )

    //display the lottie animation
    LottieAnimation(composition,progress,modifier= Modifier.fillMaxSize())

    //trigger callback when animation finishes
    if(progress==1f)
    {
        LaunchedEffect(Unit) {
            onAnimationFinished()
        }
    }
}

// References
// https://youtu.be/bvQym_hszeI?si=nvg7WL_f3QS_Ktq2
// https://medium.com/@ridvanozcan48/android-compose-making-splash-screen-with-lottie-f6bc5ae2c6ef
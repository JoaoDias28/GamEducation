package com.example.gameducation

import android.content.Context
import android.os.CountDownTimer
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.rememberImagePainter

@Composable
fun ImageWidget(content: ConteudoDidatico, dataCallback: DataCallback) {

    val timerDuration = 60000L // 60 seconds in milliseconds
    var isTimerRunning by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableStateOf(timerDuration) }

    // Start the timer when the Composable is first displayed
    if (!isTimerRunning) {
        isTimerRunning = true
        object : CountDownTimer(timerDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
            }

            override fun onFinish() {
                // Timer finished, you can perform actions here
                isTimerRunning = false
                var correto = true
                var percentagem = 100.toFloat()
                content.visto = correto
                dataCallback.onDataGenerated(correto, percentagem,null, content)
            }
        }.start()
    }

    // Display the timer
    Text(
        text = "Time remaining: ${remainingTime / 1000} seconds",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(16.dp)
    )

        println("conteudoPath " + content.conteudo_path)
        val imageUrl =
            "http://10.0.2.2:80/framework/professor/conteudos_didaticos/${content.conteudo_path}"
        Image(
            painter = rememberImagePainter(imageUrl),
            contentDescription = null, // Provide a meaningful description
            modifier = Modifier.fillMaxWidth() // Adjust the size as needed
        )
    }


@Composable
fun TextWidget(content: ConteudoDidatico, dataCallback: DataCallback){
    if(content.textoInformativo != null) {
        Text(
            text = content.textoInformativo,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
    else{
        Text(
            text = "Ocorreu um erro a recuperar o texto informativo",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }

        Button(
            onClick = {

                var correto = true
                content.visto = correto
                dataCallback.onDataGenerated(correto, 100.toFloat(),null,content)

            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Submit")
        }
}

@Composable
fun VideoWidget(content: ConteudoDidatico, dataCallback: DataCallback){
    val timerDuration = 60000L // 60 seconds in milliseconds
    var isTimerRunning by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableStateOf(timerDuration) }
    var correto = true
    // Start the timer when the Composable is first displayed
    if (!isTimerRunning) {
        isTimerRunning = true
        object : CountDownTimer(timerDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
            }

            override fun onFinish() {
                // Timer finished, you can perform actions here
                isTimerRunning = false

                content.visto = correto
                dataCallback.onDataGenerated(correto,100.toFloat(), null, content)
            }
        }.start()
    }

    // Display the timer
    Text(
        text = "Time remaining: ${remainingTime / 1000} seconds",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(16.dp)
    )


    val videoUrl = "http://10.0.2.2:80/framework/professor/conteudos_didaticos/${content.conteudo_path}"

    val context = LocalContext.current
    val playerViewWrapper = remember { ExoPlayerViewWrapper(context) }
    val mediaItem = remember { MediaItem.fromUri(videoUrl) }

    DisposableEffect(Unit) {
        onDispose {
            playerViewWrapper.releasePlayer()
        }
    }

    LaunchedEffect(mediaItem) {
        playerViewWrapper.setMediaItem(mediaItem)
    }

    Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
        AndroidView(
            factory = { context ->
                playerViewWrapper
            }
        )
    }


}




class ExoPlayerViewWrapper(context: Context) : FrameLayout(context) {
    private val player = ExoPlayer.Builder(context).build()


    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        val playerView = PlayerView(context)
        playerView.player = player

        addView(playerView)

    }

    fun setMediaItem(mediaItem: MediaItem) {
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    fun releasePlayer() {
        player.release()
    }

}


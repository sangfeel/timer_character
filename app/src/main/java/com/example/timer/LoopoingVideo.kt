package com.example.timer.ui

import android.net.Uri
import androidx.annotation.RawRes
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@Composable
fun LoopingVideo(
    modifier: Modifier = Modifier,
    @RawRes rawResId: Int
) {
    val context = LocalContext.current

    // 왜 remember(rawResId)인가?
    // -> rawResId가 바뀔 때만 새 플레이어를 만들고, 평소엔 재사용해서 깜빡임/자원낭비를 줄입니다.
    val exoPlayer = remember(rawResId) {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/$rawResId")
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ALL      // 무한 반복
            volume = 0f                               // 캐릭터 영상은 보통 무음
            playWhenReady = true
            prepare()
        }
    }

    // 왜 DisposableEffect인가?
    // -> 화면에서 사라질 때 player.release()를 해서 메모리/리소스 누수를 막습니다.
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false // 재생바 숨김 (캐릭터 영상용)
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }
    )
}

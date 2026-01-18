package com.example.timer.ui

import android.net.Uri
import androidx.annotation.RawRes
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import android.util.Log

@Composable
fun CharacterVideoRotator(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    @RawRes studyBaseRes: Int,
    studyOtherResList: List<Int>,
    restResList: List<Int>,
    baseRepeatCount: Int = 5
) {
    val context = LocalContext.current

    var baseCount by rememberSaveable { mutableIntStateOf(0) }
    var currentRes by rememberSaveable { mutableIntStateOf(studyBaseRes) }

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            volume = 0f
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF // ✅ 중요: 자동 루프 금지(끝나야 넘어감)
        }
    }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    // 모드 변경 시 시작 영상 선택
    LaunchedEffect(isRunning) {
        currentRes = if (isRunning) {
            studyBaseRes
        } else {
            restResList.randomOrNull() ?: studyBaseRes
        }
    }

    // currentRes 바뀌면 그 영상으로 재생
    LaunchedEffect(currentRes) {
        val uri = Uri.parse("android.resource://${context.packageName}/$currentRes")
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
    }

    // ✅ 영상이 "완전히 끝났을 때"만 다음 영상 선택
    DisposableEffect(isRunning, studyBaseRes, studyOtherResList, restResList, baseRepeatCount) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d("Rotator", "state=$playbackState running=$isRunning res=$currentRes baseCount=$baseCount")
                if (playbackState != Player.STATE_ENDED) return
                Log.d("Rotator", "ENDED! pick next")
                if (isRunning) {
                    val nextRes = if (baseCount < baseRepeatCount) {
                        baseCount += 1
                        studyBaseRes
                    } else {
                        baseCount = 0
                        (studyOtherResList.ifEmpty { listOf(studyBaseRes) }).random()
                    }

                    if (nextRes == currentRes) {
                        // ✅ 같은 영상 반복: 상태 변경 없이도 확실히 다시 재생
                        player.seekTo(0)
                        player.play()
                    } else {
                        currentRes = nextRes
                    }
                } else {
                    val nextRes = (restResList.ifEmpty { listOf(studyBaseRes) }).random()
                    if (nextRes == currentRes) {
                        player.seekTo(0)
                        player.play()
                    } else {
                        currentRes = nextRes
                    }
                }

            }
        }

        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }
    )
}

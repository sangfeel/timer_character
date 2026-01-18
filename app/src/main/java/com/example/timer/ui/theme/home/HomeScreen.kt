package com.example.timer.ui.theme.home

//package com.example.timer.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.timer.R
import com.example.timer.ui.LoopingVideo
import com.example.timer.ui.CharacterVideoRotator

import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    // ---- UI에서 필요한 시간 3종 ----
    // 1) 오늘 총 공부시간(현재는 메모리 변수로만)
    var totalStudySec by rememberSaveable { mutableIntStateOf(0) }

    // 2) 현재시간(매초 갱신)
    var nowText by remember { mutableStateOf("--:--:--") }

    // 3) "공부를 다시 시작한지 지나는 시간"(재생 중에만 증가)
    var sessionElapsedSec by rememberSaveable { mutableIntStateOf(0) }

    // 재생 상태 (나중에 타이머 상태로 대체)
    var isRunning by rememberSaveable { mutableStateOf(false) }

    // 현재 시간 업데이트
    LaunchedEffect(Unit) {
        val fmt = DateTimeFormatter.ofPattern("HH:mm:ss")
        while (true) {
            nowText = LocalTime.now().format(fmt)
            delay(1000)
        }
    }

    // 세션 경과 + 오늘 누적 업데이트(재생 중에만)
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            sessionElapsedSec += 1
            totalStudySec += 1
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 상단 영역: 시간 + LP 위젯
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // 왼쪽: 텍스트/타이머/버튼
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total | CurrentTime (한 줄)
                Text(
                    text = "Total ${formatHMS(totalStudySec)} | $nowText",
                    style = MaterialTheme.typography.titleMedium
                )

                // 아래 큰 타이머: "공부를 다시 시작한지 지나는 시간"
                Text(
                    text = formatHMS(sessionElapsedSec),
                    style = MaterialTheme.typography.displayMedium
                )

                // 버튼: Reset / Play-Pause
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset: "다시 시작한지 시간"을 0으로 (오늘 누적은 유지)
                    OutlinedButton(
                        onClick = { sessionElapsedSec = 0 }
                    ) { Text("Reset") }

                    Button(
                        onClick = { isRunning = !isRunning }
                    ) { Text(if (isRunning) "Pause" else "Play") }
                }
            }

            // 오른쪽: LP 영역(지금은 프레임만)
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.lp_frame),
                    contentDescription = "LP Frame",
                    modifier = Modifier.fillMaxSize()
                )
                // 나중에 여기에 "유튜브 썸네일 원형 crop"을 깔고,
                // 위에 lp_frame을 덮는 구조로 바꿀 예정입니다.
            }
        }

        // 하단: 캐릭터 영역 (MP4)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 지금은 테스트로 idle만. (이후 isRunning에 따라 study/rest 랜덤으로 교체)
//            LoopingVideo(
//                modifier = Modifier.size(300.dp),
//                rawResId = R.raw.study_penguin
            CharacterVideoRotator(
                modifier = Modifier.size(300.dp),
                isRunning = isRunning,
                studyBaseRes = R.raw.study_penguin, // 기본 10번
                studyOtherResList = listOf(
                    // 기본 제외한 study_들
                    R.raw.study_penguin_2,
                    R.raw.study_saying_penguin,
                    R.raw.study_standing_penguin,
                    R.raw.study_standing_penguin_2,
                    R.raw.study_standing_penguin_3
                ),
                restResList = listOf(
                    R.raw.rest_game_sleep_penguin,
                    R.raw.rest_game_sleep_penguin_2,
                    R.raw.rest_phone_penguin,
                    R.raw.rest_sunglasses_penguin
                ),
                baseRepeatCount = 10
            )

        }
    }
}

private fun formatHMS(totalSec: Int): String {
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

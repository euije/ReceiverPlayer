package com.example.receiverplayer

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private lateinit var audioManager: AudioManager
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        setContent {
            var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
            var statusText by remember { mutableStateOf("🎧 파일을 선택해주세요") }
            var isPlaying by remember { mutableStateOf(false) }

            val pickAudioLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    selectedAudioUri = result.data?.data
                    statusText = "🎵 오디오 선택됨"
                }
            }

            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = statusText)

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            type = "audio/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                        pickAudioLauncher.launch(intent)
                    }) {
                        Text("🎼 오디오 파일 선택")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        if (selectedAudioUri == null) {
                            statusText = "⚠️ 먼저 오디오 파일을 선택해주세요."
                            return@Button
                        }

                        try {
                            audioManager.mode = AudioManager.MODE_IN_CALL
                            audioManager.isSpeakerphoneOn = false

                            mediaPlayer?.release()
                            mediaPlayer = MediaPlayer().apply {
                                setAudioStreamType(AudioManager.STREAM_VOICE_CALL) // 이걸 추가해!
                                setDataSource(this@MainActivity, selectedAudioUri!!)
                                setOnPreparedListener {
                                    it.start()
                                    isPlaying = true
                                    statusText = "▶️ 재생 중..."
                                }
                                setOnCompletionListener {
                                    isPlaying = false
                                    statusText = "✅ 재생 완료"
                                }
                                prepareAsync()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            statusText = "❌ 재생 오류: ${e.message}"
                        }
                    }) {
                        Text("🔊 상단 스피커로 재생")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        mediaPlayer?.let {
                            if (it.isPlaying) {
                                it.pause()
                                isPlaying = false
                                statusText = "⏸ 일시정지됨"
                            } else {
                                it.start()
                                isPlaying = true
                                statusText = "▶️ 재생 재개"
                            }
                        }
                    }, enabled = mediaPlayer != null) {
                        Text(if (isPlaying) "⏸ 일시정지" else "▶ 재생 재개")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}

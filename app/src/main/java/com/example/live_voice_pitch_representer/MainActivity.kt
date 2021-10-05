 package com.example.live_voice_pitch_representer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.live_voice_pitch_representer.ui.main.MainFragment
import com.example.live_voice_pitch_representer.ui.main.VoicePitchFragment

 class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, VoicePitchFragment.newInstance)
                    .commitNow()
        }
    }
}
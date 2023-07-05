package dev.akiel.wearwhip

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import android.widget.Switch
import dev.akiel.wearwhip.Shaker.OnShakeListener
import dev.akiel.wearwhip.databinding.ActivityMainBinding


class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mShaker: Shaker
    private lateinit var switch: Switch
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        switch = findViewById(R.id.whipswitch)
        mShaker = Shaker(this)
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val mp = MediaPlayer.create(this, R.raw.whipcrack);
        mp.setVolume(100F, 100F)

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupAudio()

        mShaker.setOnShakeListener(object : OnShakeListener {
            override fun onShake() {
                Log.i("SHAKE", "shaking")
                v.vibrate(100)
                mp.start()
            }
        })
        enableDisableShaker()
    }

    private fun setupAudio() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        audioManager.setStreamVolume(
            AudioManager.STREAM_RING,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
            0
        )
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
    }

    fun switchClick(view: android.view.View) {
        enableDisableShaker()
    }

    private fun enableDisableShaker() {
        if (switch.isChecked) {
            mShaker.resume()
        } else {
            mShaker.pause()
        }
    }
}
package dev.akiel.wearwhip

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import dev.akiel.wearwhip.Shaker.OnShakeListener
import dev.akiel.wearwhip.databinding.ActivityMainBinding
import android.os.Vibrator
import android.widget.Switch


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
        mShaker.setOnShakeListener(object : OnShakeListener {
            override fun onShake() {
                Log.i("SHAKE", "shaking")
                v.vibrate(100)
                mp.start()

            }
        })
        mShaker.pause()
    }

    fun switchClick(view: android.view.View) {
        if (switch.isChecked) {
            mShaker.resume()
        } else {
            mShaker.pause()
        }
    }
}
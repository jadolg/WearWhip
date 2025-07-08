package dev.akiel.wearwhip

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import dev.akiel.wearwhip.Shaker.OnShakeListener
import dev.akiel.wearwhip.databinding.ActivityMainBinding


class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mShaker: Shaker
    private lateinit var closeButton: Button

    private lateinit var notificationManager: NotificationManager
    private lateinit var channel: NotificationChannel
    private val channelID = "dev.akiel.wearwhip"
    private val name = "Whip"
    private val importance = NotificationManager.IMPORTANCE_HIGH
    private val descriptionText = "Shake your watch!"
    private val notificationID = 9843
    private val requestCodePostNotifications = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        closeButton = findViewById(R.id.closeButton)
        mShaker = Shaker(this)
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val mp = MediaPlayer.create(this, R.raw.whipcrack);
        mp.setVolume(100F, 100F)

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupAudio()

        initNotificationsChannel()

        mShaker.setOnShakeListener(object : OnShakeListener {
            override fun onShake() {
                Log.i("SHAKE", "shaking")
                v.vibrate(100)
                mp.start()
            }
        })
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

    fun close(view: android.view.View) {
        disableShaker()
    }

    private fun disableShaker() {
        mShaker.pause()
        notificationManager.cancel(notificationID)
        finishAndRemoveTask()
    }


    private fun initNotificationsChannel() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            createNotificationsChannel()
            startNotification()
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                requestCodePostNotifications
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodePostNotifications) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                createNotificationsChannel()
                startNotification()
            } else {
                Log.w("NotificationPermission", "POST_NOTIFICATIONS permission denied.")
                disableShaker()
            }
            return
        }
    }

    private fun createNotificationsChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        channel = NotificationChannel(channelID, name, importance).apply {
            description = descriptionText
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun startNotification() {
        val notificationBuilder =
            NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.icon_bold_foreground)
                .setOngoing(true)

        val ongoingActivityStatus = Status.Builder()
            .addTemplate(descriptionText)
            .build()

        val newIntent = Intent(this, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            newIntent,
            PendingIntent.FLAG_IMMUTABLE + PendingIntent.FLAG_UPDATE_CURRENT
        )

        val ongoingActivity =
            OngoingActivity.Builder(
                applicationContext, notificationID, notificationBuilder
            )
                .setTouchIntent(pendingIntent)
                .setStatus(ongoingActivityStatus)
                .build()

        ongoingActivity.apply(applicationContext)

        notificationManager.notify(notificationID, notificationBuilder.build())
    }
}
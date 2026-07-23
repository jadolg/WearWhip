package dev.akiel.wearwhip

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.roundToInt

class Shaker(private val mContext: Context) : SensorEventListener {
    private var mSensorMgr: SensorManager? = null
    private var mLastX = -1.0f
    private var mLastY = -1.0f
    private var mLastZ = -1.0f
    private var mLastTime: Long = 0
    private var mShakeListener: OnShakeListener? = null
    private var mShakeCount = 0
    private var mLastShake: Long = 0
    private var mLastForce: Long = 0
    fun setOnShakeListener(listener: OnShakeListener?) {
        mShakeListener = listener
    }

    fun resume() {
        if (mSensorMgr != null) return
        val sensorMgr = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: throw UnsupportedOperationException("Accelerometer not supported")
        val supported = sensorMgr.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        if (!supported) {
            sensorMgr.unregisterListener(this)
            throw UnsupportedOperationException("Accelerometer not supported")
        }
        mSensorMgr = sensorMgr
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun pause() {
        mSensorMgr?.let {
            it.unregisterListener(this)
            mSensorMgr = null
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        val values = event.values
        val now = System.currentTimeMillis()
        if (now - mLastForce > SHAKE_TIMEOUT) {
            mShakeCount = 0
        }
        if (now - mLastTime > TIME_THRESHOLD) {
            val diff = now - mLastTime
            val speed =
                abs(values[0] + values[1] + values[2] - mLastX - mLastY - mLastZ) / diff * 10000
            val sensitivity = 2
            var forceThreshold = BASE_FORCE_THRESHOLD
            when (sensitivity) {
                0 -> forceThreshold = BASE_FORCE_THRESHOLD * 4
                1 -> forceThreshold = (BASE_FORCE_THRESHOLD * 2f).roundToInt()
            }
            if (speed > forceThreshold) {
                if (++mShakeCount >= SHAKE_COUNT && now - mLastShake > SHAKE_DURATION) {
                    mLastShake = now
                    mShakeCount = 0
                    mShakeListener?.onShake()
                }
                mLastForce = now
            }
            mLastTime = now
            mLastX = values[0]
            mLastY = values[1]
            mLastZ = values[2]
        }
    }

    interface OnShakeListener {
        fun onShake()
    }

    companion object {
        private const val BASE_FORCE_THRESHOLD = 350
        private const val TIME_THRESHOLD = 200
        private const val SHAKE_TIMEOUT = 500
        private const val SHAKE_DURATION = 1000
        private const val SHAKE_COUNT = 3
    }

}

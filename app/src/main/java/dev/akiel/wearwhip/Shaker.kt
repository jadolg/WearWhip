package dev.akiel.wearwhip

import android.content.Context
import android.hardware.SensorListener
import android.hardware.SensorManager

class Shaker(private val mContext: Context) : SensorListener {
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
        mSensorMgr = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (mSensorMgr == null) {
            throw UnsupportedOperationException("Sensors not supported")
        }
        val supported = mSensorMgr!!.registerListener(
            this,
            SensorManager.SENSOR_ACCELEROMETER,
            SensorManager.SENSOR_DELAY_GAME
        )
        if (!supported) {
            mSensorMgr!!.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER)
            throw UnsupportedOperationException("Accelerometer not supported")
        }
    }

    override fun onAccuracyChanged(sensor: Int, accuracy: Int) {}
    fun pause() {
        if (mSensorMgr != null) {
            mSensorMgr!!.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER)
            mSensorMgr = null
        }
    }

    override fun onSensorChanged(sensor: Int, values: FloatArray) {
        if (sensor != SensorManager.SENSOR_ACCELEROMETER) return
        val now = System.currentTimeMillis()
        if (now - mLastForce > SHAKE_TIMEOUT) {
            mShakeCount = 0
        }
        if (now - mLastTime > TIME_THRESHOLD) {
            val diff = now - mLastTime
            val speed =
                Math.abs(values[SensorManager.DATA_X] + values[SensorManager.DATA_Y] + values[SensorManager.DATA_Z] - mLastX - mLastY - mLastZ) / diff * 10000
            val sensitivity = 2
            var FORCE_THRESHOLD = BASE_FORCE_THRESHOLD
            when (sensitivity) {
                0 -> FORCE_THRESHOLD = BASE_FORCE_THRESHOLD * 4
                1 -> FORCE_THRESHOLD = Math.round((BASE_FORCE_THRESHOLD * 2).toFloat())
            }
            if (speed > FORCE_THRESHOLD) {
                if (++mShakeCount >= SHAKE_COUNT && now - mLastShake > SHAKE_DURATION) {
                    mLastShake = now
                    mShakeCount = 0
                    if (mShakeListener != null) {
                        mShakeListener!!.onShake()
                    }
                }
                mLastForce = now
            }
            mLastTime = now
            mLastX = values[SensorManager.DATA_X]
            mLastY = values[SensorManager.DATA_Y]
            mLastZ = values[SensorManager.DATA_Z]
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

    init {
        resume()
    }
}
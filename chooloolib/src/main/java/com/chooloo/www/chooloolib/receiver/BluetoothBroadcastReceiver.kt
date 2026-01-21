package com.chooloo.www.chooloolib.receiver

import android.bluetooth.BluetoothAdapter.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.MODE_IN_CALL
import android.media.AudioManager.MODE_NORMAL
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class BluetoothBroadcastReceiver : BroadcastReceiver() {
    
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReceiverEntryPoint {
        fun audioManager(): AudioManager
    }

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReceiverEntryPoint::class.java
        )
        val audioManager = entryPoint.audioManager()
        
        when (intent.action) {
            ACTION_STATE_CHANGED -> {
                when (intent.getIntExtra(EXTRA_STATE, ERROR)) {
                    STATE_CONNECTED -> {
                        audioManager.apply {
                            isBluetoothScoOn = false
                            stopBluetoothSco()
                            mode = MODE_NORMAL
                        }
                    }
                    else -> {
                        audioManager.apply {
                            isBluetoothScoOn = true
                            startBluetoothSco()
                            mode = MODE_IN_CALL
                        }
                    }
                }
            }
        }
    }
}
package com.chooloo.www.chooloolib.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chooloo.www.chooloolib.interactor.callaudio.CallAudiosInteractor
import com.chooloo.www.chooloolib.interactor.calls.CallsInteractor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class CallBroadcastReceiver : BroadcastReceiver() {
    
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReceiverEntryPoint {
        fun callsInteractor(): CallsInteractor
        fun callAudiosInteractor(): CallAudiosInteractor
    }

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReceiverEntryPoint::class.java
        )
        val calls = entryPoint.callsInteractor()
        val callAudios = entryPoint.callAudiosInteractor()
        
        when (intent.action) {
            ACTION_MUTE -> callAudios.isMuted = true
            ACTION_UNMUTE -> callAudios.isMuted = false
            ACTION_ANSWER -> calls.mainCall?.answer()
            ACTION_HANGUP -> calls.mainCall?.reject()
            ACTION_SPEAKER -> callAudios.isSpeakerOn = true
            ACTION_UNSPEAKER -> callAudios.isSpeakerOn = false
        }
    }

    companion object {
        const val ACTION_MUTE = "action_mute"
        const val ACTION_UNMUTE = "action_unmute"
        const val ACTION_ANSWER = "action_answer"
        const val ACTION_HANGUP = "action_hangup"
        const val ACTION_SPEAKER = "action_speaker"
        const val ACTION_UNSPEAKER = "action_unspeaker"
    }
}
package com.dhruv194.firebasewebrtc.service

import android.content.Context
import android.content.Intent
import android.os.Build
import javax.inject.Inject

class MainServiceRepository @Inject constructor(
    private val context: Context
) {
    //will be calling this way to start our service
    fun startService(username:String){
        Thread{ //start the service on a specific thread
            val intent = Intent(context, MainService::class.java)
            intent.putExtra("username", username)
            intent.action = MainServiceActions.START_SERVICE.name
            startServiceIntent(intent)
        }.start()
    }
    private fun startServiceIntent(intent: Intent){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            context.startForegroundService(intent)
        }else{
            context.startService(intent)
        }
    }

    fun setupViews(target: String, videoCall: Boolean, caller: Boolean) {
        val intent = Intent(context, MainService::class.java)
        intent.apply {
            action = MainServiceActions.SETUP_VIEWS.name
            putExtra("target",target)
            putExtra("isVideoCall", videoCall)
            putExtra("isCaller", caller)
        }
        startServiceIntent(intent)
    }

    fun sendEndCall() {
        val intent = Intent(context, MainService::class.java)
        intent.action = MainServiceActions.END_CALL.name
        startServiceIntent(intent)
    }

    fun switchCamera() {
        val intent = Intent(context, MainService::class.java)
        intent.action = MainServiceActions.SWITCH_CAMERA.name
        startServiceIntent(intent)
    }

    fun toggleAudio(shouldBeMuted: Boolean) {
        val intent = Intent(context, MainService::class.java)
        intent.action = MainServiceActions.TOGGLE_AUDIO.name
        intent.putExtra("shouldBeMuted", shouldBeMuted)
        startServiceIntent(intent)
    }

    fun toggleVideo(shouldBeMuted: Boolean) {
        val intent = Intent(context, MainService::class.java)
        intent.action = MainServiceActions.TOGGLE_VIDEO.name
        intent.putExtra("shouldBeMuted", shouldBeMuted)
        startServiceIntent(intent)
    }

    fun stopService() {
        val intent = Intent(context,MainService::class.java)
        intent.action = MainServiceActions.STOP_SERVICE.name
        startServiceIntent(intent)
    }
}
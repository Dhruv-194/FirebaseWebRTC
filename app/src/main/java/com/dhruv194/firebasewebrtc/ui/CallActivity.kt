package com.dhruv194.firebasewebrtc.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dhruv194.firebasewebrtc.R
import com.dhruv194.firebasewebrtc.databinding.ActivityCallBinding
import com.dhruv194.firebasewebrtc.service.MainService
import com.dhruv194.firebasewebrtc.service.MainServiceRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallActivity : AppCompatActivity(), MainService.EndCallListener {

    private var target: String ?=null
    private var isVideoCall: Boolean = true //is this a videoCall intent or a audioCall intent
    private var isCaller: Boolean = true //is the user coming here a caller or the receiver of the call

    private var isMicrophoneMuted = false //tracking state of mic
    private var isCameraMuted = false //tracking state of cam

    @Inject lateinit var serviceRepository: MainServiceRepository

    private lateinit var views: ActivityCallBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views = ActivityCallBinding.inflate(layoutInflater)
        setContentView(views.root)
        init()
    }
    private fun init(){
        intent.getStringExtra("target")?.let {
            this.target = it
        }?: kotlin.run {
            finish()
        }

        isVideoCall = intent.getBooleanExtra("isVideoCall", true)
        isCaller = intent.getBooleanExtra("isCaller", true)

        views.apply {
            callTitleTv.text = "In call with $target"
            if(!isVideoCall){ //if its audioCall
                toggleCameraButton.isVisible = false
                switchCameraButton.isVisible = false
            }

            MainService.remoteSurfaceView = remoteView
            MainService.localSurfaceView = localView

            serviceRepository.setupViews(target!!, isVideoCall, isCaller) //setting up or webRTC client according to the intent that we receive

            endCallButton.setOnClickListener {
                serviceRepository.sendEndCall()
            }

            switchCameraButton.setOnClickListener {
                serviceRepository.switchCamera()
            }
        }

        setupMicToggleClick()
        setupCamToggleClick()
        MainService.endCallListener = this
    }

    private fun setupMicToggleClick(){
        views.apply {
            toggleMicrophoneButton.setOnClickListener {
                if(!isMicrophoneMuted){
                    //we should mute our mute
                    //send a command to the repository
                    serviceRepository.toggleAudio(true)
                    //update the UI to mic is muted
                    toggleMicrophoneButton.setImageResource(R.drawable.ic_mic_on)
                }else {
                    //we should set it back to normal
                    //send a command to repository to make it back to normal status
                    serviceRepository.toggleAudio(false)
                    //update UI
                    toggleMicrophoneButton.setImageResource(R.drawable.ic_mic_off)
                }
                isMicrophoneMuted = !isMicrophoneMuted
            }
        }
    }

    private fun setupCamToggleClick(){
        views.apply {
            toggleCameraButton.setOnClickListener {
                if(!isCameraMuted){
                    //we should mute our mute
                    //send a command to the repository
                    serviceRepository.toggleVideo(true)
                    //update the UI to mic is muted
                    toggleCameraButton.setImageResource(R.drawable.ic_video_call)
                }else {
                    //we should set it back to normal
                    //send a command to repository to make it back to normal status
                    serviceRepository.toggleVideo(false)
                    //update UI
                    toggleCameraButton.setImageResource(R.drawable.ic_videocam_off)
                }
                isCameraMuted = !isCameraMuted
            }
        }
    }

    override fun onCallEnded() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        MainService.remoteSurfaceView?.release()
        MainService.remoteSurfaceView = null

        MainService.localSurfaceView?.release()
        MainService.localSurfaceView = null
    }
}
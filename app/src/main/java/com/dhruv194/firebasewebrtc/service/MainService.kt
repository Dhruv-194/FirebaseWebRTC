package com.dhruv194.firebasewebrtc.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dhruv194.firebasewebrtc.MainRepository.MainRepository
import com.dhruv194.firebasewebrtc.R
import com.dhruv194.firebasewebrtc.utils.DataModel
import com.dhruv194.firebasewebrtc.utils.DataModelType
import com.dhruv194.firebasewebrtc.utils.isValid
import dagger.hilt.android.AndroidEntryPoint
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@AndroidEntryPoint
class MainService : Service(), MainRepository.Listener {

    private var isServiceRunning = false
    private var username:String?=null

    @Inject lateinit var mainRepository: MainRepository

    private lateinit var notificationManager: NotificationManager
    private var isPreviousCallStateVideo = true

    companion object{
        var listener : Listener?=null
        var endCallListener: EndCallListener?=null
        var localSurfaceView: SurfaceViewRenderer?=null
        var remoteSurfaceView: SurfaceViewRenderer?=null
    }

    override fun onCreate() {
        super.onCreate()
        //instantiating the notification manager class
        notificationManager = getSystemService(
            NotificationManager::class.java
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { incomingIntent->
            when(incomingIntent.action){
                MainServiceActions.START_SERVICE.name->handleStartService(incomingIntent)
                MainServiceActions.SETUP_VIEWS.name ->handleViews(incomingIntent)
                MainServiceActions.END_CALL.name->handleEndCall()
                MainServiceActions.SWITCH_CAMERA.name->handeSwitchCamera()
                MainServiceActions.TOGGLE_AUDIO.name->handleToggleAudio(incomingIntent)
                MainServiceActions.TOGGLE_VIDEO.name->handleToggleVideo(incomingIntent)
                MainServiceActions.STOP_SERVICE.name -> handleStopService()

                else->Unit
            }

        }

        return START_STICKY
    }

    private fun handleStopService() {
        mainRepository.endCall()
        mainRepository.logOff {
            isServiceRunning = false
            stopSelf()
        }
    }

    private fun handleToggleVideo(incomingIntent: Intent) {
        val shouldBeMuted = incomingIntent.getBooleanExtra("shouldBeMuted",true)
        this.isPreviousCallStateVideo = !shouldBeMuted
        mainRepository.toggleVideo(shouldBeMuted)
    }

    private fun handleToggleAudio(incomingIntent: Intent) {
        val shouldBeMuted = incomingIntent.getBooleanExtra("shouldBeMuted",true)
        mainRepository.toggleAudio(shouldBeMuted)
    }

    private fun handeSwitchCamera() {
        mainRepository.switchCamera()
    }

    private fun handleEndCall() {
        //send a signal to other peer that call is ended
        mainRepository.sendEndCall()

        //end our call process & restart our webRTC client
        endCallAndRestartRepository()

    }

    private fun endCallAndRestartRepository(){
        mainRepository.endCall()
        endCallListener?.onCallEnded()
        mainRepository.initWebRTCClient(username!!)
    }

    private fun handleViews(incomingIntent: Intent) {
        val target = incomingIntent.getStringExtra("target")
        val isVideoCall = incomingIntent.getBooleanExtra("isVideoCall", true)
        val isCaller = incomingIntent.getBooleanExtra("isCaller", false)

        this.isPreviousCallStateVideo = isVideoCall
        mainRepository.setTarget(target!!)
        //initialize our widgets & start streaming our video & audio source
        //and get prepared for call
        mainRepository.initLocalSurfaceView(localSurfaceView!!, isVideoCall)
        mainRepository.initRemoteSurfaceView(remoteSurfaceView!!)
        if(!isCaller){
            //start the videoCall
            mainRepository.startCall()//start our whole WebRTC process

        }
    }

    private fun handleStartService(incomingIntent: Intent) {
        //start our foreground service
        if(!isServiceRunning){
            isServiceRunning = true
            username = incomingIntent.getStringExtra("username")
            startServiceWithNotification()

            //setup my clients
            mainRepository.listener = this
            mainRepository.initFirebase() //when we start our service, we can see if new event is coming or not
            mainRepository.initWebRTCClient(username!!)
        }
    }

    //starting our notification
    private fun startServiceWithNotification() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                "channel1", "foreground", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
            val notification = NotificationCompat.Builder(
                this, "channel1"
            ).setSmallIcon(R.mipmap.ic_launcher)

            startForeground(1, notification.build())
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onLatestEventReceived(dataModel: DataModel) {
        if(dataModel.isValid()){
            when(dataModel.type){
                DataModelType.StartVideoCall, DataModelType.StartAudioCall ->{
                    listener?.onCallReceived(dataModel)
                }
                else-> Unit
            }
        }
    }

    override fun endCall() {
        //we are receiving endCall from a remote peer
        endCallAndRestartRepository()
    }

    interface Listener{ //when the call event is coming and we need to receive it in another device
        fun onCallReceived(model:DataModel)

    }

    interface EndCallListener{
        fun onCallEnded()
    }
}
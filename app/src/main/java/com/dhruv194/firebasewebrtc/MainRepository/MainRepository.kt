package com.dhruv194.firebasewebrtc.MainRepository

import com.dhruv194.firebasewebrtc.firebaseClient.FirebaseClient
import com.dhruv194.firebasewebrtc.utils.DataModel
import com.dhruv194.firebasewebrtc.utils.DataModelType
import com.dhruv194.firebasewebrtc.utils.UserStatus
import com.dhruv194.firebasewebrtc.webrtc.MyPeerObserver
import com.dhruv194.firebasewebrtc.webrtc.WebRTCClient
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val firebaseClient: FirebaseClient,
    private val webRTCClient: WebRTCClient,
    private val gson: Gson
) : WebRTCClient.Listener {

    private var target:String?=null
    var listener : Listener?=null
    private var remoteView:SurfaceViewRenderer?=null

    fun login(username:String, password:String, isDone:(Boolean,String?)->Unit){
        firebaseClient.login(username,password,isDone) //isDone is a callback for response whether the user was logged in or not (bool) and the error response in String.
    }

    fun observeUserStatus(status: (List<Pair<String, String>>)-> Unit){
        firebaseClient.observeUserStatus(status)
    }

    fun initFirebase(){
        firebaseClient.subscribeForLatestEvent(object : FirebaseClient.Listener{
            override fun onLatestEventReceived(event: DataModel) {
                listener?.onLatestEventReceived(event) //using the listener we are notifying that here is the new event
                when(event.type){ //depending on the type we are going to interact with our WebRTC
                    DataModelType.Offer ->{
                        webRTCClient.onRemoteSessionReceived(
                            SessionDescription(
                                SessionDescription.Type.OFFER,
                                event.data.toString()
                            )
                        )
                        webRTCClient.answer(target!!)
                    }

                   DataModelType.Answer ->{
                        webRTCClient.onRemoteSessionReceived(
                            SessionDescription(
                                SessionDescription.Type.ANSWER,
                                event.data.toString()
                            )
                        )
                        webRTCClient.answer(target!!)
                    }

                    DataModelType.IceCandidates->{
                        val candidate:IceCandidate?= try {
                            gson.fromJson(event.data.toString(), IceCandidate::class.java)
                        }catch (e:Exception){
                            null
                        }
                        candidate?.let {
                            webRTCClient.addIceCandidateToPeer(it)
                        }
                    }

                    DataModelType.EndCall->{
                        listener?.endCall()
                    }

                    else->Unit
                }
            }

        })
    }

    fun sendConnectionRequest(target: String, isVideoCall:Boolean, success:(Boolean)->Unit){
            firebaseClient.sendMessageToOtherClients(
                DataModel(
                    type = if(isVideoCall)DataModelType.StartVideoCall else DataModelType.StartAudioCall,
                    target = target
                ), success
            )
    }

    fun setTarget(target: String) {
        this.target = target
    }

    //first I want to notify my MainService that is using MainRepository
    interface Listener{
        fun onLatestEventReceived(dataModel: DataModel)
        fun endCall()
    }

    fun initWebRTCClient(username: String){
        webRTCClient.listener = this
        webRTCClient.initializeWebRTCClient(username, object:MyPeerObserver(){
            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                //notify the creator of this class that a new stream is available
                try {
                    p0?.videoTracks?.get(0)?.addSink(remoteView)
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }

            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                p0?.let {
                    webRTCClient.sendIceCandidate(target!!, it)
                }
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
                if(newState==PeerConnection.PeerConnectionState.CONNECTED){
                    //change status to in-call
                    changeMyStatus(UserStatus.IN_CALL)

                    //clear latest event inside my user section in firebase database
                    firebaseClient.clearLatestEvent()
                }
            }
        })
    }

    fun initLocalSurfaceView(view:SurfaceViewRenderer, isVideoCall: Boolean){
        webRTCClient.initLocalSurfaceView(view, isVideoCall)
    }

    fun initRemoteSurfaceView(view: SurfaceViewRenderer){
        webRTCClient.initRemoteSurfaceView(view)
        this.remoteView = view
    }

    fun startCall(){
        webRTCClient.call(target!!)
    }

    fun endCall(){
        webRTCClient.closeConnection()
        changeMyStatus(UserStatus.ONLINE)
    }

    fun sendEndCall(){
        onTransferEventToSocket(
            DataModel(
                type = DataModelType.EndCall,
                target = target!!
            )
        )
    }

    private fun changeMyStatus(userStatus: UserStatus) {
        firebaseClient.changeMyStatus(userStatus)
    }

    fun toggleAudio(shouldBeMuted:Boolean){
        webRTCClient.toggleAudio(shouldBeMuted)
    }

    fun toggleVideo(shouldBeMuted:Boolean){
        webRTCClient.toggleVideo(shouldBeMuted)
    }

    fun switchCamera(){
        webRTCClient.switchCamera()
    }

    override fun onTransferEventToSocket(dataModel: DataModel) {
        firebaseClient.sendMessageToOtherClients(dataModel){}
    }

    fun logOff(function: () -> Unit) = firebaseClient.logOff(function)
}
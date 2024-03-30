package com.dhruv194.firebasewebrtc.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhruv194.firebasewebrtc.MainRepository.MainRepository
import com.dhruv194.firebasewebrtc.adapters.MyRVAdapter
import com.dhruv194.firebasewebrtc.databinding.ActivityMainBinding
import com.dhruv194.firebasewebrtc.service.MainService
import com.dhruv194.firebasewebrtc.service.MainServiceRepository
import com.dhruv194.firebasewebrtc.utils.DataModel
import com.dhruv194.firebasewebrtc.utils.DataModelType
import com.dhruv194.firebasewebrtc.utils.getCameraAndMicPermission
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MyRVAdapter.Listener, MainService.Listener {
    private lateinit var views : ActivityMainBinding
    private var username: String ?= null

    @Inject lateinit var mainRepository: MainRepository
    @Inject lateinit var mainServiceRepository: MainServiceRepository
    private var myAdapter : MyRVAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views = ActivityMainBinding.inflate(layoutInflater)
        setContentView(views.root)
        init()
    }
    private fun init(){
        username = intent.getStringExtra("username")
        if(username==null) finish()
        //observe other users status
        subscribeObservers()
        //start foreground service to listen to negotiation & calls
        startMyService()
    }

    private fun startMyService() {
        mainServiceRepository.startService(username!!)
    }

    private fun subscribeObservers() {
        setupRecyclerView()
        MainService.listener = this
        mainRepository.observeUserStatus{
            Log.d("status", "status - $it")
            myAdapter?.updateList(it)
        }
    }

    private fun setupRecyclerView(){
        myAdapter = MyRVAdapter(this)
        val layoutManager = LinearLayoutManager(this)
        views.mainRecyclerView.apply {
            setLayoutManager(layoutManager)
            adapter = myAdapter
        }
    }

    override fun onViewCallClicked(username: String) {
    //checking permission from our created extension func
        getCameraAndMicPermission {
            mainRepository.sendConnectionRequest(username, true){
                if(it){
                    //start video call
                    //create an intent to move to call activity
                    Toast.makeText(this@MainActivity, "moving to videocall activity", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, CallActivity::class.java)
                    intent.putExtra("target", username)
                    intent.putExtra("isVideoCall", true)
                    intent.putExtra("isCaller", true)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onAudioCallClicked(username: String) {
        //checking permission from our created extension func
        getCameraAndMicPermission {
            mainRepository.sendConnectionRequest(username, false){
                if(it){
                    //start audio call
                    //create an intent to move to call activity
                    val intent = Intent(this@MainActivity, CallActivity::class.java)
                    intent.putExtra("target", username)
                    intent.putExtra("isVideoCall", false)
                    intent.putExtra("isCaller", true) //but still a caller because we are clicking this method through our RV button
                    startActivity(intent)
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mainServiceRepository.stopService()
    }

    override fun onCallReceived(model: DataModel) {
        runOnUiThread {
            views.apply {
                val isVideoCall = model.type == DataModelType.StartVideoCall //if true its videocall else if false it is audiocall
                val isVideoCallText = if(isVideoCall) "Video" else "Audio"
                incomingCallTitleTv.text = model.sender+"is"+isVideoCallText+"calling you"
                incomingCallLayout.isVisible = true

                acceptButton.setOnClickListener {
                    getCameraAndMicPermission {
                        incomingCallLayout.isVisible = false
                        //create an intent to videocall activity
                        Toast.makeText(this@MainActivity, "moving to videocall activity", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, CallActivity::class.java)
                        intent.putExtra("target", model.sender)
                        intent.putExtra("isVideoCall", isVideoCall)
                        intent.putExtra("isCaller", false)
                        startActivity(intent)
                    }
                }
                declineButton.setOnClickListener {
                    incomingCallLayout.isVisible = false
                }
            }
        }
    }
}
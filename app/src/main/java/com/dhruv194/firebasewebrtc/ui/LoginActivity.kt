package com.dhruv194.firebasewebrtc.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dhruv194.firebasewebrtc.MainRepository.MainRepository
import com.dhruv194.firebasewebrtc.R
import com.dhruv194.firebasewebrtc.databinding.ActivityLoginBinding
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var views : ActivityLoginBinding
    @Inject lateinit var mainRepository: MainRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(views.root)
        init()
    }

    private fun init(){
        views.apply {
            btn.setOnClickListener{
                mainRepository.login( //calling the login fun from mainRepo
                    username =  usernameEt.text.toString(),
                    password =  passwordEt.text.toString()
                ){ isDone, reason -> //doing the callback
                    if(!isDone){ //if 'false' in callback then show the error
                        Toast.makeText(this@LoginActivity, reason, Toast.LENGTH_SHORT).show()
                    }else{
                        //start a new activity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("username", usernameEt.text.toString())
                        startActivity(intent)
                    }

                }
            }
        }
    }
}
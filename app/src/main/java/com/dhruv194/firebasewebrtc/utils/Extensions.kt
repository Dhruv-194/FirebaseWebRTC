package com.dhruv194.firebasewebrtc.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX
import java.security.Permission

//creating an extension function to get permission
fun AppCompatActivity.getCameraAndMicPermission(success:()->Unit){
    PermissionX.init(this)
        .permissions(android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO)
        .request{ allGranted, _ ,_ ->

            if(allGranted){
                success()
            }else{
                Toast.makeText(this,"Camera & Mic permission is required", Toast.LENGTH_SHORT).show()
            }

        }
}
package com.dhruv194.firebasewebrtc.utils

import java.sql.Timestamp

enum class DataModelType{
    StartAudioCall,
    StartVideoCall,
    Offer,
    Answer,
    IceCandidates,
    EndCall
}

data class DataModel(
    val sender:String?=null,
    val target:String,
    val type:DataModelType,
    val data:String?=null,
    val timestamp: Long = System.currentTimeMillis()
)
//creating an extension function for our data model to check its validation
fun DataModel.isValid():Boolean{
    return System.currentTimeMillis() - this.timestamp < 60000 //if it passed more than 1min then we dont use this event
}

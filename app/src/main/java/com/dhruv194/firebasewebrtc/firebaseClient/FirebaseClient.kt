package com.dhruv194.firebasewebrtc.firebaseClient

import com.dhruv194.firebasewebrtc.utils.DataModel
import com.dhruv194.firebasewebrtc.utils.FirebaseMenu.LATEST_EVENT
import com.dhruv194.firebasewebrtc.utils.FirebaseMenu.PASSWORD
import com.dhruv194.firebasewebrtc.utils.FirebaseMenu.STATUS
import com.dhruv194.firebasewebrtc.utils.MyEventListener
import com.dhruv194.firebasewebrtc.utils.UserStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.values
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseClient @Inject constructor(
    private val dbRef: DatabaseReference,
    private val gson: Gson
){

    private var currentUsername:String? = null
    private fun setUsername(username:String){
        this.currentUsername = username
    }
    //to check the password and then logging in the user
    fun login(username: String, password: String, done: (Boolean, String?) -> Unit) {
        dbRef.addListenerForSingleValueEvent(object: MyEventListener(){
            override fun onDataChange(snapshot: DataSnapshot) {
                //if the current user with this username exists
                if(snapshot.hasChild(username)){
                    //if the user exisits get the password from the firebase db mapped to this user.
                    val userPassowrd = snapshot.child(username).child(PASSWORD).value

                    if(userPassowrd == password){
                        //compare the two passwords and see whether they are same or not, if same then login-in
                        dbRef.child(username).child(STATUS).setValue(UserStatus.ONLINE)
                            .addOnCompleteListener{
                                setUsername(username)
                                done(true, null)
                            }.addOnFailureListener {
                                done(false, "${it.message}")
                            }
                    }else {
                        //password does not match, notify user
                            done(false, "Password is wrong")
                    }

                }else { //user does not exists
                    dbRef.child(username).child(PASSWORD).setValue(password)
                        .addOnCompleteListener {
                            dbRef.child(username).child(STATUS).setValue(UserStatus.ONLINE)
                                .addOnCompleteListener{
                                    setUsername(username)
                                    done(true, null)
                                }.addOnFailureListener {
                                    done(false, "${it.message}")
                                }
                        }
                        .addOnFailureListener {
                            done(false, "${it.message}")
                        }
                }
            }

        })
    }

    fun observeUserStatus(status: (List<Pair<String, String>>) -> Unit) {
        dbRef.addValueEventListener(object : MyEventListener(){
            override fun onDataChange(snapshot: DataSnapshot) {
                //creating a list of users with a map of username->status(ONLINE,OFFLINE)
                val list = snapshot.children.filter { it.key != currentUsername }.map {
                    it.key!! to it.child(STATUS).value.toString()
                }
                status(list)
            }



        })
    }

    //functions to interact with other Firebase members

    fun subscribeForLatestEvent(listener:Listener){
        try {
            dbRef.child(currentUsername!!).child(LATEST_EVENT).addValueEventListener(object : MyEventListener(){
                override fun onDataChange(snapshot: DataSnapshot) {
                    super.onDataChange(snapshot)
                    val event = try {
                        gson.fromJson(snapshot.value.toString(), DataModel::class.java)
                    }catch (e:java.lang.Exception){
                            e.printStackTrace()
                            null
                    }
                    event?.let {
                        listener.onLatestEventReceived(it)
                    }
                }



            })
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun sendMessageToOtherClients(message:DataModel, success:(Boolean)->Unit){
        val convertedMessage = gson.toJson(message.copy(sender = currentUsername))
        dbRef.child(message.target).child(LATEST_EVENT).setValue(convertedMessage)
            .addOnCompleteListener {
                success(true)
            }.addOnFailureListener {
                success(false)
            }
    }

    fun changeMyStatus(userStatus: UserStatus) {
        dbRef.child(currentUsername!!).child(STATUS).setValue(userStatus.name)
    }

    fun clearLatestEvent() {
        dbRef.child(currentUsername!!).child(LATEST_EVENT).setValue(null)
    }

    fun logOff(function:()->Unit) {
        dbRef.child(currentUsername!!).child(STATUS).setValue(UserStatus.OFFLINE)
            .addOnCompleteListener { function() }
    }

    interface Listener{
        fun onLatestEventReceived(event:DataModel)
    }
}
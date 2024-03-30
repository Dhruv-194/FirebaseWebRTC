package com.dhruv194.firebasewebrtc.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.dhruv194.firebasewebrtc.R
import com.dhruv194.firebasewebrtc.databinding.ItemMainRecyclerViewBinding

class MyRVAdapter(private val listener: Listener ) : RecyclerView.Adapter<MyRVAdapter.MyRVViewHolder>() {
    class MyRVViewHolder(private val binding: ItemMainRecyclerViewBinding): RecyclerView.ViewHolder(binding.root){
        private val context  = binding.root.context

        fun bind(
            user:Pair<String, String>,
            videoCallClicked:(String)-> Unit,
            audioCallClicked:(String)->Unit
        ){
            binding.apply {
                when (user.second) {
                    "ONLINE" -> {
                        videoCallBtn.isVisible = true
                        audioCallBtn.isVisible = true
                        videoCallBtn.setOnClickListener {
                            videoCallClicked.invoke(user.first)
                        }
                        audioCallBtn.setOnClickListener {
                            audioCallClicked.invoke(user.first)
                        }
                        statusTv.setTextColor(context.resources.getColor(R.color.light_green, null))
                        statusTv.text = "Online"
                    }
                    "OFFLINE" -> {
                        videoCallBtn.isVisible = false
                        audioCallBtn.isVisible = false
                        statusTv.setTextColor(context.resources.getColor(R.color.white, null))
                        statusTv.text = "Offline"
                    }
                    "IN_CALL" -> {
                        videoCallBtn.isVisible = false
                        audioCallBtn.isVisible = false
                        statusTv.setTextColor(context.resources.getColor(R.color.red, null))
                        statusTv.text = "In Call"
                    }
                }

                usernameTv.text = user.first
            }
        }
    }

    private var userList: List<Pair<String, String>>?=null
    fun updateList(list:List<Pair<String,String>>){
        this.userList = list
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRVViewHolder {
        val binding = ItemMainRecyclerViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyRVViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList?.size?:0
    }

    override fun onBindViewHolder(holder: MyRVViewHolder, position: Int) {
        userList?.let{ list->
        val user = list[position]
            holder.bind(user, {
                listener.onViewCallClicked(it)
            },{
                listener.onAudioCallClicked(it)
            })

        }
    }

    interface Listener{
        fun onViewCallClicked(username:String)
        fun onAudioCallClicked(username:String)
    }
}
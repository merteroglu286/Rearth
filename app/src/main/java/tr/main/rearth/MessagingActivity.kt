package tr.main.rearth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tr.main.rearth.Activities.UserInfoActivity
import tr.main.rearth.Adapters.ChatAdapter
import tr.main.rearth.Utils.AppUtil
import tr.main.rearth.ViewModels.ProfileViewModel
import tr.main.rearth.databinding.ActivityMessagingBinding
import com.google.firebase.database.*
import com.onesignal.OneSignal
import com.onesignal.OneSignal.PostNotificationResponseHandler
import org.json.JSONException
import org.json.JSONObject


class MessagingActivity : AppCompatActivity() {

    private lateinit var activityMessageBinding: ActivityMessagingBinding
    private lateinit var profileViewModels: ProfileViewModel
    private var hisId : String? =  null
    private var hisName : String? =  null
    private var hisImage : String? =  null
    private var myToken : String? =  null
    private var hisToken : String? =  null
    private lateinit var appUtil : AppUtil
    private lateinit var myId : String
    private lateinit var myName : String
    private lateinit var myImage : String
    private lateinit var sharedPreferences: SharedPreferences
    var chatList = ArrayList<ChatModel>()

    private var lastMessage: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMessageBinding = ActivityMessagingBinding.inflate(layoutInflater)
        setContentView(activityMessageBinding.root)

        appUtil = AppUtil()
        myId = appUtil.getUID()!!
        sharedPreferences = getSharedPreferences("Messages", MODE_PRIVATE)
        myImage = sharedPreferences.getString("myImage","").toString()

/*
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null && !TextUtils.isEmpty(task.result)) {
                        val token: String = task.result!!
                        myToken = token
                    }
                }
            }


 */
        activityMessageBinding.messageRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        //activityMessageBinding.activity = this

        hisId = intent.getStringExtra("hisId")
        hisImage = intent.getStringExtra("hisImage")
        hisName = intent.getStringExtra("hisName")


        profileViewModels = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(
            ProfileViewModel::class.java)

        profileViewModels.getUser().observe(this, androidx.lifecycle.Observer { userModel->
            myName = userModel.username
            myImage = userModel.image
        })


        getUserData(hisId)

        activityMessageBinding.btnSend.setOnClickListener {
            var message:String = activityMessageBinding.msgText.text.toString()
            if(message.isEmpty()){

            }else{
                sendNotification(message,hisToken.toString())
                var reference = FirebaseDatabase.getInstance().getReference("Chat").child(myId.toString()).child(hisId.toString())

                val map = mapOf(
                    "senderId" to myId.toString(),
                    "receiverId" to hisId!!.toString(),
                    "message" to message.toString(),
                    "date" to System.currentTimeMillis().toString(),
                )
                MessageModel(myId.toString(),hisId!!.toString(),message.toString(),System.currentTimeMillis().toString())
                reference.push().setValue(map).addOnSuccessListener {
                    FirebaseDatabase.getInstance().getReference("Chat").child(hisId!!.toString()).child(myId.toString()).push().setValue(map)
                }


/*
                var hashMap: HashMap<String,String> = HashMap()
                hashMap.put("senderId",myId.toString())
                hashMap.put("receiverId",hisId!!.toString())
                hashMap.put("message",message.toString())
                hashMap.put("date",System.currentTimeMillis().toString())
                reference!!.child("Chat").push().setValue(hashMap)
 */
                    .addOnSuccessListener {


                        var referenceConversationMy = FirebaseDatabase.getInstance().getReference("Conversations")

                        val map = mapOf(
                            "lastMessage" to lastMessage.toString(),
                            "receiverId" to hisId.toString(),
                            "receiverImage" to hisImage.toString(),
                            "receiverName" to hisName.toString(),
                            "senderId" to myId.toString(),
                            "date" to System.currentTimeMillis().toString(),
                            "okunduMu" to true
                        )
                        ConversationsModel(lastMessage,hisId.toString(),hisImage.toString(),hisName.toString(),myId,System.currentTimeMillis().toString(),true)
                        referenceConversationMy.child(myId).child(hisId!!).updateChildren(map).addOnSuccessListener {
                            var referenceConversationHis = FirebaseDatabase.getInstance().getReference("Conversations")

                            val map = mapOf(
                                "lastMessage" to lastMessage.toString(),
                                "receiverId" to myId.toString(),
                                "receiverImage" to myImage.toString(),
                                "receiverName" to myName.toString(),
                                "senderId" to hisId.toString(),
                                "date" to System.currentTimeMillis().toString(),
                                "okunduMu" to false
                            )
                            ConversationsModel(lastMessage.toString(),myId.toString(),myImage.toString(),myName.toString(),hisId!!.toString(),System.currentTimeMillis().toString(),false)
                            referenceConversationHis.child(hisId!!).child(myId).updateChildren(map)
                        }
                    }


                activityMessageBinding.msgText.setText("")

            }

        }



        /*

        activityMessageBinding.btnSend.setOnClickListener {
            val message = activityMessageBinding.msgText.text.toString()
            if(message.isEmpty()){
                Toast.makeText(this,"Mesajınınızı giriniz",Toast.LENGTH_SHORT).show()
            }else{
                sendMessage(message)
            }
        }

        if(chatId != null){
            checkChat(hisId!!)
        }

         */

        activityMessageBinding.btnBack.setOnClickListener {
            finish()
        }

        activityMessageBinding.imgProfile.setOnClickListener {
            val intent = Intent(it.context, UserInfoActivity::class.java)
            intent.putExtra("hisId",hisId.toString())
            intent.putExtra("hisImage",hisImage.toString())
            intent.putExtra("hisName",hisName.toString())
            it.context.startActivity(intent)
        }

        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Chat").child(myId.toString()).child(hisId.toString())

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(ChatModel::class.java)

                    if (chat!!.senderId.equals(myId.toString()) && chat!!.receiverId.equals(hisId.toString()) ||
                        chat!!.senderId.equals(hisId.toString()) && chat!!.receiverId.equals(myId.toString())
                    ) {
                        chatList.add(chat)

                    }
                    lastMessage = chat.message
                }
                val chatAdapter = ChatAdapter(
                    this@MessagingActivity,
                    chatList,
                    activityMessageBinding.messageRecyclerView
                )

                activityMessageBinding.messageRecyclerView.adapter = chatAdapter
                activityMessageBinding.messageRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)


            }
        })
        (activityMessageBinding.messageRecyclerView.layoutManager as LinearLayoutManager).stackFromEnd = true



    }

    private fun getUserData(userId: String?) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    activityMessageBinding.userModel = userModel
                    if (userModel?.status == "online"){
                        activityMessageBinding.status.visibility = View.VISIBLE
                    }else{
                        activityMessageBinding.status.visibility = View.GONE
                    }
                    hisToken = userModel!!.token
                    //Picasso.get().load(userModel!!.image).into(activityMessageBinding.imgProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun sendNotification(message:String,hisToken:String){
        try {
            OneSignal.postNotification(JSONObject("{'contents': {'en':'${message}'}, 'include_player_ids': ['" + hisToken + "']}"),
                object : PostNotificationResponseHandler {
                    override fun onSuccess(response: JSONObject) {
                        Log.i("OneSignalExample", "postNotification Success: $response")
                    }

                    override fun onFailure(response: JSONObject) {
                        Log.e("OneSignalExample", "postNotification Failure: $response")
                    }
                })

        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    
}

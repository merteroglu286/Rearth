package tr.main.rearth.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import tr.main.rearth.Activities.UserInfoActivity
import tr.main.rearth.FollowRequestModel
import tr.main.rearth.R
import tr.main.rearth.UserModel
import tr.main.rearth.databinding.ItemFollowRequestBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FollowRequestsAdapter(val requestList: MutableList<FollowRequestModel>, context: Context) : RecyclerView.Adapter<FollowRequestsAdapter.FollowRequestsViewHolder>() {

    val context = context
    var receiverFollowers : String = ""
    var receiverFollowing : String = ""
    var senderFollowers : String = ""
    var senderFollowing : String = ""

    class FollowRequestsViewHolder(val view: ItemFollowRequestBinding): RecyclerView.ViewHolder(view.root) {

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowRequestsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<ItemFollowRequestBinding>(inflater,
            R.layout.item_follow_request,parent,false)
        return FollowRequestsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return requestList.size
    }

    override fun onBindViewHolder(holder: FollowRequestsViewHolder, position: Int) {
        val requests = requestList[position]
        holder.view.requestModel = requests

        holder.itemView.setOnClickListener{
            val intent = Intent(it.context, UserInfoActivity::class.java)
            intent.putExtra("hisId",requests.senderId)
            intent.putExtra("hisImage",requests.senderImage)
            intent.putExtra("hisName",requests.senderName)
            it.context.startActivity(intent)
        }


        holder.view.btnToAccept.setOnClickListener {

            var referenceUsers =
                FirebaseDatabase.getInstance().getReference("Users")

            var reference =
                FirebaseDatabase.getInstance().getReference("Followers")

            val map = mapOf(
                "onaylandiMi" to true
            )
            reference.child(requests.receiverId).child(requests.senderId).updateChildren(map)



            val databaseReferenceReceiver = FirebaseDatabase.getInstance().getReference("Users").child(requests.receiverId)
            databaseReferenceReceiver.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userModel = snapshot.getValue(UserModel::class.java)
                        if (userModel != null) {
                            receiverFollowers = (userModel.followers.toInt() + 1).toString()
                            receiverFollowing = (userModel.following.toInt() + 1).toString()
                            val map2 = mapOf(
                                "followers" to receiverFollowers
                            )
                            referenceUsers.child(requests.receiverId).updateChildren(map2)
                        }
                        //Picasso.get().load(userModel!!.image).into(activityMessageBinding.imgProfile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

            val databaseReferenceSender = FirebaseDatabase.getInstance().getReference("Users").child(requests.senderId)
            databaseReferenceSender.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userModel = snapshot.getValue(UserModel::class.java)
                        if (userModel != null) {
                            senderFollowers = (userModel.followers.toInt() + 1).toString()
                            senderFollowing = (userModel.following.toInt() + 1).toString()
                            val map2 = mapOf(
                                "following" to senderFollowing
                            )
                            referenceUsers.child(requests.senderId).updateChildren(map2)
                        }
                        //Picasso.get().load(userModel!!.image).into(activityMessageBinding.imgProfile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            /*
            val map3 = mapOf(
                "following" to "1"
            )
            referenceSender.child(requests.senderId).updateChildren(map3)

             */
        }

        holder.view.btnToReject.setOnClickListener {
            var reference =
                FirebaseDatabase.getInstance().getReference("Followers").child(requests.receiverId).child(requests.senderId)
            reference.removeValue()
        }
    }

    fun updateNoticesList(newRequestList:List<FollowRequestModel>){
        requestList.clear()
        requestList.addAll(newRequestList)
        notifyDataSetChanged()
    }

    private fun getReceiverData(userId: String?) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    if (userModel != null) {
                        receiverFollowers = userModel.followers
                        receiverFollowing = userModel.following
                    }
                    //Picasso.get().load(userModel!!.image).into(activityMessageBinding.imgProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun getSenderData(userId: String?) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    if (userModel != null) {
                        senderFollowers = userModel.followers
                        senderFollowing = userModel.following
                    }
                    //Picasso.get().load(userModel!!.image).into(activityMessageBinding.imgProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}

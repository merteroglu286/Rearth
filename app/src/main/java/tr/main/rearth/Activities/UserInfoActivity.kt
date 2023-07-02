package tr.main.rearth.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import tr.main.rearth.*
import tr.main.rearth.ViewModels.ProfileViewModel
import tr.main.rearth.databinding.ActivityUserInfoBinding
import com.google.firebase.database.*

class UserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserInfoBinding
    private lateinit var profileViewModels: ProfileViewModel
    private var hisId: String? = null
    private var hisImage: String? = null
    private var hisName: String? = null
    private var hisToken: String? = null
    private lateinit var myId : String
    private lateinit var myName : String
    private lateinit var myImage : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileViewModels =
            ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(
                ProfileViewModel::class.java
            )

        profileViewModels.getUser().observe(this, androidx.lifecycle.Observer { userModel ->
            myName = userModel.username
            myImage = userModel.image
            myId = userModel.uid
            getMessagingRequest(myId,hisId!!)
        })

        hisId = intent.getStringExtra("hisId")
        hisImage = intent.getStringExtra("hisImage")
        hisName = intent.getStringExtra("hisName")
        getUserData(hisId)

        Glide.with(this).load(hisImage).into(binding.imgProfile)
        binding.userName.text = hisName.toString()


        binding.btnMessage.setOnClickListener {
            val intent = Intent(it.context, MessagingActivity::class.java)
            intent.putExtra("hisId", hisId.toString())
            intent.putExtra("hisImage", hisImage.toString())
            intent.putExtra("hisName", hisName.toString())
            it.context.startActivity(intent)
        }

        binding.imgProfile.setOnClickListener {
            val intent = Intent(it.context, FullscreenPhotoActivity::class.java)
            intent.putExtra("img", hisImage)
            it.context.startActivity(intent)
        }

        binding.btnFollowed.setOnClickListener {
            //binding.linearButtons.visibility = View.VISIBLE
            //binding.btnFollowed.visibility = View.INVISIBLE
            binding.btnFollowed.text = "Takip isteği gönderildi ✓"
            var reference =
                FirebaseDatabase.getInstance().getReference("Followers")

            val map = mapOf(
                "senderId" to myId,
                "receiverId" to hisId.toString(),
                "senderImage" to myImage,
                "receiverImage" to hisImage,
                "senderName" to myName,
                "receiverName" to hisName,
                "date" to System.currentTimeMillis().toString(),
                "onaylandiMi" to false
            )
            reference.child(hisId!!).child(myId).updateChildren(map)

                /*.addOnSuccessListener {
                    var referenceConversationHis =
                        FirebaseDatabase.getInstance().getReference("MessagingRequests")

                    val map = mapOf(
                        "receiverId" to myId.toString(),
                        "receiverImage" to myImage.toString(),
                        "receiverName" to myName.toString(),
                        "senderId" to hisId.toString(),
                        "date" to System.currentTimeMillis().toString(),
                        "onaylandıMı" to false
                    )
                    referenceConversationHis.child(hisId!!).child(myId).updateChildren(map)
                }

                 */


        }

    }
    private fun getUserData(userId: String?) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    binding.userName.text = userModel!!.username
                    hisToken = userModel.token
                    //Picasso.get().load(userModel!!.image).into(activityUserInfoBinding.imgProfile)
                    Glide.with(applicationContext).load(userModel!!.image).into(binding.imgProfile)
                    binding.countOfFollowers.text = userModel.followers
                    binding.countOfFollowing.text = userModel.following
                    //binding.countOfNotices.text = userModel.notices
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun getMessagingRequest(userId: String,hisId: String) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Followers").child(hisId).child(userId)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val data = snapshot.getValue(FollowRequestModel::class.java)
                    if (data!!.onaylandiMi == true){
                        binding.btnFollowed.visibility = View.GONE
                        binding.linearButtons.visibility = View.VISIBLE
                    }else{
                        binding.btnFollowed.visibility = View.VISIBLE
                        binding.linearButtons.visibility = View.INVISIBLE
                        binding.btnFollowed.text = "Takip isteği gönderildi ✓"
                    }
                }else{
                    binding.btnFollowed.text = "Takip isteği gönder"
                    binding.btnFollowed.visibility = View.VISIBLE
                    binding.linearButtons.visibility = View.INVISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

}
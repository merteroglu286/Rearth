package tr.main.rearth.Dashboard

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import tr.main.rearth.Activities.FollowActivity
import tr.main.rearth.Activities.FullscreenPhotoActivity
import tr.main.rearth.Activities.SettingsActivity
import tr.main.rearth.UserModel
import tr.main.rearth.ViewModels.ProfileViewModel
import tr.main.rearth.databinding.FragmentProfileBinding
import com.google.firebase.database.*

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModels: ProfileViewModel
    private lateinit var imageUrl: String
    private lateinit var userId: String
    private  var onaylandiMi: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        //getUserData(FirebaseAuth.getInstance().uid)

        profileViewModels = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity()!!.application).create(
            ProfileViewModel::class.java)

        profileViewModels.getUser().observe(viewLifecycleOwner, Observer {  userModel ->
            binding.userModel = userModel
            imageUrl = userModel.image
        })


        binding.btnSettings.setOnClickListener {
            startActivity(Intent(context, SettingsActivity::class.java))
        }
        binding.imgProfile.setOnClickListener {
            val intent = Intent(it.context, FullscreenPhotoActivity::class.java)
            intent.putExtra("img",imageUrl)
            it.context.startActivity(intent)
        }
        binding.countFollowers.setOnClickListener {
            val intent = Intent(it.context, FollowActivity::class.java)
            intent.putExtra("TYPE",0)
            startActivity(intent)
        }
        binding.countOfFollowing.setOnClickListener {
            val intent = Intent(it.context, FollowActivity::class.java)
            intent.putExtra("TYPE",1)
            startActivity(intent)
        }

        return binding.root
    }

    private fun getUserData(userId: String?) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    binding.userName.text = userModel!!.username
                    Glide.with(context!!).load(userModel.image).into(binding.imgProfile)
                    //Picasso.get().load(userModel!!.image).into(activityMessageBinding.imgProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}
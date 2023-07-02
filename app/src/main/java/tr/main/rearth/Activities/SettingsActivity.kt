package tr.main.rearth.Activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import tr.main.rearth.Constants.AppConstants
import tr.main.rearth.ConversationsModel
import tr.main.rearth.Permissions.AppPermission
import tr.main.rearth.ViewModels.NoticeViewModel
import tr.main.rearth.ViewModels.ProfileViewModel
import tr.main.rearth.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var profileViewModels: ProfileViewModel
    private lateinit var noticesViewModel : NoticeViewModel
    private lateinit var appPermission: AppPermission

    private var databaseReference: DatabaseReference? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var storageReference: StorageReference? = null

    private lateinit var uid: String
    private var image: Uri? = null
    private lateinit var imageUrl: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPermission = AppPermission()
        profileViewModels = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(
            ProfileViewModel::class.java)

        noticesViewModel = ViewModelProviders.of(this).get(NoticeViewModel::class.java)
        noticesViewModel.getDataFromFirebase()
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        profileViewModels.getUser().observe(this, Observer {  userModel ->
            binding.userModel = userModel
        })
        val userModel = profileViewModels.getUser().value
        Glide.with(this).load(userModel!!.image).into(binding.imgUser)

        binding.logOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }



        binding.imgPickImage.setOnClickListener {
            if (checkStoragePermission())
                pickImage()
            else storageRequestPermission()
        }


        binding.profileDuzenleKaydet.setOnClickListener {
            var kullaniciAdi: String = binding.editUserName.text.toString()
            var cinsiyet: String = binding.editUserGender.text.toString()
            var dogumTarihi: String = binding.editUserBirthday.text.toString()

            if (storageReference != null){
                if (image != null){
                    noticesViewModel.noticesLiveData.observe(this, Observer { notices ->
                        notices?.let {
                            for (notice in notices){
                                if (notice.userID == firebaseAuth!!.currentUser!!.uid){

                                    storageReference!!.child(firebaseAuth!!.uid + AppConstants.PATH).putFile(image!!)
                                        .addOnSuccessListener {
                                            val task = it.storage.downloadUrl
                                            task.addOnCompleteListener { uri ->
                                                imageUrl = uri.result.toString()
                                                uid = firebaseAuth!!.uid!!.toString()
                                                val map = mapOf(
                                                    "image" to imageUrl,
                                                )
                                                databaseReference!!.child(firebaseAuth!!.uid!!).updateChildren(map)

                                                val ref = FirebaseDatabase.getInstance().reference.child("Notices").child(notice.noticeID.toString())
                                                val map1 = mapOf(
                                                    "userImage" to imageUrl,
                                                )
                                                ref.updateChildren(map1)

                                                val databaseReference = FirebaseDatabase.getInstance().getReference("Conversations")

                                                databaseReference.addValueEventListener(object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        for (conversationSnapshot in snapshot.children) {
                                                            for (userSnapshot in conversationSnapshot.children) {
                                                                val conversation = userSnapshot.getValue(ConversationsModel::class.java)
                                                                conversation?.let {
                                                                    if (conversation.receiverId == firebaseAuth!!.currentUser!!.uid) {
                                                                        val map = mapOf(
                                                                            "receiverImage" to imageUrl,
                                                                        )
                                                                        databaseReference.child(conversation.senderId).child(conversation.receiverId).updateChildren(map)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {
                                                        // Hata durumunda yapılacak işlemler burada
                                                    }
                                                })
                                            }
                                        }
                                }
                            }
                        }
                    })

                }

            }

            val map = mapOf(
                "username" to kullaniciAdi,
                "gender" to cinsiyet,
                "birthday" to dogumTarihi,
            )
            databaseReference!!.child(firebaseAuth!!.uid!!).updateChildren(map)

            noticesViewModel.noticesLiveData.observe(this, Observer { notices ->
                notices?.let {
                    for (notice in notices){
                        if (notice.userID == firebaseAuth!!.currentUser!!.uid){
                            val ref = FirebaseDatabase.getInstance().reference.child("Notices").child(notice.noticeID.toString())
                            val map = mapOf(
                                "username" to kullaniciAdi,
                            )
                            ref.updateChildren(map)
                        }
                    }
                }
            })

            val databaseReference = FirebaseDatabase.getInstance().getReference("Conversations")

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (conversationSnapshot in snapshot.children) {
                        for (userSnapshot in conversationSnapshot.children) {
                            val conversation = userSnapshot.getValue(ConversationsModel::class.java)
                            conversation?.let {
                                if (conversation.receiverId == firebaseAuth!!.currentUser!!.uid) {
                                    val map = mapOf(
                                        "receiverName" to kullaniciAdi,
                                    )
                                    databaseReference.child(conversation.senderId).child(conversation.receiverId).updateChildren(map)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Hata durumunda yapılacak işlemler burada
                }
            })


            finish()
        }

    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun storageRequestPermission() = ActivityCompat.requestPermissions(
        this,
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), 1000
    )

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    pickImage()
                else Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    image = result.uri
                    binding.imgUser.setImageURI(image)
                }
            }
        }
    }

    private fun pickImage() {
        CropImage.activity()
            .setCropMenuCropButtonTitle(resources.getString(tr.main.rearth.R.string.crop_image_save_ok))
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(this)
    }

    private fun loading(isLoading:Boolean){

        if(isLoading){
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.GONE
        }
    }
}
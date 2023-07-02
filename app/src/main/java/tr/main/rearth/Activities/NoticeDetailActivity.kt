package tr.main.rearth.Activities

import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import tr.main.rearth.*
import tr.main.rearth.Adapters.NoticeCommentsAdapter
import tr.main.rearth.ViewModels.NoticeViewModel
import tr.main.rearth.ViewModels.ProfileViewModel
import tr.main.rearth.databinding.ActivityNoticeDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_notices.*
import tr.main.rearth.R
import java.text.SimpleDateFormat
import java.util.*

class NoticeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoticeDetailBinding
    private lateinit var noticesViewModel : NoticeViewModel
    private var hisId: String? = null
    private var noticeID: String? = null
    private var noticeImageUrl : String? = null
    private lateinit var profileViewModels: ProfileViewModel

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userID: String
    private lateinit var userName: String
    private lateinit var userImage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        userID = firebaseAuth.uid.toString()

        noticesViewModel = ViewModelProviders.of(this).get(NoticeViewModel::class.java)
        noticesViewModel.getDataFromFirebase()

        hisId = intent.getStringExtra("hisId")
        noticeID = intent.getStringExtra("noticeID")
        //getUserNoticeFromFirebase(hisId.toString())

        profileViewModels = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(
            ProfileViewModel::class.java)

        profileViewModels.getUser().observe(this, androidx.lifecycle.Observer {
            userImage = it.image
            userName = it.username
        })


        binding.noticeImage.setOnClickListener {
            val intent = Intent(it.context, FullscreenPhotoActivity::class.java)
            intent.putExtra("img",noticeImageUrl)
            it.context.startActivity(intent)
        }

        binding.imgContactItem.setOnClickListener{
            val intent = Intent(it.context, UserInfoActivity::class.java)
            intent.putExtra("hisId",hisId)
            it.context.startActivity(intent)
        }
        var now = System.currentTimeMillis()

        binding.btnSend.setOnClickListener {
            var comment:String = binding.commentEdt.text.toString()
            if(comment.isEmpty()){

            }else{
                val databaseReference = FirebaseDatabase.getInstance().getReference("NoticeComments")
                val map = mapOf(
                    "noticeID" to noticeID,
                    "userName" to userName,
                    "userImage" to userImage,
                    "userID" to userID,
                    "comment" to comment,
                    "commentTime" to now.toString()
                )
                databaseReference!!.child(noticeID.toString()).push().setValue(map)
            }

            binding.commentEdt.setText("")
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            imm.hideSoftInputFromWindow(binding.commentEdt.windowToken, 0)

        }

        binding.btnComment.setOnClickListener {
            binding.commentLinear.visibility = View.VISIBLE
            binding.commentEdt.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        getCommentsFromFirebase()
        getNoticeFromFirebase(noticeID.toString())

        val ref = FirebaseDatabase.getInstance().getReference("Notices").child(noticeID.toString())

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val noticeModel = snapshot.getValue(NoticeModel::class.java)
                noticeModel?.let {
                    if (noticeModel.noticeRating!!.toInt() == 0){
                        binding.btnDown
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        val databaseReferenceRating = FirebaseDatabase.getInstance().getReference("NoticeRatings")
        val noticeIDReference = databaseReferenceRating.child(noticeID.toString()).child(userID)

        noticeIDReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val noticeRating = snapshot.getValue(NoticeRatings::class.java)
                    if (noticeRating != null) {
                        if (noticeRating.ratingUp == true){
                            binding.btnUp.setImageResource(R.drawable.up_arrow_filled)
                            binding.btnDown.setImageResource(R.drawable.down_arrow_outlined)
                        }
                        if (noticeRating.ratingDown == true){
                            binding.btnUp.setImageResource(R.drawable.up_arrow_outlined)
                            binding.btnDown.setImageResource(R.drawable.down_arrow_filled)
                        }

                        if (noticeRating.ratingUp == false && noticeRating.ratingDown == false){

                            binding.btnUp.setOnClickListener {

                                binding.btnUp.setImageResource(R.drawable.up_arrow_filled)
                                binding.btnDown.setImageResource(R.drawable.down_arrow_outlined)

                                val databaseReference = FirebaseDatabase.getInstance().getReference("NoticeRatings")
                                val map = mapOf(
                                    "noticeID" to noticeID,
                                    "userID" to userID,
                                    "ratingUp" to true,
                                    "ratingDown" to false,
                                )
                                databaseReference.child(noticeID.toString()).child(userID).setValue(map)

                                val ref = FirebaseDatabase.getInstance().getReference("Notices").child(noticeID.toString())

                                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val noticeModel = snapshot.getValue(NoticeModel::class.java)
                                        noticeModel?.let {
                                            val newCount = (noticeModel.noticeRating!!.toInt() + 1).toString()
                                            val map2 = mapOf(
                                                "noticeRating" to newCount
                                            )
                                            ref.updateChildren(map2)
                                        }

                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })

                                var count = binding.countRating.text.toString()
                                binding.countRating.text = (count.toInt() + 1).toString()
                            }

                            binding.btnDown.setOnClickListener {

                                binding.btnUp.setImageResource(R.drawable.up_arrow_outlined)
                                binding.btnDown.setImageResource(R.drawable.down_arrow_filled)

                                val databaseReference = FirebaseDatabase.getInstance().getReference("NoticeRatings")
                                val map = mapOf(
                                    "noticeID" to noticeID,
                                    "userID" to userID,
                                    "ratingUp" to false,
                                    "ratingDown" to true,
                                )
                                databaseReference!!.child(noticeID.toString()).child(userID).setValue(map)


                                val ref = FirebaseDatabase.getInstance().getReference("Notices").child(noticeID.toString())

                                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val noticeModel = snapshot.getValue(NoticeModel::class.java)
                                        noticeModel?.let {
                                            val newCount = (noticeModel.noticeRating!!.toInt() - 1).toString()
                                            val map2 = mapOf(
                                                "noticeRating" to newCount
                                            )
                                            ref.updateChildren(map2)
                                        }

                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                                var count = binding.countRating.text.toString()
                                binding.countRating.text = (count.toInt() - 1).toString()
                            }
                        }

                        if (noticeRating.ratingUp == true && noticeRating.ratingDown == false){

                            binding.btnUp.setOnClickListener {
                                binding.btnUp.setImageResource(R.drawable.up_arrow_outlined)
                                binding.btnDown.setImageResource(R.drawable.down_arrow_outlined)

                                val databaseReference = FirebaseDatabase.getInstance().getReference("NoticeRatings")
                                val map = mapOf(
                                    "noticeID" to noticeID,
                                    "userID" to userID,
                                    "ratingUp" to false,
                                    "ratingDown" to false,
                                )
                                databaseReference.child(noticeID.toString()).child(userID).setValue(map)

                                val ref = FirebaseDatabase.getInstance().getReference("Notices").child(noticeID.toString())

                                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val noticeModel = snapshot.getValue(NoticeModel::class.java)
                                        noticeModel?.let {
                                            val newCount = (noticeModel.noticeRating!!.toInt() - 1).toString()
                                            val map2 = mapOf(
                                                "noticeRating" to newCount
                                            )
                                            ref.updateChildren(map2)
                                        }

                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })

                                var count = binding.countRating.text.toString()
                                binding.countRating.text = (count.toInt() - 1).toString()
                            }

                            binding.btnDown.setOnClickListener {
                                binding.btnUp.setImageResource(R.drawable.up_arrow_outlined)
                                binding.btnDown.setImageResource(R.drawable.down_arrow_filled)

                                val databaseReference = FirebaseDatabase.getInstance().getReference("NoticeRatings")
                                val map = mapOf(
                                    "noticeID" to noticeID,
                                    "userID" to userID,
                                    "ratingUp" to false,
                                    "ratingDown" to true,
                                )
                                databaseReference!!.child(noticeID.toString()).child(userID).setValue(map)


                                val ref = FirebaseDatabase.getInstance().getReference("Notices").child(noticeID.toString())

                                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val noticeModel = snapshot.getValue(NoticeModel::class.java)
                                        noticeModel?.let {
                                            val newCount = (noticeModel.noticeRating!!.toInt() - 2).toString()
                                            val map2 = mapOf(
                                                "noticeRating" to newCount
                                            )
                                            ref.updateChildren(map2)
                                        }

                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                                var count = binding.countRating.text.toString()
                                binding.countRating.text = (count.toInt() - 2).toString()
                            }
                        }

                        if (noticeRating.ratingUp == false && noticeRating.ratingDown == true){

                            binding.btnUp.setOnClickListener {
                                binding.btnUp.setImageResource(R.drawable.up_arrow_filled)
                                binding.btnDown.setImageResource(R.drawable.down_arrow_outlined)

                                val databaseReference = FirebaseDatabase.getInstance().getReference("NoticeRatings")
                                val map = mapOf(
                                    "noticeID" to noticeID,
                                    "userID" to userID,
                                    "ratingUp" to true,
                                    "ratingDown" to false,
                                )
                                databaseReference.child(noticeID.toString()).child(userID).setValue(map)

                                val ref = FirebaseDatabase.getInstance().getReference("Notices").child(noticeID.toString())

                                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val noticeModel = snapshot.getValue(NoticeModel::class.java)
                                        noticeModel?.let {
                                            val newCount = (noticeModel.noticeRating!!.toInt() + 2).toString()
                                            val map2 = mapOf(
                                                "noticeRating" to newCount
                                            )
                                            ref.updateChildren(map2)
                                        }

                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })

                                var count = binding.countRating.text.toString()
                                binding.countRating.text = (count.toInt() + 2).toString()
                            }

                            binding.btnDown.setOnClickListener {
                                binding.btnUp.setImageResource(R.drawable.up_arrow_outlined)
                                binding.btnDown.setImageResource(R.drawable.down_arrow_outlined)
                                val databaseReference = FirebaseDatabase.getInstance().getReference("NoticeRatings")
                                val map = mapOf(
                                    "noticeID" to noticeID,
                                    "userID" to userID,
                                    "ratingUp" to false,
                                    "ratingDown" to false,
                                )
                                databaseReference!!.child(noticeID.toString()).child(userID).setValue(map)


                                val ref = FirebaseDatabase.getInstance().getReference("Notices").child(noticeID.toString())

                                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val noticeModel = snapshot.getValue(NoticeModel::class.java)
                                        noticeModel?.let {
                                            val newCount = (noticeModel.noticeRating!!.toInt() + 1).toString()
                                            val map2 = mapOf(
                                                "noticeRating" to newCount
                                            )
                                            ref.updateChildren(map2)
                                        }

                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                                var count = binding.countRating.text.toString()
                                binding.countRating.text = (count.toInt() + 1).toString()
                            }
                        }
                    }
                }else{
                    binding.btnUp.setOnClickListener {

                        binding.btnUp.setImageResource(R.drawable.up_arrow_filled)
                        binding.btnDown.setImageResource(R.drawable.down_arrow_outlined)

                        val databaseReference = FirebaseDatabase.getInstance().getReference("NoticeRatings")
                        val map = mapOf(
                            "noticeID" to noticeID,
                            "userID" to userID,
                            "ratingUp" to true,
                            "ratingDown" to false,
                        )
                        databaseReference.child(noticeID.toString()).child(userID).setValue(map)

                        val ref = FirebaseDatabase.getInstance().getReference("Notices").child(noticeID.toString())

                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val noticeModel = snapshot.getValue(NoticeModel::class.java)
                                noticeModel?.let {
                                    val newCount = (noticeModel.noticeRating!!.toInt() + 1).toString()
                                    val map2 = mapOf(
                                        "noticeRating" to newCount
                                    )
                                    ref.updateChildren(map2)
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })

                        var count = binding.countRating.text.toString()
                        binding.countRating.text = (count.toInt() + 1).toString()
                    }

                    binding.btnDown.setOnClickListener {

                        binding.btnUp.setImageResource(R.drawable.up_arrow_outlined)
                        binding.btnDown.setImageResource(R.drawable.down_arrow_filled)

                        val databaseReference = FirebaseDatabase.getInstance().getReference("NoticeRatings")
                        val map = mapOf(
                            "noticeID" to noticeID,
                            "userID" to userID,
                            "ratingUp" to false,
                            "ratingDown" to true,
                        )
                        databaseReference!!.child(noticeID.toString()).child(userID).setValue(map)


                        val ref = FirebaseDatabase.getInstance().getReference("Notices").child(noticeID.toString())

                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val noticeModel = snapshot.getValue(NoticeModel::class.java)
                                noticeModel?.let {
                                    val newCount = (noticeModel.noticeRating!!.toInt() - 1).toString()
                                    val map2 = mapOf(
                                        "noticeRating" to newCount
                                    )
                                    ref.updateChildren(map2)
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                        var count = binding.countRating.text.toString()
                        binding.countRating.text = (count.toInt() - 1).toString()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })






        binding.recycler.setOnTouchListener { _, _ ->
            true
        }

    }



    fun getNoticeFromFirebase(noticeID:String){
        val databaseReference = FirebaseDatabase.getInstance().getReference("Notices")
        val query = databaseReference.orderByChild("noticeID").equalTo(noticeID)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (data in snapshot.children) {
                        val noticeModel = data.getValue(NoticeModel::class.java)
                        noticeModel?.let {
                            // istenilen child'ın verileri burada
                            binding.noticeModel = noticeModel
                            if (noticeModel.noticeDegree == "green"){
                                binding.contextNoticeItem.background = this@NoticeDetailActivity.resources.getDrawable(R.drawable.border_notice_detail_green)
                                binding.btnUp.setColorFilter(ContextCompat.getColor(this@NoticeDetailActivity, R.color.main_green), PorterDuff.Mode.SRC_IN)
                                binding.btnDown.setColorFilter(ContextCompat.getColor(this@NoticeDetailActivity, R.color.main_green), PorterDuff.Mode.SRC_IN)
                                binding.btnComment.setColorFilter(ContextCompat.getColor(this@NoticeDetailActivity, R.color.main_green), PorterDuff.Mode.SRC_IN)
                            }
                            if (noticeModel.noticeDegree == "yellow"){
                                binding.contextNoticeItem.background = this@NoticeDetailActivity.resources.getDrawable(R.drawable.border_notice_detail_yellow)
                                binding.btnUp.setColorFilter(ContextCompat.getColor(this@NoticeDetailActivity, R.color.mainYellow), PorterDuff.Mode.SRC_IN)
                                binding.btnDown.setColorFilter(ContextCompat.getColor(this@NoticeDetailActivity, R.color.mainYellow), PorterDuff.Mode.SRC_IN)
                                binding.btnComment.setColorFilter(ContextCompat.getColor(this@NoticeDetailActivity, R.color.mainYellow), PorterDuff.Mode.SRC_IN)
                            }
                            if (noticeModel.noticeDegree == "red"){
                                binding.contextNoticeItem.background = this@NoticeDetailActivity.resources.getDrawable(R.drawable.border_notice_detail_red)
                                binding.btnUp.setColorFilter(ContextCompat.getColor(this@NoticeDetailActivity, R.color.main_red2), PorterDuff.Mode.SRC_IN)
                                binding.btnDown.setColorFilter(ContextCompat.getColor(this@NoticeDetailActivity, R.color.main_red2), PorterDuff.Mode.SRC_IN)
                                binding.btnComment.setColorFilter(ContextCompat.getColor(this@NoticeDetailActivity, R.color.main_red2), PorterDuff.Mode.SRC_IN)
                            }

                            var currentTime = System.currentTimeMillis()

                            val simpleDateFormat = SimpleDateFormat("kk:mm", Locale.getDefault())
                            val simpleDateFormat2 = SimpleDateFormat("kk:mm dd.MM.yyyy", Locale.getDefault())


                            val minute = SimpleDateFormat("mm", Locale.getDefault())
                            val hours = SimpleDateFormat("kk", Locale.getDefault())
                            val day = SimpleDateFormat("dd", Locale.getDefault())
                            val month = SimpleDateFormat("MM", Locale.getDefault())
                            val year = SimpleDateFormat("yyyy", Locale.getDefault())

                            val sharedNoticeMinute = minute.format(noticeModel.noticeTime?.toLong())
                            val sharedNoticeHours = hours.format(noticeModel.noticeTime?.toLong())
                            val sharedNoticeDay = day.format(noticeModel.noticeTime?.toLong())
                            val sharedNoticeMonth = month.format(noticeModel.noticeTime?.toLong())
                            val sharedNoticeYear = year.format(noticeModel.noticeTime?.toLong())

                            val currentMinute = minute.format(currentTime)
                            val currentHours = hours.format(currentTime)
                            val currentDay = day.format(currentTime)
                            val currentMonth = month.format(currentTime)
                            val currentYear = year.format(currentTime)

                            val date = simpleDateFormat.format(noticeModel.noticeTime?.toLong())
                            val date2 = simpleDateFormat2.format(noticeModel.noticeTime?.toLong())


                            val now = Calendar.getInstance()
                            val noticeTimeMillis = noticeModel.noticeTime?.toLong() ?: 0
                            val noticeTime = Calendar.getInstance().apply { timeInMillis = noticeTimeMillis }

                            val diffMillis = now.timeInMillis - noticeTime.timeInMillis
                            val diffMinutes = diffMillis / (1000 * 60)
                            var zaman = currentTime - noticeModel.noticeTime!!.toLong()

                            if (zaman < 60_000
                            /*
                        if (now.get(Calendar.YEAR) == noticeTime.get(Calendar.YEAR) &&
                            now.get(Calendar.DAY_OF_YEAR) == noticeTime.get(Calendar.DAY_OF_YEAR) &&
                            now.get(Calendar.HOUR_OF_DAY) == noticeTime.get(Calendar.HOUR_OF_DAY) &&
                            now.get(Calendar.MINUTE) == noticeTime.get(Calendar.MINUTE)

                             */
                            ) {
                                binding.tvTime.text = "Şimdi"
                            }
                            else {
                                if (diffMinutes < 60 && diffMinutes >= 1){
                                    binding.tvTime.text = "${diffMinutes} dakika önce"
                                }else{
                                    if(sharedNoticeDay == currentDay && sharedNoticeMonth == currentMonth && sharedNoticeYear == currentYear){
                                        binding.tvTime.text = "Bugün ${date.toString()}"
                                    }else{
                                        binding.tvTime.text = date2.toString()

                                    }
                                }
                            }

                            if (noticeModel.noticeImage != ""){
                                noticeImageUrl = noticeModel.noticeImage
                                Glide.with(applicationContext).load(noticeModel.noticeImage).into(binding.noticeImage)
                                binding.noticeImage.visibility = View.VISIBLE
                            }else{
                                binding.noticeImage.visibility = View.GONE
                            }

                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // işlem iptal edildiğinde yapılacaklar burada
            }
        })



    }

    fun getCommentsFromFirebase() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("NoticeComments")
        val noticeIDReference = databaseReference.child(noticeID.toString())

        val commentList = mutableListOf<NoticeCommentModel>()

        noticeIDReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (childSnapshot in snapshot.children) {
                    val noticeComment = childSnapshot.getValue(NoticeCommentModel::class.java)
                    if (noticeComment != null) {
                        commentList.add(noticeComment)
                    }
                }
                commentList.reverse()
                val adapter = NoticeCommentsAdapter(
                    commentList,this@NoticeDetailActivity
                )


                binding.recycler.adapter = adapter
                binding.recycler.layoutManager = LinearLayoutManager(this@NoticeDetailActivity)
                // Yeni verileri kullanarak arayüzü güncelleme kodu
                // Örneğin, RecyclerView veya ListView kullanarak bir liste görünümü güncellenir.
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.commentEdt.windowToken, 0)
    }
}
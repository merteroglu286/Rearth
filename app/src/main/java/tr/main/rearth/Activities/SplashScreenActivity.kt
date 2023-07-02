package tr.main.rearth.Activities

import android.animation.ValueAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import tr.main.rearth.R
import tr.main.rearth.UserModel
import tr.main.rearth.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private var firebaseAuth: FirebaseAuth? = null

    var rotateAnimation: Animation? = null
    var imageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        imageView = binding.logo

        rotateAnimation()

        val anim = ValueAnimator.ofInt(0, 300)
        anim.duration = 2000 // 2 saniye
        anim.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            binding.logo.layoutParams.height = value
            binding.logo.layoutParams.width = value
            binding.logo.requestLayout()
        }
        anim.start()


        Handler().postDelayed({

            if (firebaseAuth?.currentUser == null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                val ref = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseAuth!!.currentUser!!.uid)

                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userModel = snapshot.getValue(UserModel::class.java)
                            if (userModel!!.kayitliMi == true){
                                FirebaseMessaging.getInstance().token
                                    .addOnCompleteListener(OnCompleteListener {
                                        if (it.isSuccessful) {
                                            //val token = it.result!!
                                            val token = OneSignal.getDeviceState()!!.userId
                                            val databaseReference =
                                                FirebaseDatabase.getInstance().getReference("Users")
                                                    .child(firebaseAuth!!.currentUser!!.uid)
                                            val map: MutableMap<String, Any> = HashMap()
                                            map["token"] = token
                                            databaseReference.updateChildren(map)
                                        }
                                    })
                                val intent = Intent(this@SplashScreenActivity, DashboardActivity::class.java)
                                startActivity(intent)
                                finish()
                            }else{
                                ref.removeValue()
                                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                                finish()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })


            }

        }, 2500)
    }

    private fun rotateAnimation() {
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_anim)
        imageView!!.startAnimation(rotateAnimation)
    }
}
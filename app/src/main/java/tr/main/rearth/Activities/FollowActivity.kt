package tr.main.rearth.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import tr.main.rearth.Adapters.FollowAdapter
import tr.main.rearth.FollowRequestModel
import tr.main.rearth.ViewModels.FollowRequestsWiewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_follow.*
import tr.main.rearth.R

class FollowActivity : AppCompatActivity() {
    private lateinit var followRequestsWiewModel : FollowRequestsWiewModel
    var TYPE = 0 // 0 ise followers listelecek, 1 ise following

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow)
        followRequestsWiewModel = ViewModelProviders.of(this).get(FollowRequestsWiewModel::class.java)
        followRequestsWiewModel.getDataFromFirebase()

        TYPE = intent.getIntExtra("TYPE",0)

        if (TYPE == 0){
            observeLiveData()
        }else{
            getFollowersData()
        }
    }

    fun getFollowersData() {
        val followingList = ArrayList<FollowRequestModel>()
        FirebaseDatabase.getInstance().getReference("Followers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    followingList.clear()
                    for (hisIdSnapshot in snapshot.children) {
                        for (userIdSnapshot in hisIdSnapshot.children) {
                            val userId = userIdSnapshot.key
                            val followRequestsWiewModel = userIdSnapshot.getValue(FollowRequestModel::class.java)
                            if (userId == FirebaseAuth.getInstance().currentUser!!.uid && followRequestsWiewModel?.onaylandiMi == true){
                                followRequestsWiewModel.let { followingList.add(it) }
                            }
                            // her bir hisId altındaki userId verilerine burada erişebilirsiniz.
                        }
                    }
                    followingList.reverse()
                    followRecycler.layoutManager = LinearLayoutManager(this@FollowActivity)
                    followRecycler.adapter = FollowAdapter(followingList,this@FollowActivity,1)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Veritabanı işlemi iptal edildiğinde burada işlem yapabilirsiniz.
                }
            })

    }

    fun observeLiveData(){
        followRequestsWiewModel.followersLiveData.observe(this, Observer { follow ->
            follow?.let{
                followRecycler.layoutManager = LinearLayoutManager(this)
                followRecycler.adapter = FollowAdapter(follow as MutableList<FollowRequestModel>,this,0)
            }
        })

        followRequestsWiewModel.requestsError.observe(this, Observer { error->
            error?.let{
                if (it){
                    Toast.makeText(this,"hata çıktı", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }
}
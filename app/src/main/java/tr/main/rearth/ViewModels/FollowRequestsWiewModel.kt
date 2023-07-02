package tr.main.rearth.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tr.main.rearth.FollowRequestModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FollowRequestsWiewModel : ViewModel() {
    val requestsLiveData = MutableLiveData<List<FollowRequestModel>>()
    val followersLiveData = MutableLiveData<List<FollowRequestModel>>()
    val requestsError = MutableLiveData<Boolean>()

    fun getDataFromFirebase() {

        val requestsList = arrayListOf<FollowRequestModel>()
        val followersList = arrayListOf<FollowRequestModel>()

        val databaseReference =
            FirebaseAuth.getInstance().currentUser?.uid?.let {
                FirebaseDatabase.getInstance().getReference("Followers").child(
                    it
                )
            }

        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requestsList.clear()
                for (data in snapshot.children) {
                    val followRequestsWiewModel = data.getValue(FollowRequestModel::class.java)
                    followRequestsWiewModel?.let {

                        if (followRequestsWiewModel.onaylandiMi == false){
                            requestsList.add(followRequestsWiewModel)
                        }else{
                            followersList.add(followRequestsWiewModel)
                        }

                    }
                }
                requestsList.reverse()
                followersList.reverse()
                requestsLiveData.value = requestsList
                followersLiveData.value = followersList
                requestsError.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                requestsError.value = true
            }
        })

    }
}

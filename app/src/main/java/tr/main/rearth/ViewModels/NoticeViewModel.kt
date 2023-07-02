package tr.main.rearth.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tr.main.rearth.NoticeModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NoticeViewModel: ViewModel() {
    val noticesLiveData = MutableLiveData<List<NoticeModel>>()
    val noticeError = MutableLiveData<Boolean>()
    val noticeLoading = MutableLiveData<Boolean>()

    fun getDataFromFirebase() {
        val noticesList = arrayListOf<NoticeModel>()
        val databaseReference = FirebaseDatabase.getInstance().getReference("Notices")

        databaseReference.orderByChild("noticeTime").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                noticesList.clear()
                for (data in snapshot.children) {
                    val noticeModel = data.getValue(NoticeModel::class.java)
                    noticeModel?.let {
                        noticesList.add(noticeModel)
                    }
                }
                noticesList.reverse()
                noticesLiveData.value = noticesList
                noticeError.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                noticeError.value = true
            }
        })
    }


    fun getUserNoticeFromFirebase(uid:String) {

        val noticesList = arrayListOf<NoticeModel>()

        val databaseReference = FirebaseDatabase.getInstance().getReference("Locations").child(uid)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val noticeModel = data.getValue(NoticeModel::class.java)
                    noticeModel?.let {
                        noticesList.add(noticeModel)
                    }
                }
                noticesLiveData.value = noticesList
                noticeError.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                noticeError.value = true
            }
        })

    }
}




package tr.main.rearth.Adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tr.main.rearth.Activities.UserInfoActivity
import tr.main.rearth.SearchHistoryModel
import tr.main.rearth.ViewModels.ProfileViewModel
import tr.main.rearth.databinding.DeleteItemDialogBinding
import tr.main.rearth.databinding.SearchHistoryRowBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.dm_screen_row.view.*
import java.util.*

class SearchHistoryAdapter(
    private var list: MutableList<SearchHistoryModel>
):
    RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>(){

    private var allHistory: MutableList<SearchHistoryModel> = list
    private lateinit var profileViewModels: ProfileViewModel
    private lateinit var myName:String
    private lateinit var myImage:String
    private lateinit var dialog: AlertDialog
    private var isEnable = false
    private val itemSelectedList = mutableListOf<Int>()
    private lateinit var deleteItemDialogBinding: DeleteItemDialogBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val searchHistoryLayoutBinding =
            SearchHistoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchHistoryAdapter.ViewHolder(searchHistoryLayoutBinding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchHistoryModel = allHistory[position]
        holder.binding.searchHistoryModel = searchHistoryModel

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, UserInfoActivity::class.java)
            intent.putExtra("hisId",searchHistoryModel.uid)
            intent.putExtra("hisImage",searchHistoryModel.imageUrl)
            intent.putExtra("hisName",searchHistoryModel.username)
            it.context.startActivity(intent)

        }

        holder.binding.deleteButtonSearchHistory.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().getReference("SearchHistory").child(
                FirebaseAuth.getInstance().currentUser!!.uid).child(searchHistoryModel.uid)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (refSnapshot in dataSnapshot.children) {
                        refSnapshot.ref.removeValue()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(ContentValues.TAG, "onCancelled", databaseError.toException())
                }
            })
        }



    }

    override fun getItemCount(): Int {
        return allHistory.size
    }

    class ViewHolder(val binding: SearchHistoryRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }


}
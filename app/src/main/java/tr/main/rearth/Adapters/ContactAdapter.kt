package tr.main.rearth.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import tr.main.rearth.Activities.UserInfoActivity
import tr.main.rearth.UserModel
import tr.main.rearth.databinding.ContactItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.ArrayList

class ContactAdapter(private var appContacts: ArrayList<UserModel>,
                     searchView: androidx.appcompat.widget.SearchView,
):
    RecyclerView.Adapter<ContactAdapter.ViewHolder>(),Filterable {

    private var allContact: ArrayList<UserModel> = appContacts
    private var searchView = searchView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val contactItemLayoutBinding =
            ContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(contactItemLayoutBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userModel = allContact[position]
        holder.contactItemBinding.userModel = userModel

/*
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context,UserInfoActivity::class.java)
            intent.putExtra("userId",userModel.uid)
            it.context.startActivity(intent)
        }

 */

        holder.itemView.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            val databaseReference = FirebaseDatabase.getInstance().getReference("SearchHistory")
            val map = mapOf(
                "uid" to userModel.uid,
                "username" to userModel.username,
                "imageUrl" to userModel.image
            )
            databaseReference!!.child(FirebaseAuth.getInstance().currentUser!!.uid).child(userModel.uid).updateChildren(map)

            val intent = Intent(it.context,UserInfoActivity::class.java)
            intent.putExtra("hisId",userModel.uid)
            intent.putExtra("hisImage",userModel.image)
            intent.putExtra("hisName",userModel.username)
            it.context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return allContact.size
    }

    class ViewHolder(val contactItemBinding: ContactItemBinding) :
        RecyclerView.ViewHolder(contactItemBinding.root) {

    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val searchContent = constraint.toString()
                if (searchContent.isEmpty()){
                    allContact= ArrayList(appContacts)
                }
                else {

                    val filterContact = ArrayList<UserModel>()
                    for (userModel in appContacts) {

                        if (userModel.username.toLowerCase(Locale.ROOT).trim()
                                .contains(searchContent.toLowerCase(Locale.ROOT).trim())
                        ){
                            filterContact.add(userModel)
                        }

                    }
                    allContact = filterContact
                }

                val filterResults = FilterResults()
                filterResults.values = allContact
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                allContact = results?.values as ArrayList<UserModel>
                notifyDataSetChanged()

            }
        }
    }

}


package tr.main.rearth.Adapters

import android.content.Context
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tr.main.rearth.ChatModel
import tr.main.rearth.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter(
    private val context: Context,
    private val chatList: ArrayList<ChatModel>,
    messageRecyclerView: RecyclerView
) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1
    var firebaseUser: FirebaseUser? = null

    private val recycler = messageRecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == MESSAGE_TYPE_RIGHT) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.right_item_layout, parent, false)
            return ViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.left_item_layout, parent, false)
            return ViewHolder(view)
        }

    }

    override fun getItemCount(): Int {
        return chatList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatList[position]
        holder.txtUserName.text = chat.message
        //val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy kk:mm", Locale.getDefault())
        val simpleDateFormat = SimpleDateFormat("kk:mm", Locale.getDefault())
        val dayFormat = SimpleDateFormat("dd.MM.yyyy",Locale.getDefault())
        val date = simpleDateFormat.format(chat.date.toLong())
        val day = dayFormat.format(chat.date.toLong())
        holder.txtTime.text = date.toString()
        holder.txtDay.text = day.toString()
        //Glide.with(context).load(user.profileImage).placeholder(R.drawable.profile_image).into(holder.imgUser)

        holder.itemView.setOnClickListener {
            TransitionManager.beginDelayedTransition(recycler)
            holder.txtDay.visibility = if (holder.txtDay.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUserName: TextView = view.findViewById(R.id.tvMessage)
        val txtTime : TextView = view.findViewById(R.id.sendTime)
        val txtDay : TextView = view.findViewById(R.id.sendDay)
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (chatList[position].senderId == firebaseUser!!.uid) {
            return MESSAGE_TYPE_RIGHT
        } else {
            return MESSAGE_TYPE_LEFT
        }

    }
}
package tr.main.rearth.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import tr.main.rearth.Activities.NoticeDetailActivity
import tr.main.rearth.Activities.FullscreenPhotoActivity
import tr.main.rearth.Activities.UserInfoActivity
import tr.main.rearth.NoticeModel
import tr.main.rearth.R
import tr.main.rearth.databinding.ItemNoticeBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NoticeAdapter(val noticeList: ArrayList<NoticeModel>,context: Context): RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    val context = context
    class NoticeViewHolder(var view: ItemNoticeBinding) : RecyclerView.ViewHolder(view.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<ItemNoticeBinding>(inflater,R.layout.item_notice,parent,false)
        return NoticeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return noticeList.size
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = noticeList[position]
        holder.view.noticeModel = notice

        if (notice.noticeImage != ""){
            Glide.with(context).load(notice.noticeImage).into(holder.view.noticeImage)
            holder.view.noticeImage.visibility = View.VISIBLE
        }else{
            holder.view.noticeImage.visibility = View.GONE
        }
        if (notice.noticeDegree == "green"){
            //holder.view.contextNoticeItem.background = context.resources.getDrawable(R.drawable.border_dialog_green)
            holder.view.degreeIcon.setColorFilter(ContextCompat.getColor(context, R.color.main_green), PorterDuff.Mode.SRC_IN)

        }
        if (notice.noticeDegree == "yellow"){
            // holder.view.contextNoticeItem.background = context.resources.getDrawable(R.drawable.border_dialog_yellow)
            holder.view.degreeIcon.setColorFilter(ContextCompat.getColor(context, R.color.mainYellow), PorterDuff.Mode.SRC_IN)

        }
        if (notice.noticeDegree == "red"){
            //  holder.view.contextNoticeItem.background = context.resources.getDrawable(R.drawable.border_dialog_red)
            holder.view.degreeIcon.setColorFilter(ContextCompat.getColor(context, R.color.main_red2), PorterDuff.Mode.SRC_IN)
        }

        var currentTime = System.currentTimeMillis()

        val simpleDateFormat = SimpleDateFormat("kk:mm", Locale.getDefault())
        val simpleDateFormat2 = SimpleDateFormat("kk:mm dd.MM.yyyy", Locale.getDefault())


        val minute = SimpleDateFormat("mm", Locale.getDefault())
        val hours = SimpleDateFormat("kk", Locale.getDefault())
        val day = SimpleDateFormat("dd", Locale.getDefault())
        val month = SimpleDateFormat("MM", Locale.getDefault())
        val year = SimpleDateFormat("yyyy", Locale.getDefault())

        val sharedNoticeMinute = minute.format(notice.noticeTime?.toLong())
        val sharedNoticeHours = hours.format(notice.noticeTime?.toLong())
        val sharedNoticeDay = day.format(notice.noticeTime?.toLong())
        val sharedNoticeMonth = month.format(notice.noticeTime?.toLong())
        val sharedNoticeYear = year.format(notice.noticeTime?.toLong())

        val currentMinute = minute.format(currentTime)
        val currentHours = hours.format(currentTime)
        val currentDay = day.format(currentTime)
        val currentMonth = month.format(currentTime)
        val currentYear = year.format(currentTime)

        val date = simpleDateFormat.format(notice.noticeTime?.toLong())
        val date2 = simpleDateFormat2.format(notice.noticeTime?.toLong())


        val now = Calendar.getInstance()
        val noticeTimeMillis = notice.noticeTime?.toLong() ?: 0
        val noticeTime = Calendar.getInstance().apply { timeInMillis = noticeTimeMillis }

        val diffMillis = now.timeInMillis - noticeTime.timeInMillis
        val diffMinutes = diffMillis / (1000 * 60)

        var zaman = currentTime - notice.noticeTime!!.toLong()

        if (zaman < 60_000
        ) {
            holder.view.tvTime.text = "Şimdi"
        }
        else{
            if (diffMinutes < 60 && diffMinutes >= 1){
                holder.view.tvTime.text = "${diffMinutes} dakika önce"
            }else{
                if(sharedNoticeDay == currentDay && sharedNoticeMonth == currentMonth && sharedNoticeYear == currentYear){
                    holder.view.tvTime.text = "Bugün ${date.toString()}"
                }else{
                    holder.view.tvTime.text = date2.toString()
                }
            }
        }

        holder.view.imgContactItem.setOnClickListener{
            val intent = Intent(it.context, UserInfoActivity::class.java)
            intent.putExtra("hisId",notice.userID)
            intent.putExtra("hisImage",notice.userImage)
            intent.putExtra("hisName",notice.username)
            it.context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, NoticeDetailActivity::class.java)
            intent.putExtra("hisId",notice.userID)
            intent.putExtra("noticeID",notice.noticeID)
            it.context.startActivity(intent)
        }

        holder.view.noticeImage.setOnClickListener {
            val intent = Intent(it.context, FullscreenPhotoActivity::class.java)
            intent.putExtra("img",notice.noticeImage)
            it.context.startActivity(intent)
        }
    }

    fun updateNoticesList(newNoticesList:List<NoticeModel>){
        noticeList.clear()
        noticeList.addAll(newNoticesList)
        notifyDataSetChanged()
    }
}
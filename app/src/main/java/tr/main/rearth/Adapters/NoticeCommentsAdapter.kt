package tr.main.rearth.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import tr.main.rearth.Activities.UserInfoActivity
import tr.main.rearth.NoticeCommentModel
import tr.main.rearth.R
import tr.main.rearth.databinding.ItemNoticeCommentBinding
import java.text.SimpleDateFormat
import java.util.*

class NoticeCommentsAdapter(val commentsList: MutableList<NoticeCommentModel>, context: Context) : RecyclerView.Adapter<NoticeCommentsAdapter.NoticeCommentsViewHolder>() {

    val context = context

    class NoticeCommentsViewHolder(val view:ItemNoticeCommentBinding):RecyclerView.ViewHolder(view.root) {

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeCommentsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<ItemNoticeCommentBinding>(inflater,
            R.layout.item_notice_comment,parent,false)
        return NoticeCommentsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    override fun onBindViewHolder(holder: NoticeCommentsViewHolder, position: Int) {
        val comments = commentsList[position]
        holder.view.noticeCommentModel = comments



        var currentTime = System.currentTimeMillis()

        val simpleDateFormat = SimpleDateFormat("kk:mm", Locale.getDefault())
        val simpleDateFormat2 = SimpleDateFormat("kk:mm dd.MM.yyyy", Locale.getDefault())


        val minute = SimpleDateFormat("mm", Locale.getDefault())
        val hours = SimpleDateFormat("kk", Locale.getDefault())
        val day = SimpleDateFormat("dd", Locale.getDefault())
        val month = SimpleDateFormat("MM", Locale.getDefault())
        val year = SimpleDateFormat("yyyy", Locale.getDefault())

        val sharedNoticeMinute = minute.format(comments.commentTime?.toLong())
        val sharedNoticeHours = hours.format(comments.commentTime?.toLong())
        val sharedNoticeDay = day.format(comments.commentTime?.toLong())
        val sharedNoticeMonth = month.format(comments.commentTime?.toLong())
        val sharedNoticeYear = year.format(comments.commentTime?.toLong())

        val currentMinute = minute.format(currentTime)
        val currentHours = hours.format(currentTime)
        val currentDay = day.format(currentTime)
        val currentMonth = month.format(currentTime)
        val currentYear = year.format(currentTime)

        val date = simpleDateFormat.format(comments.commentTime?.toLong())
        val date2 = simpleDateFormat2.format(comments.commentTime?.toLong())


        val now = Calendar.getInstance()
        val noticeTimeMillis = comments.commentTime?.toLong() ?: 0
        val noticeTime = Calendar.getInstance().apply { timeInMillis = noticeTimeMillis }

        val diffMillis = now.timeInMillis - noticeTime.timeInMillis
        val diffMinutes = diffMillis / (1000 * 60)

        var zaman = currentTime - comments.commentTime!!.toLong()

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
            intent.putExtra("hisId",comments.userID)
            intent.putExtra("hisImage",comments.userImage)
            intent.putExtra("hisName",comments.userName)
            it.context.startActivity(intent)
        }
    }
}



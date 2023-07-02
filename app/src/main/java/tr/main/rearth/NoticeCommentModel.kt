package tr.main.rearth

data class NoticeCommentModel(
    val noticeID : String?,
    val userName : String?,
    val userImage : String?,
    val userID : String?,
    val comment : String?,
    val commentTime : String?,
){
    constructor(): this("","","","","","")
}

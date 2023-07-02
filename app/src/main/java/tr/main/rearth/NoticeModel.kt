package tr.main.rearth

data class NoticeModel(
    val username : String?,
    val userImage:String?,
    val userID : String?,
    val latitude: String?,
    val longitude : String?,
    val noticeID : String?,
    val noticeMessage : String?,
    val noticeImage : String?,
    val noticeDegree : String?,
    val noticeTime : String?,
    val noticeRating : String?,
    val noticeComment : String?,
) {
    constructor() : this("","", "","", "", "", "", "", "","","","")
}


package tr.main.rearth

data class NoticeRatings(
    val userID : String?,
    val noticeID : String?,
    val ratingUp : Boolean?,
    val ratingDown : Boolean?
) {
    constructor() : this("","", false,false)
}

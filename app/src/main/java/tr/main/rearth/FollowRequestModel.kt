package tr.main.rearth

data class FollowRequestModel(
    val senderId : String = "",
    val receiverId : String = "",
    val senderImage : String = "",
    val receiverImage : String = "",
    val senderName : String = "",
    val receiverName : String = "",
    val date : String = "",
    var onaylandiMi : Boolean = false
)

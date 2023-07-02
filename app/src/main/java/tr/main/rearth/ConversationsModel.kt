package tr.main.rearth

data class ConversationsModel(
    var lastMessage: String = "",
    val receiverId: String = "",
    val receiverImage: String = "",
    val receiverName: String = "",
    val senderId: String = "",
    var date: String = System.currentTimeMillis().toString(),
    var okunduMu: Boolean = false
)

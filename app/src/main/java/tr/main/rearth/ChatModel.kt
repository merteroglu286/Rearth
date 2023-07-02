package tr.main.rearth

data class ChatModel(
    var senderId:String = "",
    var receiverId:String = "",
    var message:String = "",
    var date: String = System.currentTimeMillis().toString(),
)

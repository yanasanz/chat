data class Message(
    val userId: Int,
    val messageId: Int,
    val message: String,
    val sendTo: Int,
    var isRead: Boolean = false,
    var isDeleted: Boolean = false,
)
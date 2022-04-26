data class Chat(
    val chatId: Int,
    val messages: MutableList<Message> = mutableListOf(),
    val chatUsers: MutableList<User> = mutableListOf(),
    var isDeleted: Boolean = false,
)
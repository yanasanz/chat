data class User(
    val userId: Int,
    val userName: String,
    val receivedMessages: MutableList<Message> = mutableListOf(),
    val sentMessages: MutableList<Message> = mutableListOf()
)
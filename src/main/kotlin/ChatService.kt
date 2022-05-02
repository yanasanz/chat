class ChatService {

    val chats: MutableList<Chat> = mutableListOf()
    private val allMessageIds: MutableList<Int> = mutableListOf()
    val users: MutableList<User> = mutableListOf()

    fun addUser(userId: Int, userName: String): User {
        users.apply { add(User(userId, userName)) }
        return users.last()
    }

    fun createChat(userId: Int, message: String, sendTo: Int): Chat {
        val newMessage = Message(userId, allMessageIds.size + 1, message, sendTo)
        allMessageIds.add(newMessage.messageId)
        val newChat = Chat(chats.size + 1).apply {
            messages.add(newMessage)
        }
        chats.add(newChat)
        users.first { it.userId == userId }.apply {
            sentMessages.add(newMessage)
            let(newChat.chatUsers::add)
        }
        users.first { it.userId == sendTo }.apply {
            receivedMessages.add(newMessage)
            let(newChat.chatUsers::add)
        }
        return newChat
    }

    fun createMessage(userId: Int, message: String, chatId: Int): Message {
        val sender = chats.first { it.chatId == chatId }.chatUsers.first { it.userId == userId }
        val receiver = chats.first { it.chatId == chatId }.chatUsers.first { it.userId != userId }
        val newMessage = Message(sender.userId, allMessageIds.size + 1, message, receiver.userId)
        allMessageIds.add(newMessage.messageId)
        chats.first { it.chatId == chatId }.apply {
            messages.add(newMessage)
            sender.sentMessages.add(newMessage)
            receiver.receivedMessages.add(newMessage)
        }
        return newMessage
    }

    fun deleteChat(chatId: Int) {
        if (chatId !in 1..chats.size || chats[chatId - 1].isDeleted) throw ChatNotFoundException("Chat not found")
        chats.first { it.chatId == chatId }.apply {
            isDeleted = true
            messages.filter { !it.isDeleted }.forEach { message ->
                message.isDeleted = true
                chatUsers.forEach { user ->
                    user.sentMessages.filter { it.messageId == message.messageId }.forEach { it.isDeleted = true }
                    user.receivedMessages.filter { it.messageId == message.messageId }.forEach { it.isDeleted = true }
                }
            }
        }
    }

    fun deleteMessage(messageId: Int) {
        if (messageId !in 1..allMessageIds.size) throw MessageNotFoundException("Message not found")
        chats.forEach { chat ->
            chat.messages.first { it.messageId == messageId }.let { message ->
                when {
                    message.isDeleted -> throw MessageNotFoundException("Message not found")
                    !message.isDeleted -> message.isDeleted = true
                }
            }
            chat.messages.all { it.isDeleted }.let { chat.isDeleted = true }
        }
    }

    fun getChats(userId: Int) {
        if (userId !in 1..users.size) {
            println("Нет такого пользователя")
            return
        }
        chats.forEach { chat ->
            val currentUser: User? = chat.chatUsers.find { it.userId == userId }
            if (chat.chatUsers.contains(currentUser) && !chat.isDeleted) {
                val user: User? = chat.chatUsers.find { it.userId == chat.messages.last().userId }.takeIf { it != null }
                println("Чат #${chat.chatId} ${chat.chatUsers[0].userName} - ${chat.chatUsers[1].userName}")
                println("Последнее сообщение:")
                println("${user?.userName} : ${chat.messages.last().message}")
            } else {
                println("Нет сообщений")
            }
        }
    }

    fun getChatMessages(chatId: Int, firstMessageId: Int, messagesAmount: Int): List<Message> {
        if (chatId !in 1..chats.size || chats[chatId - 1].isDeleted) throw ChatNotFoundException("Chat not found")
        val showMessagesList = chats.first { it.chatId == chatId }.let { chat ->
            val messageIndex = chat.messages.indexOfFirst { it.messageId == firstMessageId }
            chat.messages.asSequence()
                .drop(messageIndex)
                .take(messagesAmount)
                .toMutableList()
        }
        chats.first { it.chatId == chatId }.let { chat ->
            println("Чат #${chat.chatId} ${chat.chatUsers[0].userName} - ${chat.chatUsers[1].userName}")
            showMessagesList.forEach { message ->
                val currentUserName = users.first { it.userId == message.userId }.userName
                if (!message.isDeleted) {
                    println("$currentUserName: ${message.message}")
                }
            }
            (showMessagesList.indices).forEach { i ->
                chat.messages.forEach { message ->
                    if (message.messageId == showMessagesList[i].messageId) {
                        message.isRead = true
                    }
                }
            }
        }
        return showMessagesList
    }

    fun getUnreadChatsCount(userId: Int): Int {
        if (userId !in 1..users.size) {
            println("Нет такого пользователя")
            return -1
        }
        var unreadChatsCount = 0
        chats.forEach { chat ->
            var messageCounter = 0
            val currentUser = chat.chatUsers.first { it.userId == userId }
            if (chat.chatUsers.contains(currentUser) && !chat.isDeleted) {
                chat.messages.forEach {
                    if (!it.isRead) messageCounter++
                }
            }
            if (messageCounter > 0) unreadChatsCount++
        }
        return unreadChatsCount
    }

}

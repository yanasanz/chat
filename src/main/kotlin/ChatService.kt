class ChatService {

    val chats: MutableList<Chat> = mutableListOf()
    private val allMessageIds: MutableList<Int> = mutableListOf()
    val users: MutableList<User> = mutableListOf()

    fun addUser(userId: Int, userName: String): User {
        val newUser = User(userId, userName)
        users.add(newUser)
        return newUser
    }

    fun createChat(userId: Int, message: String, sendTo: Int): Chat {
        val newMessage = Message(userId, allMessageIds.size + 1, message, sendTo)
        allMessageIds += newMessage.messageId
        val chatId = chats.size + 1
        val newChat = Chat(chatId)
        newChat.messages += newMessage
        chats.add(newChat)
        val sender = users.first { it.userId == userId }
        sender.sentMessages.add(newMessage)
        val receiver = users.first { it.userId == sendTo }
        receiver.receivedMessages.add(newMessage)
        sender.let(newChat.chatUsers::add)
        receiver.let(newChat.chatUsers::add)
        return newChat
    }

    fun createMessage(userId: Int, message: String, chatId: Int): Message {
        val currentChatId = chats.indexOfFirst { it.chatId == chatId }
        val currentChat = chats[currentChatId]
        val sender = currentChat.chatUsers.first { it.userId == userId }
        val receiver = currentChat.chatUsers.first { it.userId != userId }
        val newMessage = Message(sender.userId, allMessageIds.size + 1, message, receiver.userId)
        allMessageIds += newMessage.messageId
        val messages = currentChat.messages
        messages.add(newMessage)
        chats[currentChatId] = currentChat.copy(messages = messages)
        sender.sentMessages.add(newMessage)
        receiver.receivedMessages.add(newMessage)
        return newMessage
    }

    fun deleteChat(chatId: Int) {
        if (chatId !in 1..chats.size || chats[chatId - 1].isDeleted) throw ChatNotFoundException("Chat not found")
        val currentChatId = chats.indexOfFirst { it.chatId == chatId }
        val currentChat = chats[currentChatId]
        chats[currentChatId] = currentChat.copy(isDeleted = true)
        chats[currentChatId].messages.forEach { message ->
            if (!message.isDeleted) {
                message.isDeleted = true
                chats[currentChatId].chatUsers.forEach { user ->
                    user.sentMessages.forEach { sentMessage ->
                        if (sentMessage.messageId == message.messageId) {
                            sentMessage.isDeleted = true
                        }
                    }
                    user.receivedMessages.forEach { receivedMessage ->
                        if (receivedMessage.messageId == message.messageId) {
                            receivedMessage.isDeleted = true
                        }
                    }
                }
            }
        }
    }

    fun deleteMessage(messageId: Int) {
        if (messageId !in 1..allMessageIds.size) throw MessageNotFoundException("Message not found")
        chats.forEach { chat ->
            chat.messages.forEach { message ->
                if (message.messageId == messageId && message.isDeleted) throw MessageNotFoundException("Message not found")
                if (message.messageId == messageId && !message.isDeleted) {
                    message.isDeleted = true
                    val chatIndex: Int = chats.indexOf(chat)
                    val newMessages = chat.messages
                    chats[chatIndex] = chat.copy(messages = newMessages)
                }
            }
        }
        chats.forEach { chat ->
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
        val currentChatId = chats.indexOfFirst { it.chatId == chatId }
        val currentChat = chats[currentChatId]
        val firstMessage = currentChat.messages.first { it.messageId == firstMessageId }
        val messageIndex = currentChat.messages.indexOf(firstMessage)
        val showMessagesList = currentChat.messages.slice(messageIndex until messageIndex + messagesAmount)
        println("Чат #${currentChat.chatId} ${currentChat.chatUsers[0].userName} - ${currentChat.chatUsers[1].userName}")
        showMessagesList.forEach { message ->
            val currentUserName = users.first { it.userId == message.userId }.userName
            if (!message.isDeleted) {
                println("$currentUserName: ${message.message}")
            }
        }
        (showMessagesList.indices).forEach { i ->
            currentChat.messages.forEach {
                if (it.messageId == showMessagesList[i].messageId) {
                    it.isRead = true
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
            val currentUser: User? = chat.chatUsers.find { it.userId == userId }
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

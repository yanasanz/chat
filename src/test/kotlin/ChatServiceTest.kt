import org.junit.Assert.*
import org.junit.Test

class ChatServiceTest {

    @Test
    fun is_user_added_to_list_with_the_right_id() {
        val service = ChatService()
        service.addUser(1, "Сергей")
        service.addUser(2, "Валентина")
        val result1 = service.users[0].userId
        val result2 = service.users[1].userId
        assertEquals(1, result1)
        assertEquals(2, result2)
    }

    @Test
    fun is_chat_created() {
        val service = ChatService()
        val sergei = service.addUser(1, "Сергей")
        val valentina = service.addUser(2, "Валентина")
        service.createChat(sergei.userId, "Привет", valentina.userId)
        val message = service.chats[0].messages[0].message
        val firstChatUserName = service.chats[0].chatUsers[0].userName
        val secondChatUserName = service.chats[0].chatUsers[1].userName
        assertEquals("Привет", message)
        assertEquals("Сергей", firstChatUserName)
        assertEquals("Валентина", secondChatUserName)
    }

    @Test
    fun is_message_created_in_the_right_chat() {
        val service = ChatService()
        val sergei = service.addUser(1, "Сергей")
        val valentina = service.addUser(2, "Валентина")
        service.createChat(sergei.userId, "Привет", valentina.userId)
        service.createMessage(valentina.userId, "И тебе привет", 1)
        val message = service.chats[0].messages[1].message
        assertEquals("И тебе привет", message)
    }

    @Test
    fun are_messages_added_to_sent_messages_list_and_received_messages_list() {
        val service = ChatService()
        val sergei = service.addUser(1, "Сергей")
        val valentina = service.addUser(2, "Валентина")
        val marina = service.addUser(3, "Марина")
        service.createChat(sergei.userId, "Привет", valentina.userId)
        service.createMessage(valentina.userId, "И тебе привет", 1)
        service.createChat(valentina.userId, "Здравствуйте", marina.userId)
        val result1 = sergei.sentMessages.size
        val result2 = valentina.sentMessages.size
        val result3 = valentina.receivedMessages.size
        val result4 = marina.receivedMessages.size
        assertEquals(1, result1)
        assertEquals(2, result2)
        assertEquals(1, result3)
        assertEquals(1, result4)
    }

    @Test
    fun is_chat_and_all_corresponding_messages_deleted() {
        val service = ChatService()
        val sergei = service.addUser(1, "Сергей")
        val valentina = service.addUser(2, "Валентина")
        service.createChat(sergei.userId, "Привет", valentina.userId)
        service.createMessage(valentina.userId, "И тебе привет", 1)
        service.deleteChat(1)
        assertTrue(service.chats[0].isDeleted)
        assertTrue(service.chats[0].messages[0].isDeleted)
        assertTrue(service.chats[0].messages[1].isDeleted)
        assertTrue(sergei.sentMessages[0].isDeleted)
        assertTrue(valentina.receivedMessages[0].isDeleted)
    }

    @Test
    fun is_message_deleted() {
        val service = ChatService()
        val sergei = service.addUser(1, "Сергей")
        val valentina = service.addUser(2, "Валентина")
        service.createChat(sergei.userId, "Привет", valentina.userId)
        service.createMessage(valentina.userId, "И тебе привет", 1)
        service.deleteMessage(2)
        assertTrue(service.chats[0].messages[1].isDeleted)
    }

    @Test
    fun is_chat_deleted_when_all_chat_messages_are_deleted() {
        val service = ChatService()
        val sergei = service.addUser(1, "Сергей")
        val valentina = service.addUser(2, "Валентина")
        service.createChat(sergei.userId, "Привет", valentina.userId)
        service.createMessage(valentina.userId, "И тебе привет", 1)
        service.deleteMessage(1)
        service.deleteMessage(2)
        assertTrue(service.chats[0].isDeleted)
    }

    @Test
    fun is_get_chat_messages_fun_returns_the_right_range() {
        val service = ChatService()
        val sergei = service.addUser(1, "Сергей")
        val valentina = service.addUser(2, "Валентина")
        service.createChat(sergei.userId, "Привет", valentina.userId)
        service.createMessage(valentina.userId, "И тебе привет", 1)
        service.createMessage(sergei.userId, "Как дела?", 1)
        service.createMessage(valentina.userId, "Прекрасно", 1)
        val range = service.getChatMessages(1, 2, 2)
        val message1 = range[0].message
        val message2 = range[1].message
        val result = range.size
        assertEquals("И тебе привет", message1)
        assertEquals("Как дела?", message2)
        assertEquals(2, result)
    }

    @Test
    fun is_unread_chat_count_right() {
        val service = ChatService()
        val sergei = service.addUser(1, "Сергей")
        val valentina = service.addUser(2, "Валентина")
        val marina = service.addUser(3, "Марина")
        service.createChat(sergei.userId, "Привет", valentina.userId)
        service.createMessage(valentina.userId, "И тебе привет", 1)
        service.createChat(valentina.userId, "Здравствуйте", marina.userId)
        service.createMessage(marina.userId, "Добрый день", 2)
        service.getChatMessages(1, 1, 1)
        val result1 = service.getUnreadChatsCount(valentina.userId)
        service.getChatMessages(2, 3, 2)
        val result2 = service.getUnreadChatsCount(valentina.userId)
        assertEquals(2, result1)
        assertEquals(1, result2)
    }

    @Test(expected = ChatNotFoundException::class)
    fun should_throw_chat_not_found_exception_in_fun_delete_chat() {
        val service = ChatService()
        val sergei = service.addUser(1, "Сергей")
        val valentina = service.addUser(2, "Валентина")
        service.createChat(sergei.userId, "Привет", valentina.userId)
        service.deleteChat(2)
    }

    @Test(expected = ChatNotFoundException::class)
    fun should_throw_chat_not_found_exception_in_fun_get_chat_messages() {
        val service = ChatService()
        val sergei = service.addUser(1, "Сергей")
        val valentina = service.addUser(2, "Валентина")
        service.createChat(sergei.userId, "Привет", valentina.userId)
        service.getChatMessages(2, 2, 2)
    }

    @Test(expected = MessageNotFoundException::class)
    fun should_throw_message_not_found_exception_in_fun_delete_message() {
        val service = ChatService()
        val sergei = service.addUser(1, "Сергей")
        val valentina = service.addUser(2, "Валентина")
        service.createChat(sergei.userId, "Привет", valentina.userId)
        service.deleteMessage(2)
    }

}
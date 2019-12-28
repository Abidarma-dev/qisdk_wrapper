package jp.pepper_atelier_akihabara.qisdk_wrapper.chatbot;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbot;

public interface QLChatbotBuilder {
    public BaseChatbot build(QiContext qiContext);
}

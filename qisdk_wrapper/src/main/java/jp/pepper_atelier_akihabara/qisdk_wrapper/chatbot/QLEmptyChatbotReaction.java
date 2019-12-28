package jp.pepper_atelier_akihabara.qisdk_wrapper.chatbot;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbotReaction;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;

public class QLEmptyChatbotReaction extends BaseChatbotReaction {
    public QLEmptyChatbotReaction(QiContext context) {
        super(context);
    }

    @Override
    public void runWith(SpeechEngine speechEngine) {

    }

    @Override
    public void stop() {

    }
}

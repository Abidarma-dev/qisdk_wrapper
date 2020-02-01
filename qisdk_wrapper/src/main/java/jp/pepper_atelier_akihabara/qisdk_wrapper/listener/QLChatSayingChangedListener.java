package jp.pepper_atelier_akihabara.qisdk_wrapper.listener;

import com.aldebaran.qi.sdk.object.conversation.Phrase;

public interface QLChatSayingChangedListener {
    void onSayingChanged(String sayingPhrase);
}

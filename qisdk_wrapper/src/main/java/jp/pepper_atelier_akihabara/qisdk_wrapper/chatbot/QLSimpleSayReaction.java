package jp.pepper_atelier_akihabara.qisdk_wrapper.chatbot;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbotReaction;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;
import com.aldebaran.qi.sdk.object.locale.Locale;

import static jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper.TAG;

public class QLSimpleSayReaction extends BaseChatbotReaction {

    private String answer;
    private Locale locale;
    private Future<Void> sayFuture;

    public QLSimpleSayReaction(QiContext context, String answer, Locale locale) {
        super(context);
        this.answer = answer;
        this.locale = locale;
    }

    @Override
    public void runWith(SpeechEngine speechEngine) {
        Say say = SayBuilder.with(speechEngine).withLocale(locale).withText(answer).build();
        sayFuture = say.async().run();
        try {
            sayFuture.get();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        if(sayFuture != null) sayFuture.requestCancellation();
    }
}

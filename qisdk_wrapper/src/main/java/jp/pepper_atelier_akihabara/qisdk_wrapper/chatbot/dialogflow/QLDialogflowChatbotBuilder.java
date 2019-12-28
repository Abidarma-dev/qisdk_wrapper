package jp.pepper_atelier_akihabara.qisdk_wrapper.chatbot.dialogflow;

import android.content.Context;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbot;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;

import java.io.InputStream;

import jp.pepper_atelier_akihabara.qisdk_wrapper.chatbot.QLChatbotBuilder;

import static jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper.TAG;

public class QLDialogflowChatbotBuilder implements QLChatbotBuilder {

    private Context context;
    private int credentialsId;
    private String sessionId;
    private OnDetectedIntentListener listener;

    public QLDialogflowChatbotBuilder(Context context, int credentialsId, String sessionId) {
        this.context = context;
        this.credentialsId = credentialsId;
        this.sessionId = sessionId;
    }

    public void setOnDetectedIntentListener(OnDetectedIntentListener listener){
        this.listener = listener;
    }

    public BaseChatbot build(QiContext qiContext){
        QLDialogflowChatbot chatbot = null;
        try{
            InputStream stream = context.getResources().openRawResource(credentialsId);
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(stream);
            SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();
            SessionsClient sessionsClient = SessionsClient.create(sessionsSettings);
            SessionName sessionName = SessionName.of(credentials.getProjectId(), sessionId);

            chatbot = new QLDialogflowChatbot(qiContext, sessionsClient, sessionName);
            if(listener != null){
                chatbot.setOnDetectedIntentListener(listener);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return chatbot;
    }
}
package com.softbankrobotics.qitest;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbot;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.ReplyPriority;
import com.aldebaran.qi.sdk.object.conversation.StandardReplyReaction;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.Intent;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.protobuf.Value;

import java.util.List;
import java.util.Map;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;
import jp.pepper_atelier_akihabara.qisdk_wrapper.chatbot.QLEmptyChatbotReaction;
import jp.pepper_atelier_akihabara.qisdk_wrapper.chatbot.QLSimpleSayReaction;

import static jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper.TAG;

public class QLDialogflowChatbot extends BaseChatbot {

    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private OnDetectedIntentListener listener;

    public QLDialogflowChatbot(QiContext qiContext, SessionsClient sessionsClient, SessionName sessionName) {
        super(qiContext);
        this.sessionsClient =sessionsClient;
        this.sessionName = sessionName;
    }

    public void setOnDetectedIntentListener(OnDetectedIntentListener listener){
        this.listener = listener;
    }

    @Override
    public StandardReplyReaction replyTo(Phrase phrase, Locale locale) {
        Log.d(TAG, "QLDialogflowChatbot.replyTo: " + phrase.getText());
        StandardReplyReaction replyReaction = null;
        if(sessionsClient!= null && sessionName != null && !phrase.getText().isEmpty()) {
            try {
                TextInput textInput = TextInput.newBuilder().setText(phrase.getText()).setLanguageCode(locale.getLanguage().toString()).build();
                final QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
                DetectIntentResponse response = sessionsClient.detectIntent(sessionName, queryInput);
                
                final QueryResult queryResult = response.getQueryResult();
                final Bundle payload = getPayload(queryResult);
                if(payload != null && listener != null){
                    QLPepper.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onDetected(payload);
                        }
                    });
                }

                String answer = queryResult.getFulfillmentText();
                if (answer != null) {
                    replyReaction = new StandardReplyReaction(
                            new QLSimpleSayReaction(getQiContext(), answer, locale),
                            ReplyPriority.NORMAL);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        if(replyReaction == null){
            replyReaction =  new StandardReplyReaction(
                    new QLEmptyChatbotReaction(getQiContext()),
                    ReplyPriority.FALLBACK);
        }

        return replyReaction;
    }

    private Bundle getPayload(QueryResult queryResult){
        Bundle payload = null;
        List<Intent.Message> list = queryResult.getFulfillmentMessagesList();
        Map<String, Value> map = null;
        for(Intent.Message message: list){
            if(message.hasPayload()){
                map = message.getPayload().getFieldsMap();
                break;
            }
        }

        if(map != null){
            payload = new Bundle();
            for (Map.Entry<String, Value> entry : map.entrySet()) {
                Value value = entry.getValue();
                Value.KindCase kind = value.getKindCase();
                if(kind == Value.KindCase.NUMBER_VALUE){
                    payload.putInt(entry.getKey(), value.getNullValueValue());
                }else if(kind == Value.KindCase.BOOL_VALUE){
                    payload.putBoolean(entry.getKey(), value.getBoolValue());
                }else{
                    payload.putString(entry.getKey(), value.getStringValue());
                }
            }
        }

        return payload;
    }
}
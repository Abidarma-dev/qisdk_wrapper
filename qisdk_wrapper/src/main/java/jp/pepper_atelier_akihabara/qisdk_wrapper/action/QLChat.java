package jp.pepper_atelier_akihabara.qisdk_wrapper.action;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbot;
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Chatbot;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLAction;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;
import jp.pepper_atelier_akihabara.qisdk_wrapper.chatbot.QLChatbotBuilder;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLBookmarkReachedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLChatHeardListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLChatHearingChangedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLChatListeningChangedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLChatSayingChangedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLChatStartedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLLanguage;

public class QLChat extends QLAction<String> {

    private ArrayList<Integer> topicIdList = new ArrayList<>();
    Map<Integer, Topic> topicTable = new HashMap<>();
    private List<Topic> topicList = new ArrayList<>();
    private QLBookmarkReachedListener qlBookmarkReachedListener;
    private QLChatStartedListener qlChatStartedListener;
    private QLChatSayingChangedListener qlChatSayingChangedListener;
    private QLChatHeardListener qlChatHeardListener;
    private QLChatHearingChangedListener qlChatHearingChangedListener;
    private QLChatListeningChangedListener qlChatListeningChangedListener;
    private QLLanguage.Language language;
    private Boolean bodyLanguage = true;
    private QiChatbot qiChatbot;

    private volatile ArrayList<QLChatbotBuilder> chatbotBuilders = new ArrayList<>();
    private volatile ArrayList<Chatbot> chatbotList = new ArrayList<>();


    public QLChat(QLPepper qlPepper) {
        super(qlPepper);
        this.isAlwaysCanceled = true;
        actionTypeList.add( ActionType.Conversation);
    }

    /**
     * 言語の設定
     * デフォルトは端末設定
     * @param language
     * @return
     */
    public QLChat setLanguage(QLLanguage.Language language){
        this.language = language;
        return this;
    }

    /**
     * ボディーランゲージの有効、無効設定
     * デフォルトは有効
     * @param bodyLanguage
     * @return
     */
    public QLChat setBodyLanguage(Boolean bodyLanguage){
        this.bodyLanguage = bodyLanguage;
        return this;
    }

    /**
     * チャットに使用するトピックファイルのリソースIDの登録
     * 複数登録された場合はすべてのトピックファイルを使用してチャットを開始する
     * @param topicId
     * @return
     */
    public QLChat addResourceId(Integer topicId){
        topicIdList.add(topicId);
        return this;
    }

    /**
     * チャットに使用するトピックファイルのリソースIDの登録
     * 複数登録された場合はすべてのトピックファイルを使用してチャットを開始する
     * @param topicIdList
     * @return
     */
    public QLChat addResourceId(List<Integer> topicIdList){
        this.topicIdList.addAll(topicIdList);
        return this;
    }

    /**
     * チャットに使用するカスタムチャットボットを生成するためのビルダーを登録する
     * @param chatbot
     * @return
     */
    public QLChat addChatbotBuilder(QLChatbotBuilder chatbot){
        chatbotBuilders.add(chatbot);
        return this;
    }

    /**
     * トピックファイル内のいずれかのBookmarkに到達した時に呼ばれるリスナー
     * @param listener
     * @return
     */
    public QLChat setQlBookmarkReachedListener(QLBookmarkReachedListener listener){
        qlBookmarkReachedListener = listener;
        return this;
    }

    /**
     * チャットが開始された時に呼ばれるリスナーの設定
     * @param listener
     * @return
     */
    public QLChat setQlChatStartedListener(QLChatStartedListener listener){
        qlChatStartedListener = listener;
        return this;
    }

    /**
     * チャットが聞き取りを行った時に呼ばれるリスナーの設定
     * @param listener
     * @return
     */
    public QLChat setQlChatHeardListener(QLChatHeardListener listener){
        qlChatHeardListener = listener;
        return this;
    }

    /**
     * チャットが発話を行った時に呼ばれるリスナーの設定
     * @param listener
     * @return
     */
    public QLChat setQlChatSayingChangedListener(QLChatSayingChangedListener listener){
        qlChatSayingChangedListener = listener;
        return this;
    }

    /**
     * チャットが聞き取り状態が変わった時に呼ばれるリスナーの設定
     * @param listener
     * @return
     */
    public QLChat setQlChatHearingChangedListener(QLChatHearingChangedListener listener){
        qlChatHearingChangedListener = listener;
        return this;
    }

    /**
     * チャットが聞き取り状態が変わった時に呼ばれるリスナーの設定
     * @param listener
     * @return
     */
    public QLChat setQlChatListeningChangedListener(QLChatListeningChangedListener listener){
        qlChatListeningChangedListener = listener;
        return this;
    }

    @Override
    protected Future<Void> execute() {
        Future<Void> futureVoid = null;

        buildQLChatbot();

        if(topicIdList.size() > 0){
            for(final int topicId: topicIdList){
                futureVoid = buildTopic(topicId, futureVoid);
            }
            futureVoid = buildQiChatbot(futureVoid);
        }
        futureVoid = runChat(futureVoid);

        return futureVoid;
    }

    @Override
    protected Boolean validate() {
        if(topicIdList.isEmpty() && chatbotBuilders.size() == 0) return false;

        if(!topicIdList.isEmpty()){
            // 重複削除
            topicIdList = new ArrayList<>(new LinkedHashSet<>(topicIdList));
            for(int current: topicIdList){
                if(current <= 0){
                    return false;
                }
            }

        }
        return true;
    }

    private void buildQLChatbot(){
        if(chatbotBuilders.size() > 0){
            for(QLChatbotBuilder builder: chatbotBuilders){
                chatbotList.add(builder.build(qiContext));
            }
        }
    }

    private Future<Void> buildTopic(final int topicId, Future<Void> futureVoid){
        Future<Topic> futureTopic;
        if(futureVoid == null){
            futureTopic = TopicBuilder.with(qiContext).withResource(topicId).buildAsync();
        }else{
            futureTopic = futureVoid.andThenCompose(new Function<Void, Future<Topic>>() {
                @Override
                public Future<Topic> execute(Void aVoid) throws Throwable {
                    return TopicBuilder.with(qiContext).withResource(topicId).buildAsync();
                }
            });
        }
        return futureTopic.andThenConsume(new Consumer<Topic>() {
            @Override
            public void consume(Topic topic) throws Throwable {
                topicList.add(topic);
                topicTable.put(topicId, topic);
            }
        });
    }

    private Future<Void> buildQiChatbot(Future<Void> futureVoid){
        return futureVoid.andThenCompose(new Function<Void, Future<QiChatbot>>() {
            @Override
            public Future<QiChatbot> execute(Void aVoid) {
                QiChatbotBuilder qiChatbotBuilder = QiChatbotBuilder.with(qiContext).withTopics(topicList);
                if(language!= null){
                    qiChatbotBuilder.withLocale(QLLanguage.makeLocale(language));
                }
                return qiChatbotBuilder.buildAsync();
            }
        }).andThenConsume(new Consumer<QiChatbot>() {
            @Override
            public void consume(QiChatbot qiChatbot) {
                QLChat.this.qiChatbot = qiChatbot;
                if(!bodyLanguage){
                    qiChatbot.setSpeakingBodyLanguage(BodyLanguageOption.DISABLED);
                }

                if (qlBookmarkReachedListener != null) {
                    qiChatbot.addOnBookmarkReachedListener(new QiChatbot.OnBookmarkReachedListener() {
                        @Override
                        public void onBookmarkReached(final Bookmark bookmark) {
                            final String bookmarkName = bookmark.getName();
                            qlPepper.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    qlBookmarkReachedListener.onBookmarkReached(bookmarkName);
                                }
                            });
                        }
                    });
                }

                qiChatbot.addOnEndedListener(new QiChatbot.OnEndedListener() {
                    @Override
                    public void onEnded(String endReason) {
                        isSuccess = true;
                        actionResult = endReason;
                        if (future != null) future.requestCancellation();
                    }
                });

                chatbotList.add(0, qiChatbot);
            }
        });
    }

    private Future<Void> runChat(Future<Void> futureVoid){
        Future<Chat> futureChat;
        if(futureVoid == null){
            // カスタムチャットボットのみの場合はfutureVoidがnullになる
            ChatBuilder builder  = ChatBuilder.with(qiContext).withChatbots(chatbotList);
            if(language!= null){
                builder.withLocale(QLLanguage.makeLocale(language));
            }
            futureChat = builder.buildAsync();
        }else{
            futureChat = futureVoid.andThenCompose(new Function<Void, Future<Chat>>() {
                @Override
                public Future<Chat> execute(Void aVoid) {
                    ChatBuilder builder  = ChatBuilder.with(qiContext).withChatbots(chatbotList);
                    if(language!= null){
                        builder.withLocale(QLLanguage.makeLocale(language));
                    }
                    return builder.buildAsync();
                }
            });
        }

        return futureChat.andThenCompose(new Function<Chat, Future<Void>>() {
            @Override
            public Future<Void> execute(final Chat chat) throws Throwable {
                if(!bodyLanguage){
                    chat.setListeningBodyLanguage(BodyLanguageOption.DISABLED);
                }
                if (qlChatStartedListener != null){
                    chat.addOnStartedListener(new Chat.OnStartedListener() {
                        @Override
                        public void onStarted() {
                            qlPepper.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    qlChatStartedListener.onStarted();
                                }
                            });
                        }
                    });
                }

                if (qlChatHeardListener != null){
                    chat.addOnHeardListener(new Chat.OnHeardListener() {
                        @Override
                        public void onHeard(final Phrase heardPhrase) {
                            qlPepper.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    qlChatHeardListener.onHeard(heardPhrase.getText());
                                }
                            });
                        }
                    });
                }

                if (qlChatSayingChangedListener != null){
                    chat.addOnSayingChangedListener(new Chat.OnSayingChangedListener() {
                        @Override
                        public void onSayingChanged(final Phrase sayingPhrase) {
                            qlPepper.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    qlChatSayingChangedListener.onSayingChanged(sayingPhrase.getText());
                                }
                            });
                        }
                    });
                }

                if(qlChatHearingChangedListener != null){
                    chat.addOnHearingChangedListener(new Chat.OnHearingChangedListener() {
                        @Override
                        public void onHearingChanged(final Boolean hearing) {
                            qlPepper.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    qlChatHearingChangedListener.onHearingChanged(hearing);
                                }
                            });
                        }
                    });
                }

                if(qlChatListeningChangedListener != null){
                    chat.addOnListeningChangedListener(new Chat.OnListeningChangedListener() {
                        @Override
                        public void onListeningChanged(final Boolean listening) {
                            qlPepper.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    qlChatListeningChangedListener.onListeningChanged(listening);
                                }
                            });
                        }
                    });
                }


                return chat.async().run();
            }
        });
    }

    /**
     * チャット実行中に呼ぶことで、指定のBookmarkに即座に会話を移す
     * @param topicId
     * @param bookmarkName
     */
    public void goTo(int topicId, final String bookmarkName){
        if(qiChatbot == null) return;

        Topic topic = topicTable.get(topicId);
        if(topic != null){
            topic.async().getBookmarks().andThenConsume(new Consumer<Map<String, Bookmark>>() {
                @Override
                public void consume(Map<String, Bookmark> stringBookmarkMap) throws Throwable {
                    Bookmark bookmark = stringBookmarkMap.get(bookmarkName);
                    if(bookmark != null) qiChatbot.async().goToBookmark(bookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
                }
            });
        }
    }
}

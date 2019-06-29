package jp.pepper_atelier_akihabara.qisdk_wrapper.action;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLAction;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLBookmarkReachedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLChatStartedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLLanguage;

public class QLChat extends QLAction<String> {

    private ArrayList<Integer> topicIdList = new ArrayList<>();
    Map<Integer, Topic> topicTable = new HashMap<>();
    private List<Topic> topicList = new ArrayList<>();
    private QLBookmarkReachedListener qlBookmarkReachedListener;
    private QLChatStartedListener qlChatStartedListener;
    private QLLanguage.Language language;
    private Boolean bodyLanguage = true;
    private QiChatbot qiChatbot;
    private Future<Void> futureQiChatbot = null;

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
     * トピックファイル内のいずれかのBookmarkに到達した時に呼ばれるリスナー
     * @param listener
     * @return
     */
    public QLChat setQlBookmarkReachedListener(QLBookmarkReachedListener listener){
        qlBookmarkReachedListener = listener;
        return this;
    }

    /**
     * チャット外資された時に呼ばれるリスナーの設定
     * @param listener
     * @return
     */
    public QLChat setQlChatStartedListener(QLChatStartedListener listener){
        qlChatStartedListener = listener;
        return this;
    }

    @Override
    protected Future<Void> execute() {
        Future<Void> futureVoid = null;
        for(final int topicId: topicIdList){
            futureVoid = buildTopic(topicId, futureVoid);
        }

        if(futureVoid != null){
            futureVoid = runChat(futureVoid);
        }

        return futureVoid;
    }

    @Override
    protected Boolean validate() {
        if(topicIdList.isEmpty()){
            return false;
        }else{
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

    private Future<Void> runChat(Future<Void> futureVoid){
        if(futureVoid == null) return null;

        return futureVoid.andThenCompose(new Function<Void, Future<QiChatbot>>() {
            @Override
            public Future<QiChatbot> execute(Void aVoid) throws Throwable {
                QiChatbotBuilder qiChatbotBuilder = QiChatbotBuilder.with(qiContext).withTopics(topicList);
                if(language!= null){
                    qiChatbotBuilder.withLocale(QLLanguage.makeLocale(language));
                }
                return qiChatbotBuilder.buildAsync();
            }
        }).andThenCompose(new Function<QiChatbot, Future<Chat>>() {
            @Override
            public Future<Chat> execute(QiChatbot qiChatbot) throws Throwable {
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
                        if (futureQiChatbot != null) futureQiChatbot.requestCancellation();
                    }
                });

                ChatBuilder builder = ChatBuilder.with(qiContext).withChatbot(qiChatbot);
                if(language!= null){
                    builder.withLocale(QLLanguage.makeLocale(language));
                }
                return builder.buildAsync();
            }
        }).andThenCompose(new Function<Chat, Future<Void>>() {
            @Override
            public Future<Void> execute(final Chat chat) throws Throwable {
                if(!bodyLanguage){
                    chat.setListeningBodyLanguage(BodyLanguageOption.DISABLED);
                }

                if (qlChatStartedListener != null){
                    chat.addOnStartedListener(new Chat.OnStartedListener() {
                        @Override
                        public void onStarted() {
                            qlChatStartedListener.onStarted();
                        }
                    });
                }

                return chat.async().run();
            }
        }).thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> future) throws Throwable {
                // nop
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

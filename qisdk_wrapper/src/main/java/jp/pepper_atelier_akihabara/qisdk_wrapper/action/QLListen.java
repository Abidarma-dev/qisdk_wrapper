package jp.pepper_atelier_akihabara.qisdk_wrapper.action;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;

import java.util.ArrayList;
import java.util.List;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLAction;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLLanguage;

public class QLListen extends QLAction<String> {
    private List<Phrase> listenPhraseList = new ArrayList<>();
    private QLLanguage.Language language;
    private Boolean bodyLanguage = true;

    public QLListen(QLPepper qlPepper) {
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
    public QLListen setLanguage(QLLanguage.Language language){
        this.language = language;
        return this;
    }

    /**
     * ボディーランゲージの有効、無効設定
     * デフォルトは有効
     * @param bodyLanguage
     * @return
     */
    public QLListen setBodyLanguage(Boolean bodyLanguage){
        this.bodyLanguage = bodyLanguage;
        return this;
    }

    /**
     * 聞き取るフレーズの登録
     * @param listenPhrase
     * @return
     */
    public QLListen addPhrase(String listenPhrase){
        listenPhraseList.add(new Phrase(listenPhrase));
        return this;
    }

    /**
     * 聞き取るフレーズの登録
     * @param listenPhraseList
     * @return
     */
    public QLListen addPhrase(List<String> listenPhraseList){
        for(String listenPhrase: listenPhraseList){
            addPhrase(listenPhrase);
        }
        return this;
    }

    @Override
    protected Future<Void> execute(){
        Future<Void> futureVoid = null;

        futureVoid = PhraseSetBuilder.with(qiContext).withPhrases(listenPhraseList).buildAsync()
                .andThenCompose(new Function<PhraseSet, Future<Listen>>() {
                    @Override
                    public Future<Listen> execute(PhraseSet phraseSet) throws Throwable {
                        ListenBuilder builder = ListenBuilder.with(qiContext).withPhraseSet(phraseSet);
                        if(!bodyLanguage){
                            builder.withBodyLanguageOption(BodyLanguageOption.DISABLED);
                        }
                        if(language != null){
                            builder.withLocale(QLLanguage.makeLocale(language));
                        }
                        return builder.buildAsync();
                    }
                }).andThenCompose(new Function<Listen, Future<ListenResult>>() {
                    @Override
                    public Future<ListenResult> execute(Listen listen) throws Throwable {
                        return listen.async().run();
                    }
                }).andThenConsume(new Consumer<ListenResult>() {
                    @Override
                    public void consume(ListenResult listenResult) throws Throwable {
                        isSuccess = true;
                        actionResult = listenResult.getHeardPhrase().getText();
                    }
                }).thenConsume(new Consumer<Future<Void>>() {
                    @Override
                    public void consume(Future<Void> future) throws Throwable {
                        // nop
                    }
                });

        return futureVoid;
    }

    @Override
    protected Boolean validate() {
        if(listenPhraseList.isEmpty()){
            return false;
        }
        return true;
    }
}

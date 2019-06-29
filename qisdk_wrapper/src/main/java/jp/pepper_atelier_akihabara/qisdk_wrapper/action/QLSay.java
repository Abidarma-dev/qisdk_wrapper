package jp.pepper_atelier_akihabara.qisdk_wrapper.action;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Say;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLAction;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLLanguage;

public class QLSay extends QLAction<Void> {
    private static final int NO_ANIMATION = -1;

    private QLLanguage.Language language = null;
    private Boolean bodyLanguage = true;
    private Queue<QLPhrase> phraseQueue = new ArrayDeque<>();

    public QLSay(QLPepper qlPepper) {
        super(qlPepper);
        actionTypeList.add(ActionType.Conversation);
    }

    /**
     * 言語の設定
     * デフォルトは端末設定
     * @param language
     * @return
     */
    public QLSay setLanguage(QLLanguage.Language language){
        this.language = language;
        return this;
    }

    /**
     * ボディーランゲージの有効、無効設定
     * デフォルトは有効
     * @param bodyLanguage
     * @return
     */
    public QLSay setBodyLanguage(Boolean bodyLanguage){
        this.bodyLanguage = bodyLanguage;
        return this;
    }

    /**
     * 発話とモーションの内容を設定
     * 複数登録された場合は順次実行
     * @param phrase
     * @param animationId アニメーションファイルのリソースID
     * @return
     */
    public QLSay addPhrase(String phrase, Integer animationId){
        if(phrase == null) phrase = "";
        if(!phrase.isEmpty() || animationId != NO_ANIMATION){
            phraseQueue.add(new QLPhrase(phrase, animationId));
        }
        return this;
    }

    /**
     * 発話の内容を設定
     * 複数登録された場合は順次実行
     * @param phrase
     * @return
     */
    public QLSay addPhrase(String phrase){
        addPhrase(phrase, NO_ANIMATION);
        return this;
    }

    /**
     * 発話の内容を設定
     * 複数登録された場合は順次実行
     * @param phraseList
     * @return
     */
    public QLSay addPhrase(List<String> phraseList){
        for(int i=0; i<phraseList.size(); i++){
            addPhrase(phraseList.get(i));
        }
        return this;
    }

    /**
     * 発話とモーションの内容を設定
     * 複数登録された場合は順次実行
     * @param phraseList
     * @param animationIdList アニメーションファイルのリソースID
     * @return
     */
    public QLSay addPhrase(List<String> phraseList, List<Integer> animationIdList){
        int phraseSize = phraseList.size();
        int animationSize = animationIdList.size();
        int size = phraseSize;
        if(size < animationSize) size = animationSize;

        for(int i=0; i < size; i++){
            String phrase = "";
            Integer animationId = NO_ANIMATION;
            if(phraseSize > i) phrase = phraseList.get(i);
            if(animationSize > i) animationId = animationIdList.get(i);
            addPhrase(phrase, animationId);
        }
        return this;
    }

    /**
     * 発話とモーションの内容を設定
     * 複数登録された場合は順次実行
     * @param phraseResourceId 発話内容のリソースID
     * @param animationId アニメーションファイルのリソースID
     * @return
     */
    public QLSay addResourceId(Integer phraseResourceId, Integer animationId){
        String phrase = "";
        try{
            phrase = qlPepper.getStringResource(phraseResourceId);
        }catch (Exception e){
            // nop
        }
        return addPhrase(phrase, animationId);
    }

    /**
     * 発話の内容を設定
     * 複数登録された場合は順次実行
     * @param phraseResourceId 発話内容のリソースID
     * @return
     */
    public QLSay addResourceId(Integer phraseResourceId){
        return addResourceId(phraseResourceId, NO_ANIMATION);
    }

    /**
     * 発話の内容を設定
     * 複数登録された場合は順次実行
     * @param phraseResourceIdList 発話内容のリソースID
     * @return
     */
    public QLSay addResourceId(List<Integer> phraseResourceIdList){
        for(int i=0; i<phraseResourceIdList.size(); i++){
            addResourceId(phraseResourceIdList.get(i));
        }
        return this;
    }

    /**
     * 発話とモーションの内容を設定
     * 複数登録された場合は順次実行
     * @param phraseResourceIdList 発話内容のリソースID
     * @param animationIdList アニメーションファイルのリソースID
     * @return
     */
    public QLSay addResourceId(List<Integer> phraseResourceIdList, List<Integer> animationIdList){
        int phraseResourceIdSize = phraseResourceIdList.size();
        int animationSize = animationIdList.size();
        int size = phraseResourceIdSize;
        if(size < animationSize) size = animationSize;

        for(int i=0; i < size; i++){
            Integer animationId = NO_ANIMATION;
            if(animationSize > i) animationId = animationIdList.get(i);

            if(phraseResourceIdSize > i) {
                addResourceId(phraseResourceIdList.get(i), animationId);
            }else{
                addPhrase("", animationId);
            }
        }
        return this;
    }

    @Override
    protected Future<Void> execute(){
        Future<Void> futureVoid = null;
        while(true){
            final QLPhrase current = phraseQueue.poll();
            if(current == null) break;

            if(!current.phrase.isEmpty() && current.animationId != NO_ANIMATION){
                futureVoid = runSayWithAnimate(current.phrase, current.animationId, futureVoid);
            }else if(!current.phrase.isEmpty()){
                futureVoid = runSay(current.phrase, false, futureVoid);
            }else if(current.animationId != NO_ANIMATION){
                futureVoid = runAnimate(current.animationId, futureVoid);
            }
        }
        return futureVoid;
    }

    @Override
    protected Boolean validate() {
        if(phraseQueue.isEmpty()) return false;
        for (QLPhrase phrase: phraseQueue){
            if(phrase.animationId == 0) return false;
        }
        return true;
    }

    private Future<Void> runSay(String phrase, Boolean hasAnimation, Future<Void> futureVoid){
        final SayBuilder action = SayBuilder.with(qiContext).withText(phrase);
        if(language!= null){
            action.withLocale(QLLanguage.makeLocale(language));
        }

        if(!bodyLanguage || hasAnimation){
            action.withBodyLanguageOption(BodyLanguageOption.DISABLED);
        }

        Future<Say> futureSay;
        if(futureVoid == null){
            futureSay = action.buildAsync();
        }else{
            futureSay = futureVoid.andThenCompose(new Function<Void, Future<Say>>() {
                @Override
                public Future<Say> execute(Void aVoid) throws Throwable {
                    return action.buildAsync();
                }
            });
        }

        return futureSay.andThenCompose(new Function<Say, Future<Void>>() {
            @Override
            public Future<Void> execute(Say say) throws Throwable {
                return say.async().run();
            }
        });
    }

    private Future<Void> runAnimate(final int animationId, Future<Void> futureVoid){
        Future<Animation> futureAnimation;
        if(futureVoid == null){
            futureAnimation = AnimationBuilder.with(qiContext).withResources(animationId).buildAsync();

        }else{
            futureAnimation = futureVoid.andThenCompose(new Function<Void, Future<Animation>>() {
                @Override
                public Future<Animation> execute(Void aVoid) throws Throwable {
                    return AnimationBuilder.with(qiContext).withResources(animationId).buildAsync();
                }
            });
        }

        return futureAnimation.andThenCompose(new Function<Animation, Future<Animate>>() {
            @Override
            public Future<Animate> execute(Animation animation) throws Throwable {
                return AnimateBuilder.with(qiContext).withAnimation(animation).buildAsync();
            }
        }).andThenCompose(new Function<Animate, Future<Void>>() {
            @Override
            public Future<Void> execute(Animate animate) throws Throwable {
                return animate.async().run();
            }
        }).thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> voidFuture) throws Throwable {
                // nop Animateのエラーではアクションの連鎖を止めない
            }
        });

    }

    private Future<Void> runSayWithAnimate(String phrase, int animationId, Future<Void> futureVoid){
        return Future.waitAll(runSay(phrase, true, futureVoid), runAnimate(animationId, futureVoid));
    }

    class QLPhrase {
        public String phrase;
        public int animationId;

        public QLPhrase(String phrase, int animationId){
            this.phrase = phrase;
            this.animationId = animationId;
        }
    }
}

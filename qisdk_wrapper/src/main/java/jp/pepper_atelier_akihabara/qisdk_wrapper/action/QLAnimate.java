package jp.pepper_atelier_akihabara.qisdk_wrapper.action;

import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;

import java.util.ArrayList;
import java.util.List;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLAction;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLLabelReachedListener;

public class QLAnimate extends QLAction<Void> {
    public static final int NO_ANIMATION = -1;
    public static final int XML_ANIMATION = -2;

    protected List<QLAnimation> animationIdList = new ArrayList<>();
    protected QLLabelReachedListener qlLabelReachedListener = null;

    public QLAnimate(QLPepper qlPepper) {
        super(qlPepper);
        actionTypeList.add(ActionType.Animate);
    }

    /**
     * 実行するアニメーションのリソースIDの登録
     * 複数登録された場合は順次実行
     * @param animationId
     * @return
     */
    public QLAnimate addResourceId(Integer animationId){
        animationIdList.add(new QLAnimation(animationId));
        return this;
    }

    /**
     * 実行するアニメーションのXMLの登録
     * 複数登録された場合は順次実行
     * @param animationXml
     * @return
     */
    public QLAnimate addAnimationXml(String animationXml){
        animationIdList.add(new QLAnimation(animationXml));
        return this;
    }

    /**
     * 実行するアニメーションのリソースIDの登録
     * 複数登録された場合は順次実行
     * @param animationIdList
     * @return
     */
    public QLAnimate addResourceId(List<Integer> animationIdList){
        for(int i=0; i<animationIdList.size(); i++){
            addResourceId(animationIdList.get(i));
        }
        return this;
    }

    /**
     * Animation内のいずれかのLabelに到達した時に呼ばれるリスナー
     * @param listener
     * @return
     */
    public QLAnimate setLabelReachedListener(QLLabelReachedListener listener){
        qlLabelReachedListener = listener;
        return this;
    }

    @Override
    protected Future<Void> execute(){
        Future<Void> futureVoid = null;
        for(int i=0; i<animationIdList.size(); i++){
            final QLAnimation animation = animationIdList.get(i);
            futureVoid = runAnimate(animation, futureVoid);
        }
        return futureVoid;
    }

    @Override
    protected Boolean validate() {
        if(animationIdList.isEmpty()){
            return false;
        }else{
            for(QLAnimation current: animationIdList){
                if((current.animationId <= 0 && current.animationId != XML_ANIMATION) ||
                        (current.animationId == XML_ANIMATION && (current.animationXml == null || current.animationXml.isEmpty())))
                {
                    return false;
                }
            }
        }
        return true;
    }

    protected Future<Void> runAnimate(final QLAnimation animation, Future<Void> futureVoid){
        Future<Animation> futureAnimation;
        if(futureVoid == null){
            if(animation.animationId == XML_ANIMATION){
                futureAnimation = AnimationBuilder.with(qiContext).withTexts(animation.animationXml).buildAsync();
            }else{
                futureAnimation = AnimationBuilder.with(qiContext).withResources(animation.animationId).buildAsync();
            }
        }else{
            if(animation.animationId == XML_ANIMATION) {
                futureAnimation = futureVoid.andThenCompose(new Function<Void, Future<Animation>>() {
                    @Override
                    public Future<Animation> execute(Void aVoid) throws Throwable {
                        return AnimationBuilder.with(qiContext).withTexts(animation.animationXml).buildAsync();
                    }
                });
            }else{
                futureAnimation = futureVoid.andThenCompose(new Function<Void, Future<Animation>>() {
                    @Override
                    public Future<Animation> execute(Void aVoid) throws Throwable {
                        return AnimationBuilder.with(qiContext).withResources(animation.animationId).buildAsync();
                    }
                });
            }
        }

        return futureAnimation.andThenCompose(new Function<Animation, Future<Animate>>() {
                    @Override
                    public Future<Animate> execute(Animation animation) throws Throwable {
                        return AnimateBuilder.with(qiContext).withAnimation(animation).buildAsync();
                    }
                }).andThenCompose(new Function<Animate, Future<Void>>() {
                    @Override
                    public Future<Void> execute(Animate animate) throws Throwable {
                        if(qlLabelReachedListener != null){
                            animate.addOnLabelReachedListener(new Animate.OnLabelReachedListener() {
                                @Override
                                public void onLabelReached(final String s, Long aLong) {
                                    qlPepper.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            qlLabelReachedListener.onLabelReached(s);
                                        }
                                    });
                                }
                            });
                        }
                        return animate.async().run();
                    }
                });
    }

    class QLAnimation {
        public int animationId;
        public String animationXml;

        public QLAnimation(int animationId){
            this.animationId = animationId;
        }

        public QLAnimation(String animationXml){
            this.animationXml = animationXml;
            this.animationId = XML_ANIMATION;
        }
    }
}

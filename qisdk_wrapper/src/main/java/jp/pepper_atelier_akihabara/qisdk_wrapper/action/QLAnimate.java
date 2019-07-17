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

    protected List<Integer> animationIdList = new ArrayList<>();
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
        animationIdList.add(animationId);
        return this;
    }

    /**
     * 実行するアニメーションのリソースIDの登録
     * 複数登録された場合は順次実行
     * @param animationIdList
     * @return
     */
    public QLAnimate addResourceId(List<Integer> animationIdList){
        this.animationIdList.addAll(animationIdList);
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
            final int animationId = animationIdList.get(i);
            futureVoid = runAnimate(animationId, futureVoid);
        }
        return futureVoid;
    }

    @Override
    protected Boolean validate() {
        if(animationIdList.isEmpty()){
            return false;
        }else{
            for(int current: animationIdList){
                if(current <= 0){
                    return false;
                }
            }
        }
        return true;
    }

    protected Future<Void> runAnimate(final int animationId, Future<Void> futureVoid){
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
}

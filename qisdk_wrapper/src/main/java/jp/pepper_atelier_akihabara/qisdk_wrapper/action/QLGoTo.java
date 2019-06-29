package jp.pepper_atelier_akihabara.qisdk_wrapper.action;

import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.geometry.Transform;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLAction;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLHuman;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLFrame;

public class QLGoTo extends QLFrameAction {

    public QLGoTo(QLPepper qlPepper) {
        super(qlPepper);
        actionTypeList.add( ActionType.Move);
    }

    /**
     * 移動先の指定
     * @param qlHuman 基点となる位置
     * @param locationX 移動先の座標。基点を中心に指定メートル先に移動。マイナスは後ろ。
     * @param locationY 移動先の座標。基点を中心に指定メートル右に移動。マイナスは左。
     * @return
     */
    public QLGoTo setDestination(QLHuman qlHuman, double locationX, double locationY){
        this.qlHuman = qlHuman;
        this.qlFrame = null;
        this.transform = TransformBuilder.create().from2DTranslation(locationX, locationY);
        return this;
    }

    /**
     * 移動先の指定
     * @param qlFrame 基点となる位置
     * @param locationX 移動先の座標。基点を中心に指定メートル先に移動。マイナスは後ろ。
     * @param locationY 移動先の座標。基点を中心に指定メートル右に移動。マイナスは左。
     * @return
     */
    public QLGoTo setDestination(QLFrame qlFrame, double locationX, double locationY){
        this.qlFrame = qlFrame;
        this.qlHuman = null;
        this.transform = TransformBuilder.create().from2DTranslation(locationX, locationY);
        return this;
    }

    @Override
    protected Future<Void> execute() {
        Future<Void> futureVoid = null;
        futureVoid = makeFrame();
        if(futureVoid != null){
            futureVoid = runGoTo(futureVoid);
        }
        return futureVoid;
    }

    private Future<Void> runGoTo(Future<Void> futureVoid){
        return futureVoid.andThenCompose(new Function<Void, Future<Frame>>() {
            @Override
            public Future<Frame> execute(Void aVoid) throws Throwable {
                return targetFreeFrame.async().frame();
            }
        }).andThenCompose(new Function<Frame, Future<GoTo>>() {
            @Override
            public Future<GoTo> execute(Frame frame) throws Throwable {
                return GoToBuilder.with(qiContext).withFrame(frame).buildAsync();
            }
        }).andThenCompose(new Function<GoTo, Future<Void>>() {
            @Override
            public Future<Void> execute(GoTo goTo) throws Throwable {
                return goTo.async().run();
            }
        });
    }
}

package jp.pepper_atelier_akihabara.qisdk_wrapper.action;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.LookAtBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.LookAt;
import com.aldebaran.qi.sdk.object.actuation.LookAtMovementPolicy;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.geometry.Vector3;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLAction;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLHuman;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLFrame;

public class QLLookAt extends QLFrameAction {

    private LookAtMovementPolicy movementPolicy = LookAtMovementPolicy.HEAD_AND_BASE;

    public QLLookAt(QLPepper qlPepper) {
        super(qlPepper);
        isAlwaysCanceled = true;
        actionTypeList.add(ActionType.Move);
        actionTypeList.add(ActionType.Animate);
    }

    /**
     * 移動先の指定
     * @param qlHuman 基点となる位置
     * @param locationX 向き先の座標。基点を中心に指定メートル前を向く。マイナスは後ろ。
     * @param locationY 向き先の座標。基点を中心に指定メートル右を向く。マイナスは左。
     * @return
     */
    public QLLookAt setDestination(QLHuman qlHuman, double locationX, double locationY){
        return setDestination(qlHuman, locationX, locationY, 1.2);
    }

    /**
     * 移動先の指定
     * @param qlFrame 基点となる位置
     * @param locationX 向き先の座標。基点を中心に指定メートル前を向く。マイナスは後ろ。
     * @param locationY 向き先の座標。基点を中心に指定メートル右を向く。マイナスは左。
     * @return
     */
    public QLLookAt setDestination(QLFrame qlFrame, double locationX, double locationY){
        return setDestination(qlFrame, locationX, locationY, 1.2);
    }

    /**
     * 向き先の指定
     * @param qlHuman 基点となる位置
     * @param locationX 向き先の座標。基点を中心に指定メートル前を向く。マイナスは後ろ。
     * @param locationY 向き先の座標。基点を中心に指定メートル右を向く。マイナスは左。
     * @param locationZ 向き先の座標。基点を中心に指定メートル上方を向く。
     * @return
     */
    public QLLookAt setDestination(QLHuman qlHuman, double locationX, double locationY, double locationZ){
        this.qlHuman = qlHuman;
        this.qlFrame = null;
        this.transform = TransformBuilder.create().fromTranslation(new Vector3(locationX, locationY, locationZ));
        return this;
    }

    /**
     * 向き先の指定
     * @param qlFrame 基点となる位置
     * @param locationX 向き先の座標。基点を中心に指定メートル前を向く。マイナスは後ろ。
     * @param locationY 向き先の座標。基点を中心に指定メートル右を向く。マイナスは左。
     * @param locationZ 向き先の座標。基点を中心に指定メートル上方を向く。
     * @return
     */
    public QLLookAt setDestination(QLFrame qlFrame, double locationX, double locationY, double locationZ){
        this.qlFrame = qlFrame;
        this.qlHuman = null;
        this.transform = TransformBuilder.create().fromTranslation(new Vector3(locationX, locationY, locationZ));
        return this;
    }

    /**
     * 首だけで指定の方向を向くように設定
     * @return
     */
    public QLLookAt setHeadOnlyMovement(){
        this.actionTypeList.remove(ActionType.Move);
        this.movementPolicy = LookAtMovementPolicy.HEAD_ONLY;
        return this;
    }

    @Override
    protected Future<Void> execute() {
        Future<Void> futureVoid = null;
        futureVoid = makeFrame();
        if(futureVoid != null){
            futureVoid = runLookAt(futureVoid);
        }
        return futureVoid;
    }

    private Future<Void> runLookAt(Future<Void> futureVoid){
        return futureVoid.andThenCompose(new Function<Void, Future<Frame>>() {
            @Override
            public Future<Frame> execute(Void aVoid) throws Throwable {
                return targetFreeFrame.async().frame();
            }
        }).andThenCompose(new Function<Frame, Future<LookAt>>() {
            @Override
            public Future<LookAt> execute(Frame frame) throws Throwable {
                return LookAtBuilder.with(qiContext).withFrame(frame).buildAsync();
            }
        }).andThenCompose(new Function<LookAt, Future<Void>>() {
            @Override
            public Future<Void> execute(LookAt lookAt) throws Throwable {
                lookAt.setPolicy(movementPolicy);
                isSuccess = true;
                return lookAt.async().run();
            }
        }).thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> future) throws Throwable {
                // nop
            }
        });
    }
}

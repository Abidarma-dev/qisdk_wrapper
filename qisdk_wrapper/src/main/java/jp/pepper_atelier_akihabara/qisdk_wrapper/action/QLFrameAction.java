package jp.pepper_atelier_akihabara.qisdk_wrapper.action;

import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.LookAtBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.LookAt;
import com.aldebaran.qi.sdk.object.actuation.LookAtMovementPolicy;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.geometry.Vector3;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLAction;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLFrame;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLHuman;

public abstract class QLFrameAction extends QLAction<Void> {

    protected FreeFrame targetFreeFrame = null;
    protected Frame baseFrame = null;
    protected Transform transform;
    protected QLHuman qlHuman = null;
    protected QLFrame qlFrame = null;

    public QLFrameAction(QLPepper qlPepper) {
        super(qlPepper);
    }

    protected Future<Void> makeFrame(){
        Future<Frame> futureFrame = null;

        if(qlHuman != null){
            futureFrame = qlHuman.getHuman().async().getHeadFrame();
        }else if(qlFrame != null){
            switch (qlFrame.getType()){
                case QLFrame.FRAME_TYPE_ROBOT:
                    futureFrame = qiContext.getActuation().async().robotFrame();
                    break;
                case QLFrame.FRAME_TYPE_GAZE:
                    futureFrame = qiContext.getActuation().async().gazeFrame();
                    break;
                case QLFrame.FRAME_TYPE_MAP:
                    futureFrame = qiContext.getMapping().async().mapFrame();
                    break;
            }
        }

        if(futureFrame != null){
            return futureFrame.andThenCompose(new Function<Frame, Future<FreeFrame>>() {
                @Override
                public Future<FreeFrame> execute(Frame frame) throws Throwable {
                    baseFrame = frame;
                    return qiContext.getMapping().async().makeFreeFrame();
                }
            }).andThenCompose(new Function<FreeFrame, Future<Void>>() {
                @Override
                public Future<Void> execute(FreeFrame freeFrame) throws Throwable {
                    targetFreeFrame = freeFrame;
                    long timestamp = 0L;
                    if(qlFrame != null) timestamp = qlFrame.getTimestamp();
                    return targetFreeFrame.async().update(baseFrame, transform, timestamp);
                }
            });
        }

        return null;
    }

    @Override
    protected Boolean validate() {
        if(this.transform == null || (qlFrame == null && qlHuman == null)){
            return false;
        }
        return true;
    }
}

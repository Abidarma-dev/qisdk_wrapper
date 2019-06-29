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

public class QLTrajectory extends QLAnimate {

    public QLTrajectory(QLPepper qlPepper) {
        super(qlPepper);
        actionTypeList = new ArrayList<>();
        actionTypeList.add(ActionType.Move);
    }
}

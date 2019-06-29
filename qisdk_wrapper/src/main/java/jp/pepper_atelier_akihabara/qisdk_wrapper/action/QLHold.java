package jp.pepper_atelier_akihabara.qisdk_wrapper.action;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLAction;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;

public class QLHold extends QLAction<Void> {

    private Holder holder;

    public QLHold(QLPepper qlPepper) {
        super(qlPepper);
        actionTypeList.add( ActionType.Hold);
    }

    @Override
    protected Future<Void> execute() {
        Future<Void> futureVoid = null;
        holder = HolderBuilder.with(qiContext).withAutonomousAbilities(
                AutonomousAbilitiesType.BASIC_AWARENESS,
                AutonomousAbilitiesType.BACKGROUND_MOVEMENT,
                AutonomousAbilitiesType.AUTONOMOUS_BLINKING).build();
        return holder.async().hold();
    }

    @Override
    protected Boolean validate() {
        return true;
    }

    @Override
    public void cancel() {
        if(holder != null) holder.async().release();
    }
}

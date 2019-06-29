package jp.pepper_atelier_akihabara.qisdk_wrapper.manager;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLEngagedHumanChangedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLHumansAroundChangedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLHuman;

public class QLHumanManager {
    private static QLHumanManager instance;

    private volatile QiContext qiContext;
    private volatile List<QLEngagedHumanChangedListener> qlEngagedHumanChangedListenerList =  new ArrayList<>();
    private volatile List<QLHumansAroundChangedListener> qlHumansAroundChangedListenerList = new ArrayList<>();

    private AtomicBoolean isReadyHumansAroundChanged = new AtomicBoolean(false);
    private AtomicBoolean isReadyEngagedHumanChanged = new AtomicBoolean(false);

    private HumanAwareness.OnHumansAroundChangedListener onHumansAroundChangedListener = new HumanAwareness.OnHumansAroundChangedListener(){
        @Override
        public void onHumansAroundChanged(List<Human> humans) {
            final List<QLHuman> qlHumanList = new ArrayList<>();
            for(Human human: humans){
                QLHuman qlHuman = new QLHuman(human);
                qlHuman.updateSync();
                qlHumanList.add(qlHuman);
            }
            QLPepper.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(QLHumansAroundChangedListener current: qlHumansAroundChangedListenerList){
                        current.onHumansAroundChanged(qlHumanList);
                    }
                }
            });
        }
    };

    private HumanAwareness.OnEngagedHumanChangedListener onEngagedHumanChangedListener = new HumanAwareness.OnEngagedHumanChangedListener() {
        @Override
        public void onEngagedHumanChanged(Human engagedHuman) {
            final QLHuman qlHuman = new QLHuman(engagedHuman);
            qlHuman.updateSync();
            QLPepper.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (QLEngagedHumanChangedListener current : qlEngagedHumanChangedListenerList) {
                        current.onEngagedHumanChanged(qlHuman);
                    }
                }
            });
        }
    };

    public static QLHumanManager getInstance(){
        if(instance == null) instance = new QLHumanManager();
        return instance;
    }

    public QLHumanManager(){
    }

    public void setQiContext(QiContext qiContext){
        if(qiContext == null) return;

        this.qiContext = qiContext;
        updateHumansAroundChangedListener();
        updateEngagedHumanChangedListener();
    }

    public synchronized void addQLHumansAroundChangedListener(QLHumansAroundChangedListener listener) {
        qlHumansAroundChangedListenerList.add(listener);
        updateHumansAroundChangedListener();
    }

    public synchronized void removeQLHumansAroundChangedListener(QLHumansAroundChangedListener listener) {
        qlHumansAroundChangedListenerList.remove(listener);
        updateHumansAroundChangedListener();
    }

    public synchronized void removeAllQLHumansAroundChangedListener() {
        qlHumansAroundChangedListenerList.clear();
        updateHumansAroundChangedListener();
    }

    public synchronized void addQLEngagedHumanChangedListener(QLEngagedHumanChangedListener listener) {
        qlEngagedHumanChangedListenerList.add(listener);
        updateEngagedHumanChangedListener();
    }

    public synchronized void removeQLEngagedHumanChangedListener(QLEngagedHumanChangedListener listener) {
        qlEngagedHumanChangedListenerList.remove(listener);
        updateEngagedHumanChangedListener();
    }

    public synchronized void removeAllQLEngagedHumanChangedListener() {
        qlEngagedHumanChangedListenerList.clear();
        updateEngagedHumanChangedListener();
    }

    private void updateHumansAroundChangedListener(){
        if(qiContext == null) return;
        if(!qlHumansAroundChangedListenerList.isEmpty() && isReadyHumansAroundChanged.compareAndSet(false, true)){
            qiContext.getHumanAwareness().async().addOnHumansAroundChangedListener(onHumansAroundChangedListener);
        }
        if(qlHumansAroundChangedListenerList.isEmpty() && isReadyHumansAroundChanged.compareAndSet(true, false)){
            qiContext.getHumanAwareness().async().removeOnHumansAroundChangedListener(onHumansAroundChangedListener);
        }
    }

    private void updateEngagedHumanChangedListener(){
        if(qiContext == null) return;
        if(!qlEngagedHumanChangedListenerList.isEmpty() && isReadyEngagedHumanChanged.compareAndSet(false, true)){
            qiContext.getHumanAwareness().async().addOnEngagedHumanChangedListener(onEngagedHumanChangedListener);
        }
        if(qlEngagedHumanChangedListenerList.isEmpty() && isReadyEngagedHumanChanged.compareAndSet(true, false)){
            qiContext.getHumanAwareness().async().removeOnEngagedHumanChangedListener(onEngagedHumanChangedListener);
        }
    }

}

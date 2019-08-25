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
            final QLHuman qlHuman;
            if(engagedHuman == null) {
                qlHuman = null;
            }else{
                qlHuman= new QLHuman(engagedHuman);
                qlHuman.updateSync();
            }

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
        if(qiContext == null && this.qiContext != null){
            if(isReadyHumansAroundChanged.compareAndSet(true, false)){
                this.qiContext.getHumanAwareness().async().removeOnHumansAroundChangedListener(onHumansAroundChangedListener);
            }
            if(isReadyEngagedHumanChanged.compareAndSet(true, false)){
                this.qiContext.getHumanAwareness().async().removeOnEngagedHumanChangedListener(onEngagedHumanChangedListener);
            }
            this.qiContext = null;
        }else{
            this.qiContext = qiContext;
            if(isReadyHumansAroundChanged.compareAndSet(false, true)){
                qiContext.getHumanAwareness().async().addOnHumansAroundChangedListener(onHumansAroundChangedListener);
            }
            if(isReadyEngagedHumanChanged.compareAndSet(false, true)){
                qiContext.getHumanAwareness().async().addOnEngagedHumanChangedListener(onEngagedHumanChangedListener);
            }
        }
    }

    public synchronized void addQLHumansAroundChangedListener(QLHumansAroundChangedListener listener) {
        qlHumansAroundChangedListenerList.add(listener);
    }

    public synchronized void removeQLHumansAroundChangedListener(QLHumansAroundChangedListener listener) {
        qlHumansAroundChangedListenerList.remove(listener);
    }

    public synchronized void removeAllQLHumansAroundChangedListener() {
        qlHumansAroundChangedListenerList.clear();
    }

    public synchronized void addQLEngagedHumanChangedListener(QLEngagedHumanChangedListener listener) {
        qlEngagedHumanChangedListenerList.add(listener);
    }

    public synchronized void removeQLEngagedHumanChangedListener(QLEngagedHumanChangedListener listener) {
        qlEngagedHumanChangedListenerList.remove(listener);
    }

    public synchronized void removeAllQLEngagedHumanChangedListener() {
        qlEngagedHumanChangedListenerList.clear();
    }
}

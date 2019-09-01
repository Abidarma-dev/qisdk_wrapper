package jp.pepper_atelier_akihabara.qisdk_wrapper.manager;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.touch.TouchSensor;
import com.aldebaran.qi.sdk.object.touch.TouchState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLTouchedListener;

public class QLTouchManager {
    private static QLTouchManager instance;

    private volatile QiContext qiContext;
    private AtomicBoolean isReady = new AtomicBoolean(false);
    private volatile List<QLTouchedListener> qlTouchedListenerList = new ArrayList<>();

    private static final Map<QLTouchedListener.QLSensor, String> sensorStringMap = new HashMap<QLTouchedListener.QLSensor, String>() {
        {
            put(QLTouchedListener.QLSensor.Head, "Head/Touch");
            put(QLTouchedListener.QLSensor.HandLeft, "LHand/Touch");
            put(QLTouchedListener.QLSensor.HandRigh, "RHand/Touch" );
            put(QLTouchedListener.QLSensor.BumperLeft, "Bumper/FrontLeft");
            put(QLTouchedListener.QLSensor.BumperRight, "Bumper/FrontRight");
            put(QLTouchedListener.QLSensor.BumperBack, "Bumper/Back");
        }
    };

    private volatile List<QLSensor> qlSensorList = new ArrayList<>();

    public static QLTouchManager getInstance(){
        if(instance == null) instance = new QLTouchManager();
        return instance;
    }

    public static void release(){
        if(instance == null) return;
        instance.removeAllOnTouchedListener();
        instance.setQiContext(null);
        instance = null;
    }

    public QLTouchManager(){
        for(QLTouchedListener.QLSensor current: sensorStringMap.keySet()){
            qlSensorList.add(new QLSensor(current));
        }
    }

    public void setQiContext(QiContext qiContext){
        if(qiContext == null && this.qiContext != null){
            if(isReady.compareAndSet(true, false)){
                for(QLSensor current: qlSensorList){
                    current.removeOnStateChangedListener();
                }
            }
            this.qiContext = null;
        }else{
            this.qiContext = qiContext;
            if(isReady.compareAndSet(false, true)){
                for(QLSensor current: qlSensorList){
                    current.setOnStateChangedListener();
                }
            }
        }
    }

    public synchronized void addOnTouchedListener(QLTouchedListener listener){
        qlTouchedListenerList.add(listener);
    }

    public synchronized void removeOnTouchedListener(QLTouchedListener listener){
        qlTouchedListenerList.remove(listener);
    }

    public synchronized void removeAllOnTouchedListener(){
        qlTouchedListenerList.clear();
    }

    private class QLSensor{
        private String name;
        private QLTouchedListener.QLSensor sensor;
        private TouchSensor touchSensor;
        private TouchSensor.OnStateChangedListener onStateChangedListener  = new TouchSensor.OnStateChangedListener(){
            @Override
            public void onStateChanged(TouchState state) {
                if(!state.getTouched()) return;
                for(final QLTouchedListener current: qlTouchedListenerList){
                    QLPepper.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            current.onTouched(sensor);
                        }
                    });
                }
            }
        };
        private AtomicBoolean isPreparedSensor = new AtomicBoolean(false);

        public QLSensor(QLTouchedListener.QLSensor sensor){
            this.sensor = sensor;
            this.name = sensorStringMap.get(sensor);
        }

        public void setOnStateChangedListener(){
            if(qiContext == null || !isPreparedSensor.compareAndSet(false, true)) return;

            qiContext.getTouch().async().getSensor(name)
                    .andThenConsume(new Consumer<TouchSensor>() {
                        @Override
                        public void consume(TouchSensor touchSensor) throws Throwable {
                            QLSensor.this.touchSensor =touchSensor;
                            if(isPreparedSensor.compareAndSet(true, false)){
                                touchSensor.addOnStateChangedListener(onStateChangedListener);
                            }
                        }
                    });
        }

        public void removeOnStateChangedListener(){
            isPreparedSensor.set(false);
            if(touchSensor != null) {
                touchSensor.async().removeOnStateChangedListener(onStateChangedListener);
                touchSensor = null;
            }
        }
    }
}

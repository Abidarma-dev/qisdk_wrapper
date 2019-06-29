package jp.pepper_atelier_akihabara.qisdk_wrapper.listener;

public interface QLTouchedListener {
    public enum QLSensor {
        Head,
        HandRigh,
        HandLeft,
        BumperRight,
        BumperLeft,
        BumperBack,
    }

    void onTouched(QLSensor sensor);
}

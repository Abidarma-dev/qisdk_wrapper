package jp.pepper_atelier_akihabara.qisdk_wrapper.listener;

import java.util.List;

import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLHuman;

public interface QLHumansAroundCallback {
    void onHumansAround(List<QLHuman> humanList);
}

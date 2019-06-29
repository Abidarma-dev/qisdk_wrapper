package jp.pepper_atelier_akihabara.qisdk_wrapper.listener;

import java.util.List;

import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLHuman;

public interface QLHumansAroundChangedListener {
    void onHumansAroundChanged(List<QLHuman> humanList);
}

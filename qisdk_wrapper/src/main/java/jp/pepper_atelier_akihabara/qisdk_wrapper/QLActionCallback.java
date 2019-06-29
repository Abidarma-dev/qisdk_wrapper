package jp.pepper_atelier_akihabara.qisdk_wrapper;

public interface QLActionCallback<T> {
    void onSuccess(T value);
    void onCancel();
    void onError(String message);
}

package jp.pepper_atelier_akihabara.qisdk_wrapper;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import jp.pepper_atelier_akihabara.qisdk_wrapper.manager.QLActionManager;

public abstract class QLAction <T> {

    private static final int ACTION_STATE_IDLE = 0;
    private static final int ACTION_STATE_RUNNING = 1;
    private static final int ACTION_STATE_PENDING = 2;
    private static final int ACTION_STATE_CANCELED = 3;
    private static final int ACTION_STATE_DONE = 4;
    private static final String ERROR_MESSAGE_ALREADY_RUNNING = "This action is already running";
    private static final String ERROR_MESSAGE_INVALID_PARAMETER = "invalid parameter";

    public enum ActionType {
        None,
        Conversation,
        Animate,
        Hold,
        Move,
        Localize,
        TakePicture,
    }

    protected QLPepper qlPepper;

    protected volatile Future<Void> future = null;
    private QLActionCallback<T> callback;
    private Boolean interrupt = true;

    protected volatile T actionResult = null;
    protected Boolean isAlwaysCanceled  = false;
    protected Boolean isSuccess = false;
    protected List<ActionType> actionTypeList = new ArrayList<>();
    protected volatile QiContext qiContext = null;
    protected AtomicInteger actionState = new AtomicInteger(ACTION_STATE_IDLE);

    public QLAction(QLPepper qlPepper){
        this.qlPepper = qlPepper;
    }

    /**
     * アクションの種別を取得
     * 同じ種別のアクションを同時に実行することは出来ない
     * 発話と聞き取り、GotoとTrajectoryなど
     * @return
     */
    public List<ActionType> getActionTypeList(){
        return actionTypeList;
    }

    /**
     * アクションのキャンセル
     */
    public synchronized void cancel(){
        if(actionState.compareAndSet(ACTION_STATE_IDLE, ACTION_STATE_CANCELED) ||
                actionState.compareAndSet(ACTION_STATE_RUNNING, ACTION_STATE_CANCELED) ||
                actionState.compareAndSet(ACTION_STATE_PENDING, ACTION_STATE_CANCELED)){
            if (future != null && !future.isDone()){
                future.requestCancellation();
            }else{
                qlPepper.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(callback != null) callback.onCancel();
                    }
                });
            }
        }
    }

    /**
     * アクションの実行が完了しているかどうか
     * @return
     */
    public Boolean isDone(){
        int state = actionState.get();
        return (state == ACTION_STATE_DONE || state == ACTION_STATE_CANCELED);
    }

    /**
     * アクションが実行中、もしくは実行済みの場合はfutureを返す
     * @return
     */
    @Nullable
    public Future<Void> getFuture(){
        return future;
    }

    /**
     * 設定されているコールバックオブジェクトを取得
     * @return
     */
    public QLActionCallback getCallback(){
        return callback;
    }

    /**
     * 設定されているアクション実行時の中断の要否を取得
     * @return
     */
    public Boolean getInterrupt(){
        return interrupt;
    }

    /**
     * アクションの同期実行
     * UIThreadで呼ばないこと
     * @return
     */
    @WorkerThread
    public synchronized T runSync(){
        if(!actionState.compareAndSet(ACTION_STATE_IDLE, ACTION_STATE_RUNNING) || !validate()) return null;

        qiContext = qlPepper.getQiContext();
        if(qiContext == null) return null;

        future = QLActionManager.getInstance().getRelationalFuture(this, interrupt);
        if(future == null){
            future = execute();
        }else{
            future = future.thenCompose(new Function<Future<Void>, Future<Void>>() {
                @Override
                public Future<Void> execute(Future<Void> voidFuture) throws Throwable {
                    return QLAction.this.execute();
                }
            });
        }

        if(future != null) {
            try {
                future.get();
            } catch (ExecutionException e) {
                future.requestCancellation();
                e.printStackTrace();
            }
        }

        actionState.compareAndSet(ACTION_STATE_RUNNING, ACTION_STATE_DONE);

        return actionResult;
    }

    /**
     * アクションの非同期実行
     * アクションの種別が重複するアクションが既に実行中の場合は、実行中のアクションを中断する
     */
    public void run(){
        run(null, true);
    }

    /**
     * アクションの非同期実行
     * @param interrupt trueの場合は、アクションの種別が重複するアクションが既に実行中の場合に、実行中のアクションを中断する
     */
    public void run(Boolean interrupt){
        run(null, interrupt);
    }

    /**
     * アクションの非同期実行
     * アクションの種別が重複するアクションが既に実行中の場合は、実行中のアクションを中断する
     * @param callback アクション実行完了時のコールバック
     */
    public void run(QLActionCallback<T> callback){
        run(callback, true);
    }

    /**
     * アクションの非同期実行
     * @param callback アクション実行完了時のコールバック
     * @param interrupt trueの場合は、アクションの種別が重複するアクションが既に実行中の場合に、実行中のアクションを中断する
     */
    public synchronized void run(final QLActionCallback<T> callback, Boolean interrupt){
        qiContext = qlPepper.getQiContext();
        this.callback = callback;
        this.interrupt = interrupt;

        if((actionState.get() == ACTION_STATE_RUNNING || actionState.get() == ACTION_STATE_CANCELED || actionState.get() == ACTION_STATE_DONE) ||
                (qiContext == null && actionState.get() == ACTION_STATE_PENDING)
        ){
            error(ERROR_MESSAGE_ALREADY_RUNNING);
            return;
        }

        if(!validate()){
            error(ERROR_MESSAGE_INVALID_PARAMETER);
            return;
        }

        if(qiContext == null && actionState.compareAndSet(ACTION_STATE_IDLE, ACTION_STATE_PENDING)){
            QLActionManager.getInstance().addPendingAction(this);
            return;
        }

        actionState.set(ACTION_STATE_RUNNING);

        future = QLActionManager.getInstance().getRelationalFuture(this, interrupt);
        if(future == null){
            future = execute();
        }else{
            future = future.thenCompose(new Function<Future<Void>, Future<Void>>() {
                @Override
                public Future<Void> execute(Future<Void> voidFuture) throws Throwable {
                    return QLAction.this.execute();
                }
            });
        }

        future.thenConsume(Qi.onUiThread(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> voidFuture) throws Throwable {
                if(voidFuture.isSuccess()){
                    actionState.set(ACTION_STATE_DONE);
                    if(callback != null) callback.onSuccess(actionResult);
                }else if(voidFuture.isCancelled()){
                    if(callback != null){
                        if(isAlwaysCanceled && isSuccess){
                            callback.onSuccess(actionResult);
                        }else{
                            callback.onCancel();
                        }
                    }
                }else if(voidFuture.hasError()){
                    future.requestCancellation();
                    actionState.set(ACTION_STATE_DONE);
                    if(callback != null) callback.onError(voidFuture.getErrorMessage());
                }
            }
        }));
    }

    protected void error(final String message){
        if(callback == null) return;
        qlPepper.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.onError(message);
            }
        });
    }

    protected abstract Future<Void> execute();
    protected abstract Boolean validate();
}

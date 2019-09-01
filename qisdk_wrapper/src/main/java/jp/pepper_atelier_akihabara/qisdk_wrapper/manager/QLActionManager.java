package jp.pepper_atelier_akihabara.qisdk_wrapper.manager;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLAction;

public class QLActionManager {

    private volatile static QLActionManager instance;

    public static QLActionManager getInstance() {
        if(instance == null) instance = new QLActionManager();
        return instance;
    }

    public static void release(){
        if(instance == null) return;
        instance.cancelAction();
        instance = null;
    }

    private volatile Map<QLAction.ActionType, QLAction> actionMap = new HashMap<>();
    private volatile Queue<QLAction> actionSetQueue = new ArrayDeque<>();

    public QLActionManager(){

    }

    public synchronized Future<Void> getRelationalFuture(QLAction action, Boolean interrupt) {
        if(action ==null) return null;

        ArrayList<Future<Void>> futureList = new ArrayList<>();
        List<QLAction.ActionType> list = action.getActionTypeList();
        for(QLAction.ActionType actionType: list) {
            QLAction currentAction = actionMap.get(actionType);
            if(currentAction != null){
                Future<Void> future = currentAction.getFuture();
                if(future != null && !future.isDone()){
                    futureList.add(currentAction.getFuture());
                }
                if(interrupt) currentAction.cancel();
            }

            actionMap.put(actionType, action);
        }

        if (futureList.isEmpty()) return null;
        return Future.waitAll(futureList.toArray(new Future[futureList.size()])).thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> voidFuture) throws Throwable {

            }
        });
    }

    public synchronized void runPendingAction(){
        QLAction current;
        while((current = actionSetQueue.poll()) != null){
            current.run(current.getCallback(), current.getInterrupt());
        }
    }

    public synchronized void addPendingAction(QLAction action){
        if(action ==null) return;
        actionSetQueue.add(action);
    }

    public synchronized void cancelAction(){
        for (Map.Entry<QLAction.ActionType, QLAction> entry : actionMap.entrySet()) {
            entry.getValue().cancel();
        }

        QLAction current;
        while((current = actionSetQueue.poll()) != null){
            current.cancel();
        }
    }
}

package jp.pepper_atelier_akihabara.qisdk_wrapper;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;

import java.util.ArrayList;
import java.util.List;

import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLAnimate;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLChat;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLGoTo;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLHold;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLListen;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLLocalize;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLLookAt;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLSay;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLTakePicture;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLTrajectory;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLEngagedHumanChangedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLHumansAroundChangedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLTouchedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.manager.QLActionManager;
import jp.pepper_atelier_akihabara.qisdk_wrapper.manager.QLHumanManager;
import jp.pepper_atelier_akihabara.qisdk_wrapper.manager.QLTouchManager;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLFrame;

public class QLPepper {
    public static final String TAG = "QiSDK_Wrapper";
    private static volatile QLPepper instance;
    private Context context;

    public static QLPepper getInstance() {
        if(instance == null) instance = new QLPepper();
        return instance;
    }

    private volatile QiContext qiContext = null;
    private volatile List<QLRobotLifecycleCallbacks> qlRobotLifecycleCallbacksList= new ArrayList<>();
    private Handler handler;


    private QLPepper(){
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Activityの登録
     * ActivityのonCreateで呼ぶこと
     * @param activity
     */
    public void register(Activity activity) {
        register(activity, null);
    }

    /**
     * Activityの登録
     * ActivityのonCreateで呼ぶこと
     * @param activity
     * @param callbacks
     */
    public synchronized void register(Activity activity, final RobotLifecycleCallbacks callbacks){
        QLRobotLifecycleCallbacks qlRobotLifecycleCallbacks = new QLRobotLifecycleCallbacks(activity, callbacks);
        QiSDK.register(activity, qlRobotLifecycleCallbacks);
        qlRobotLifecycleCallbacksList.add(qlRobotLifecycleCallbacks);
        context = activity.getApplicationContext();
    }

    /**
     * Activityの登録解除
     * registerしたActivityのonDestroyで必ず呼ぶこと
     * @param activity
     */
    public synchronized void unregister(Activity activity){
        for(int index = qlRobotLifecycleCallbacksList.size(); 0 < index; index--){
            QLRobotLifecycleCallbacks current = qlRobotLifecycleCallbacksList.get(index-1);
            if(current.activity == activity){
                QiSDK.unregister(current.activity, current);
                qlRobotLifecycleCallbacksList.remove(current);
            }
        }
        if(qlRobotLifecycleCallbacksList.size() == 0) qiContext = null;
    }

    /**
     * QiContextの取得
     * @return
     */
    public QiContext getQiContext() {return qiContext; }

    /**
     * UIThreadでRunnableオブジェクトを実行
     * @param runnable
     */
    public void runOnUiThread(Runnable runnable){
        if(handler != null) handler.post(runnable);
    }

    /**
     * 指定された内容で発話を行います
     * @return
     */
    public QLSay buildSay(){
        return new QLSay(this);
    }

    /**
     * 指定された内容で発話を行います
     * @return
     */
    public QLSay buildSay(String phrase){
        QLSay qlSay = new QLSay(this);
        qlSay.addPhrase(phrase);
        return qlSay;
    }

    /**
     * 指定された内容で発話とモーションを行います
     * @return
     */
    public QLSay buildSay(String phrase, Integer animationId){
        QLSay qlSay = new QLSay(this);
        qlSay.addPhrase(phrase, animationId);
        return qlSay;
    }

    /**
     * 聞き取りを開始し、実行時(run)の戻り値で聞き取った内容を返します
     * @return
     */
    public QLListen buildListen(){
        return  new QLListen(this);
    }

    /**
     * 聞き取りを開始し、実行時(run)の戻り値で聞き取った内容を返します
     * @param listenPhraseList 聞き取りするフレーズの一覧
     * @return
     */
    public QLListen buildListen(List<String> listenPhraseList){
        QLListen qlListen = new QLListen(this);
        qlListen.addPhrase(listenPhraseList);
        return qlListen;
    }

    /**
     * 指定されたトピックファイルの内容でチャットを開始します
     * 実行時(run）の戻り値でチャット終了の理由を返します
     * チャット終了理由はトピックファイル内の ^endDiscussの引数です
     * @return
     */
    public QLChat buildChat(){
        return new QLChat(this);
    }

    /**
     * 指定されたトピックファイルの内容でチャットを開始します
     * 実行時(run）の戻り値でチャット終了の理由を返します
     * チャット終了理由はトピックファイル内の ^endDiscussの引数です
     * @param topicId
     * @return
     */
    public QLChat buildChat(Integer topicId){
        QLChat qlChat = new QLChat(this);
        qlChat.addResourceId(topicId);
        return qlChat;
    }

    /**
     * 指定されたモーションファイル(qianim)を実行します。
     * @return
     */
    public QLAnimate buildAnimate(){
        return new QLAnimate(this);
    }

    /**
     * 指定されたモーションファイル(qianim)を実行します。
     * @param animationId
     * @return
     */
    public QLAnimate buildAnimate(Integer animationId){
        QLAnimate qlAnimate = new QLAnimate(this);
        qlAnimate.addResourceId(animationId);
        return qlAnimate;
    }

    /**
     * 指定された位置に移動します
     * 障害物等があれば迂回しようとします
     * @return
     */
    public QLGoTo buildGoTo(){
        return new QLGoTo(this);
    }

    /**
     * 指定された位置に移動します
     * 障害物等があれば迂回しようとします
     * @param locationX 移動先の座標。現在位置を中心に指定メートル先に移動。マイナスは後ろ。
     * @param locationY 移動先の座標。現在位置を中心に指定メートル右に移動。マイナスは左。
     * @return
     */
    public QLGoTo buildGoTo(double locationX, double locationY){
        QLGoTo qlGoTo = new QLGoTo(this);
        qlGoTo.setDestination(new QLFrame(QLFrame.FRAME_TYPE_ROBOT), locationX, locationY);
        return qlGoTo;
    }

    /**
     * 指定されたTrajectoryファイルの内容で移動を行います
     * @return
     */
    public QLTrajectory buildTrajectory(){
        return new QLTrajectory(this);
    }

    /**
     * 指定されたTrajectoryファイルの内容で移動を行います
     * @param trajectoryId
     * @return
     */
    public QLTrajectory buildTrajectory(Integer trajectoryId){
        QLTrajectory qlTrajectory = new QLTrajectory(this);
        qlTrajectory.addResourceId(trajectoryId);
        return qlTrajectory;
    }

    /**
     * 指定された位置を見続けます
     * アクションをキャンセルするまで指定位置を見続けます
     * @return
     */
    public QLLookAt buildLookAt(){
        QLLookAt qlLookAt = new QLLookAt(this);
        return qlLookAt;
    }

    /**
     * 指定された位置を見続けます
     * アクションをキャンセルするまで指定位置を見続けます
     * @param locationX 向き先の座標。現在位置を中心に指定メートル先に移動。マイナスは後ろ。
     * @param locationY 向き先の座標。現在位置を中心に指定メートル右に移動。マイナスは左。
     * @return
     */
    public QLLookAt buildLookAt(double locationX, double locationY){
        QLLookAt qlLookAt = new QLLookAt(this);
        qlLookAt.setDestination(new QLFrame(QLFrame.FRAME_TYPE_ROBOT), locationX, locationY, 1.2);
        return qlLookAt;
    }

    /**
     * 周辺の地図情報を生成します
     * 生成した地図情報は戻り値で返されるQLLocalizeオブジェクトのsaveMapメソッドでファイル保存できます
     * 次回実行時にQLLocalizeオブジェクトのsetMapFileNameメソッドでファイルを指定してから実行(run)することで生成済みの地図情報を使用します
     * @return
     */
    public QLLocalize buildLocalize(){
        QLLocalize qlLocalize = new QLLocalize(this);
        return qlLocalize;
    }

    /**
     * おでこのカメラで写真を撮影します
     * 実行時(run)時の戻り値で撮影した写真をBitmapオブジェクトで返します
     * @return
     */
    public QLTakePicture buildTakePicture(){
        return new QLTakePicture(this);
    }

    /**
     * Autonomousの機能を一時的に停止します
     * @return
     */
    public QLHold buildHold(){
        QLHold qlHold = new QLHold(this);
        return qlHold;
    }

    /**
     * 全てのアクションのキャンセルします
     */
    public void cancelAll(){
        QLActionManager.getInstance().cancelAction();
    }

    /**
     * 周辺の人の情報が更新されたとき用のリスナーを登録
     * @param listener
     */
    public void addQLHumansAroundChangedListener(QLHumansAroundChangedListener listener){
        QLHumanManager.getInstance().addQLHumansAroundChangedListener(listener);
    }

    /**
     * 周辺の人の情報が更新されたとき用のリスナーを削除
     * @param listener
     */
    public void removeHumansAroundChangedListener(QLHumansAroundChangedListener listener){
        QLHumanManager.getInstance().removeQLHumansAroundChangedListener(listener);
    }

    /**
     * 周辺の人の情報が更新されたとき用のリスナーをすべて削除
     */
    public void removeAllHumansAroundChangedListener(){
        QLHumanManager.getInstance().removeAllQLHumansAroundChangedListener();
    }

    /**
     * エンゲージしている人の情報が更新されたとき用のリスナーを登録
     */
    public void addQLEngagedHumanChangedListener(final QLEngagedHumanChangedListener listener){
        QLHumanManager.getInstance().addQLEngagedHumanChangedListener(listener);
    }

    /**
     * エンゲージしている人の情報が更新されたとき用のリスナーを削除
     */
    public void removeEngagedHumanChangedListener(QLEngagedHumanChangedListener listener){
        QLHumanManager.getInstance().removeQLEngagedHumanChangedListener(listener);
    }

    /**
     * エンゲージしている人の情報が更新されたとき用のリスナーをすべて削除
     */
    public void removeAllEngagedHumanChangedListener(){
        QLHumanManager.getInstance().removeAllQLEngagedHumanChangedListener();
    }

    /**
     * タッチセンサー、バンパーセンサーが押された時用のリスナーを登録
     * @param listener
     */
    public void addOnTouchedListener(QLTouchedListener listener){
        QLTouchManager.getInstance().addOnTouchedListener(listener);
    }

    /**
     * タッチセンサー、バンパーセンサーが押された時用のリスナーを削除
     * @param listener
     */
    public void removeOnTouchedListener(QLTouchedListener listener){
        QLTouchManager.getInstance().removeOnTouchedListener(listener);
    }

    /**
     * タッチセンサー、バンパーセンサーが押された時用のリスナーをすべて削除
     */
    public void removeAllOnTouchedListener(){
        QLTouchManager.getInstance().removeAllOnTouchedListener();
    }

    public String getStringResource(int resId){
        return context.getResources().getString(resId);
    }

    private class QLRobotLifecycleCallbacks implements RobotLifecycleCallbacks{
        private final Activity activity;
        private final RobotLifecycleCallbacks callbacks;

        public QLRobotLifecycleCallbacks(Activity activity,RobotLifecycleCallbacks callbacks){
            this.activity = activity;
            this.callbacks = callbacks;
        }

        @Override
        public void onRobotFocusGained(QiContext qiContext_) {
            Log.d(TAG, "onRobotFocusGained");
            qiContext = qiContext_;
            QLTouchManager.getInstance().setQiContext(qiContext_);
            QLHumanManager.getInstance().setQiContext(qiContext_);
            QLActionManager.getInstance().runPendingAction();
            if(callbacks != null) callbacks.onRobotFocusGained(qiContext_);
        }

        @Override
        public void onRobotFocusLost() {
            Log.d(TAG, "onRobotFocusLost");
            qiContext = null;
            QLTouchManager.getInstance().setQiContext(null);
            QLHumanManager.getInstance().setQiContext(null);
            if(callbacks != null) callbacks.onRobotFocusLost();
        }

        @Override
        public void onRobotFocusRefused(String reason) {
            Log.d(TAG, "onRobotFocusRefused " + reason);
            if(callbacks!= null) callbacks.onRobotFocusRefused(reason);
        }
    }
}

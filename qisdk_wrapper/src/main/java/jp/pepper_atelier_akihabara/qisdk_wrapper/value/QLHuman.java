package jp.pepper_atelier_akihabara.qisdk_wrapper.value;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.object.human.Age;
import com.aldebaran.qi.sdk.object.human.AttentionState;
import com.aldebaran.qi.sdk.object.human.Emotion;
import com.aldebaran.qi.sdk.object.human.EngagementIntentionState;
import com.aldebaran.qi.sdk.object.human.ExcitementState;
import com.aldebaran.qi.sdk.object.human.FacialExpressions;
import com.aldebaran.qi.sdk.object.human.Gender;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.human.PleasureState;
import com.aldebaran.qi.sdk.object.human.SmileState;
import com.aldebaran.qi.sdk.object.image.TimestampedImage;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLActionCallback;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;

public class QLHuman {
    private AttentionState attentionState = AttentionState.UNKNOWN;
    private ExcitementState excitementState = ExcitementState.UNKNOWN;
    private PleasureState pleasureState = PleasureState.UNKNOWN;
    private int age = 0;
    private Gender gender = Gender.UNKNOWN;
    private EngagementIntentionState engagementIntention = EngagementIntentionState.UNKNOWN;
    private SmileState smileState = SmileState.UNKNOWN;

    private volatile Bitmap facePictureBitmap = null;

    private Human human = null;

    public QLHuman(Human human){
        this.human = human;
    }

    public void updateSync(){
        if(human == null) return;
        attentionState = human.getAttention();
        excitementState = human.getEmotion().getExcitement();
        pleasureState = human.getEmotion().getPleasure();
        age = human.getEstimatedAge().getYears();
        gender = human.getEstimatedGender();
        engagementIntention = human.getEngagementIntention();
        smileState = human.getFacialExpressions().getSmile();
    }

    public void update(){
        update(null);
    }

    /**
     * 認識している人の情報を更新する
     * @param callback
     */
    public void update(final QLActionCallback<QLHuman> callback){
        if(human == null){
            return;
        }
        Future.waitAll(
            human.async().getAttention().thenConsume(new Consumer<Future<AttentionState>>() {
                @Override
                public void consume(Future<AttentionState> attentionStateFuture) throws Throwable {
                    attentionState = attentionStateFuture.getValue();
                }
            }),
            human.async().getEmotion().thenConsume(new Consumer<Future<Emotion>>() {
                @Override
                public void consume(Future<Emotion> emotionFuture) throws Throwable {
                    excitementState = emotionFuture.getValue().getExcitement();
                    pleasureState = emotionFuture.getValue().getPleasure();
                }
            }),
            human.async().getEstimatedAge().thenConsume(new Consumer<Future<Age>>() {
                @Override
                public void consume(Future<Age> ageFuture) throws Throwable {
                    age = ageFuture.getValue().getYears();
                }
            }),
            human.async().getEstimatedGender().thenConsume(new Consumer<Future<Gender>>() {
                @Override
                public void consume(Future<Gender> genderFuture) throws Throwable {
                    gender = genderFuture.getValue();
                }
            }),
            human.async().getEngagementIntention().thenConsume(new Consumer<Future<EngagementIntentionState>>() {
                @Override
                public void consume(Future<EngagementIntentionState> engagementIntentionStateFuture) throws Throwable {
                    engagementIntention = engagementIntentionStateFuture.getValue();
                }
            }),
            human.async().getFacialExpressions().thenConsume(new Consumer<Future<FacialExpressions>>() {
                @Override
                public void consume(Future<FacialExpressions> facialExpressionsFuture) throws Throwable {
                    smileState = facialExpressionsFuture.getValue().getSmile();
                }
            })
        ).thenConsume(Qi.onUiThread(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> future) throws Throwable {
                if(callback == null) return;
                if (future.hasError()) {
                    callback.onError(future.getErrorMessage());
                } else if (future.isCancelled()) {
                    callback.onCancel();
                } else {
                    callback.onSuccess(QLHuman.this);
                }
            }
        }));
    }

    /**
     * Pepperに注目しているかどうか
     * @return
     */
    public AttentionState getAttentionState(){
        return attentionState;
    }

    /**
     * 興奮しているか
     * @return
     */
    public ExcitementState getExcitementState(){
        return excitementState;
    }

    /**
     * 喜んでいるか
     * @return
     */
    public PleasureState getPleasureState() {
        return pleasureState;
    }

    /**
     * 推定年齢
     * @return
     */
    public int getAge() {
        return age;
    }

    /**
     * 推定性別
     * @return
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * エンゲージしているかどうか
     * @return
     */
    public EngagementIntentionState getEngagementIntention() {
        return engagementIntention;
    }

    /**
     * 笑顔かどうか
     * @return
     */
    public SmileState getSmileState() {
        return smileState;
    }

    /**
     * モノクロの顔写真を取得
     * @return
     */
    @WorkerThread
    public Bitmap getFacePictureSync(){
        Future<Void> future = runGetFacePicture();
        try {
            future.get();
        } catch (ExecutionException e) {
            Log.e(QLPepper.TAG, e.getMessage(), e);
        }
        return facePictureBitmap;
    }

    /**
     * モノクロの顔写真を取得
     * @param callback
     */
    public void getFacePicture(final QLActionCallback<Bitmap> callback){
        Future<Void> future = runGetFacePicture();
        future.thenConsume(Qi.onUiThread(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> voidFuture) throws Throwable {
                if (voidFuture.hasError()) {
                    callback.onError(voidFuture.getErrorMessage());
                } else if (voidFuture.isCancelled()) {
                    callback.onCancel();
                } else {
                    callback.onSuccess(facePictureBitmap);
                }
            }
        }));
    }

    private Future<Void> runGetFacePicture(){
        return human.async().getFacePicture()
                .andThenConsume(new Consumer<TimestampedImage>() {
                    @Override
                    public void consume(TimestampedImage timestampedImage) throws Throwable {
                        ByteBuffer buffer = timestampedImage.getImage().getData();
                        buffer.rewind();
                        int size = buffer.remaining();
                        byte[] byteArray = new byte[size];
                        buffer.get(byteArray);
                        facePictureBitmap = BitmapFactory.decodeByteArray(byteArray, 0 ,size);
                    }
                });
    }

    public Human getHuman(){
        return human;
    }
}

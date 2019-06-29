package jp.pepper_atelier_akihabara.qisdk_wrapper.action;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.object.image.TimestampedImageHandle;

import java.nio.ByteBuffer;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLAction;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;

public class QLTakePicture extends QLAction<Bitmap> {

    public QLTakePicture(QLPepper qlPepper) {
        super(qlPepper);
        actionTypeList.add(ActionType.TakePicture);
    }

    @Override
    protected Future<Void> execute(){
        return runTakePicture();
    }

    @Override
    protected Boolean validate() {
        return true;
    }
    private Future<Void> runTakePicture(){
        return TakePictureBuilder.with(qiContext).buildAsync()
                .andThenCompose(new Function<TakePicture, Future<TimestampedImageHandle>>() {
                    @Override
                    public Future<TimestampedImageHandle> execute(TakePicture takePicture) throws Throwable {
                        return takePicture.async().run();
                    }
                }).andThenConsume(new Consumer<TimestampedImageHandle>() {
                    @Override
                    public void consume(TimestampedImageHandle timestampedImageHandle) throws Throwable {
                        ByteBuffer buffer = timestampedImageHandle.getImage().getValue().getData();
                        buffer.rewind();
                        int size = buffer.remaining();
                        byte[] byteArray = new byte[size];
                        buffer.get(byteArray);
                        actionResult = BitmapFactory.decodeByteArray(byteArray, 0 ,size);
                    }
                });
    }
}

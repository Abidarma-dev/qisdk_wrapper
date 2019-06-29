package jp.pepper_atelier_akihabara.qisdk_wrapper.action;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.builder.ExplorationMapBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeBuilder;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.Localize;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLAction;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLActionCallback;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;

public class QLLocalize extends QLAction<Void> {
    private static final String ERROR_MESSAGE_NOT_FOUND_MAP = "not found map data";

    private LocalizeAndMap localizeAndMap;
    private String mapFileName = null;

    private Boolean saveFileResult;

    public QLLocalize(QLPepper qlPepper) {
        super(qlPepper);
        this.isAlwaysCanceled = true;
        actionTypeList.add(ActionType.Animate);
        actionTypeList.add(ActionType.Localize);
    }

    /**
     * 過去に作成した地図データがあればそのファイル名を指定
     * 設定されていない場合は実行時に周辺の地図生成を行う
     * @param mapFileName
     * @return
     */
    public QLLocalize setMapFileName(String mapFileName){
        if(mapFileName.isEmpty()) return this;
        this.mapFileName = mapFileName;
        return this;
    }

    @Override
    protected Future<Void> execute() {
        Future<Void> futureVoid = null;
        if(mapFileName == null) {
            futureVoid = runLocalizeAndMap();
        } else {
            futureVoid = runLocalize();
        }
        return futureVoid;
    }

    @Override
    protected Boolean validate() {
        if(mapFileName != null && !mapFileName.isEmpty()){
            String filepath = String. format("%s/%s" , qiContext.getFilesDir().getAbsolutePath() , mapFileName);
            File file = new File(filepath);
            return file.exists();
        }
        return true;
    }

    private Boolean saveFile(String fileName, String str) {
        Boolean result = false;
        try (FileOutputStream fileOutputstream = qiContext.openFileOutput(fileName, qiContext.MODE_PRIVATE)){
            fileOutputstream.write(str.getBytes());
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String loadFile(String fileName) {

        StringBuilder builder = new StringBuilder();
        try (FileInputStream fileInputStream = qiContext.openFileInput(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"))) {
            String lineBuffer;
            while( (lineBuffer = reader.readLine()) != null ) {
                builder.append(lineBuffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    private Future<Void> runLocalizeAndMap(){
        Future<Void> futureLocalizingAndMap = LocalizeAndMapBuilder.with(qiContext).buildAsync()
                .andThenCompose(new Function<LocalizeAndMap, Future<Void>>() {
                    @Override
                    public Future<Void> execute(final LocalizeAndMap localizeAndMap) throws Throwable {
                        QLLocalize.this.localizeAndMap = localizeAndMap;
                        localizeAndMap.addOnStatusChangedListener(new LocalizeAndMap.OnStatusChangedListener() {
                            @Override
                            public void onStatusChanged(LocalizationStatus status) {
                                if (status == LocalizationStatus.LOCALIZED) {
                                    isSuccess = true;
                                }
                            }
                        });
                        return QLLocalize.this.localizeAndMap.async().run();
                    }
                }).thenConsume(new Consumer<Future<Void>>() {
                    @Override
                    public void consume(Future<Void> voidFuture) throws Throwable {
                        // nop
                    }
                });
        return futureLocalizingAndMap;
    }

    private Future<Void> runLocalize(){
        String mapString = loadFile(mapFileName);
        return ExplorationMapBuilder.with(qiContext).withMapString(mapString).buildAsync()
                .andThenCompose(new Function<ExplorationMap, Future<Localize>>() {
                    @Override
                    public Future<Localize> execute(ExplorationMap explorationMap) throws Throwable {
                        return LocalizeBuilder.with(qiContext).withMap(explorationMap).buildAsync();
                    }
                }).andThenCompose(new Function<Localize, Future<Void>>() {
                    @Override
                    public Future<Void> execute(Localize localize) throws Throwable {
                        isSuccess = true;
                        return localize.async().run();
                    }
                }).thenConsume(new Consumer<Future<Void>>() {
                    @Override
                    public void consume(Future<Void> voidFuture) throws Throwable {
                        // nop
                    }
                });
    }

    /**
     * 生成された地図データを保存
     * @param fileName
     */
    public void saveMap(String fileName){
        saveMap(fileName, null);
    }

    /**
     * 生成された地図データを保存
     * @param fileName
     * @param callback
     */
    public void saveMap(final String fileName, final QLActionCallback<Boolean> callback){
        saveFileResult = false;
        if(localizeAndMap == null){
            error(ERROR_MESSAGE_NOT_FOUND_MAP);
            return;
        }
        localizeAndMap.async().dumpMap()
                .andThenCompose(new Function<ExplorationMap, Future<String>>() {
                    @Override
                    public Future<String> execute(ExplorationMap explorationMap) throws Throwable {
                        return explorationMap.async().serialize();
                    }
                }).andThenConsume(new Consumer<String>() {
                    @Override
                    public void consume(String s) throws Throwable {
                        saveFileResult = saveFile(fileName, s);
                    }
                }).thenConsume(Qi.onUiThread(new Consumer<Future<Void>>() {
                    @Override
                    public void consume(Future<Void> voidFuture) throws Throwable {
                        if(callback == null) return;
                        if (voidFuture.hasError()) {
                            callback.onError(voidFuture.getErrorMessage());
                        } else if (voidFuture.isCancelled()) {
                            callback.onCancel();
                        } else {
                            callback.onSuccess(saveFileResult);
                        }
                    }
                }));
    }
}

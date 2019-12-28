package com.softbankrobotics.qitest;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jp.pepper_atelier_akihabara.qisdk_wrapper.QLActionCallback;
import jp.pepper_atelier_akihabara.qisdk_wrapper.QLPepper;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLAnimate;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLChat;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLGoTo;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLListen;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLLocalize;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLLookAt;
import jp.pepper_atelier_akihabara.qisdk_wrapper.action.QLSay;
import jp.pepper_atelier_akihabara.qisdk_wrapper.chatbot.dialogflow.OnDetectedIntentListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.chatbot.dialogflow.QLDialogflowChatbot;
import jp.pepper_atelier_akihabara.qisdk_wrapper.chatbot.dialogflow.QLDialogflowChatbotBuilder;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLBookmarkReachedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLEngagedHumanChangedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLHumansAroundChangedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.listener.QLTouchedListener;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLFrame;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLHuman;
import jp.pepper_atelier_akihabara.qisdk_wrapper.value.QLLanguage;


public class MainActivity extends AppCompatActivity {
    private static  final String TAG = "sampletest";
    private QLChat chat;
    private QLLocalize qlLocalize;
    private QLHuman human;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().cancelAll();
            }
        });
        findViewById(R.id.btn_test_001).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLDialogflowChatbotBuilder qlDialogflowChatbotBuilder = new QLDialogflowChatbotBuilder(getApplicationContext(), R.raw.credentials, "test-session-id");

                qlDialogflowChatbotBuilder.setOnDetectedIntentListener(new OnDetectedIntentListener() {
                    @Override
                    public void onDetected(Bundle payload) {
                        for(String key: payload.keySet()){
                            Toast.makeText(MainActivity.this, payload.getString(key), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                QLPepper.getInstance().buildChat().addChatbotBuilder(qlDialogflowChatbotBuilder).run();
            }
        });
        findViewById(R.id.btn_test_002).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().buildAnimate().addResourceId(R.raw.anim001).run(new QLActionCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Call onSuccess");
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Call onCancel");
                    }

                    @Override
                    public void onError(String s) {
                        Log.d(TAG, "Call onError" + s);
                    }
                });
            }
        });
        findViewById(R.id.btn_test_003).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLAnimate m_animate = QLPepper.getInstance().buildAnimate();
                ArrayList animList = new ArrayList<>();
                animList.add(R.raw.anim001);
                animList.add(R.raw.anim002);
                animList.add(R.raw.anim003);
                m_animate.addResourceId(animList);
                m_animate.run();
            }
        });
        findViewById(R.id.btn_test_004).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().buildAnimate().addResourceId(0).run();
            }
        });
        findViewById(R.id.btn_test_005).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat = QLPepper.getInstance().buildChat()
                        .addResourceId(R.raw.topic001)
                        .addResourceId(R.raw.topic002)
                        .addResourceId(R.raw.topic003);
                chat.run(new QLActionCallback<String>() {
                    @Override
                    public void onSuccess(String value) {
                        Log.d(TAG, "Call onSuccess");
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Call onCancel");
                    }

                    @Override
                    public void onError(String message) {
                        Log.d(TAG, "Call onError" + message);
                    }
                });
            }
        });
        findViewById(R.id.btn_test_006).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().cancelAll();
            }
        });
        findViewById(R.id.btn_test_007).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat.goTo(R.raw.topic001, "sample1");
            }
        });
        findViewById(R.id.btn_test_008).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat = QLPepper.getInstance().buildChat()
                        .addResourceId(R.raw.topic004)
                        .setLanguage(QLLanguage.Language.ENGLISH);
                chat.run(new QLActionCallback<String>() {
                    @Override
                    public void onSuccess(String value) {
                        Log.d(TAG, "Call onSuccess");
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Call onCancel");
                    }

                    @Override
                    public void onError(String message) {
                        Log.d(TAG, "Call onError" + message);
                    }
                });
            }
        });
        findViewById(R.id.btn_test_009).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat.goTo(R.raw.topic004, "sample4");
            }
        });
        findViewById(R.id.btn_test_010).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat = QLPepper.getInstance().buildChat()
                        .addResourceId(R.raw.topic003);
                chat.setQlBookmarkReachedListener(new QLBookmarkReachedListener() {
                    @Override
                    public void onBookmarkReached(String bookmarkName) {
                        Toast.makeText(getApplicationContext(), "onBookmarkReached: " + bookmarkName, Toast.LENGTH_SHORT).show();
                    }
                });
                chat.run(new QLActionCallback<String>() {
                    @Override
                    public void onSuccess(String value) {
                        Log.d(TAG, "Call onSuccess");
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Call onCancel");
                    }

                    @Override
                    public void onError(String message) {
                        Log.d(TAG, "Call onError" + message);
                    }
                });
            }
        });
        findViewById(R.id.btn_test_011).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLListen m_listen = QLPepper.getInstance().buildListen().addPhrase("こんにちわ");
                m_listen.run(new QLActionCallback<String>() {
                    @Override
                    public void onSuccess(String value) {
                        Toast.makeText(getApplicationContext(), "onSuccess: " + value, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }
        });
        findViewById(R.id.btn_test_012).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qlLocalize = QLPepper.getInstance().buildLocalize();
                qlLocalize.run(new QLActionCallback<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        Toast.makeText(getApplicationContext(), "onSuccess: ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), "onCancel: ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getApplicationContext(), "onError: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        findViewById(R.id.btn_test_013).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qlLocalize.saveMap("samplemap", new QLActionCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean value) {
                        Toast.makeText(getApplicationContext(), "onSuccess: " + value, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), "onCancel: ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getApplicationContext(), "onError: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        findViewById(R.id.btn_test_014).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qlLocalize = QLPepper.getInstance().buildLocalize();
                qlLocalize.setMapFileName("samplemap");
                qlLocalize.run(new QLActionCallback<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        Toast.makeText(getApplicationContext(), "onSuccess: ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), "onCancel: ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getApplicationContext(), "onError: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        findViewById(R.id.btn_test_015).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qlLocalize = QLPepper.getInstance().buildLocalize();
                qlLocalize.setMapFileName("samplemapfdsd");
                qlLocalize.run(new QLActionCallback<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        Toast.makeText(getApplicationContext(), "onSuccess: ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), "onCancel: ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getApplicationContext(), "onError: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        findViewById(R.id.btn_test_016).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLLookAt qlLookat = QLPepper.getInstance().buildLookAt().setDestination(new QLFrame(QLFrame.FRAME_TYPE_ROBOT), 1, 1);
                qlLookat.run();
            }
        });
        findViewById(R.id.btn_test_017).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().buildSay().addPhrase("こんにちは 1").run();
                QLPepper.getInstance().buildSay().addPhrase("おはようございます").run();
            }
        });
        findViewById(R.id.btn_test_018).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLGoTo m_goTo = QLPepper.getInstance().buildGoTo().setDestination(new QLFrame(QLFrame.FRAME_TYPE_ROBOT), 1, 1);
                m_goTo.run();
            }
        });
        findViewById(R.id.btn_test_019).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLSay m_say = QLPepper.getInstance().buildSay().addPhrase("");
                m_say.run(new QLActionCallback<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        Toast.makeText(getApplicationContext(), "onSuccess: ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), "onCancel: ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getApplicationContext(), "onError: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        findViewById(R.id.btn_test_020).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> sayList = new ArrayList<>();
                QLSay m_say = QLPepper.getInstance().buildSay().addPhrase(sayList);
                m_say.run();
            }
        });
        findViewById(R.id.btn_test_021).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLSay m_say = QLPepper.getInstance().buildSay().addResourceId(R.string.sample1);
                m_say.run();
            }
        });
        findViewById(R.id.btn_test_022).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().addQLEngagedHumanChangedListener(new QLEngagedHumanChangedListener() {
                    @Override
                    public void onEngagedHumanChanged(QLHuman human) {
                        if(MainActivity.this.human == null) MainActivity.this.human = human;
                        Toast.makeText(getApplicationContext(), "age:" + human.getAge(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        findViewById(R.id.btn_test_023).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().removeAllEngagedHumanChangedListener();
            }
        });

        findViewById(R.id.btn_test_024).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().addOnTouchedListener(new QLTouchedListener() {
                    @Override
                    public void onTouched(QLSensor sensor) {
                        Toast.makeText(getApplicationContext(), "age:" + sensor.name(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        findViewById(R.id.btn_test_025).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().removeAllOnTouchedListener();
            }
        });

        findViewById(R.id.btn_test_025).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().removeAllOnTouchedListener();
            }
        });

        findViewById(R.id.btn_test_026).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                human.update(new QLActionCallback<QLHuman>() {
                    @Override
                    public void onSuccess(QLHuman value) {
                        Toast.makeText(getApplicationContext(), "age:" + value.getAge(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }
        });

        findViewById(R.id.btn_test_027).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().buildTakePicture().run(new QLActionCallback<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap value) {
                        findViewById(R.id.main).setBackground(new BitmapDrawable(getResources(), value));
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }
        });
        findViewById(R.id.btn_test_027).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().buildLocalize().setMapFileName("hogehgoe").run();
            }
        });

        findViewById(R.id.btn_test_028).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QLPepper.getInstance().addQLHumansAroundChangedListener(new QLHumansAroundChangedListener() {
                    @Override
                    public void onHumansAroundChanged(List<QLHuman> humanList) {
                        Log.d("QLTest", "onHumansAroundChanged Test");
                    }
                });

                QLPepper.getInstance().addOnTouchedListener(new QLTouchedListener() {
                    @Override
                    public void onTouched(QLSensor sensor) {
                        Log.d("QLTest", "onTouched Test" + sensor);
                    }
                });
                QLPepper.getInstance().addQLEngagedHumanChangedListener(new QLEngagedHumanChangedListener() {
                    @Override
                    public void onEngagedHumanChanged(QLHuman human) {
                        Log.d("QLTest", "addQLEngagedHumanChangedListener Test");
                    }
                });

                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_test_029).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Resources res = getResources();

                InputStream is = null;
                BufferedReader br = null;
                StringBuilder sb = new StringBuilder();
                try{
                    try {
                        is = res.openRawResource(R.raw.dog_a001);
                        br = new BufferedReader(new InputStreamReader(is));
                        String str;
                        while((str = br.readLine()) != null){
                            sb.append(str +"\n");
                        }
                    } finally {
                        if (br !=null) br.close();
                    }
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "読み込み失敗", Toast.LENGTH_SHORT).show();
                }

                QLPepper.getInstance().buildSay().addPhrase("aa", sb.toString()).run();
            }
        });

        findViewById(R.id.btn_test_030).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Resources res = getResources();

                InputStream is = null;
                BufferedReader br = null;
                StringBuilder sb = new StringBuilder();
                try{
                    try {
                        is = res.openRawResource(R.raw.dog_a001);
                        br = new BufferedReader(new InputStreamReader(is));
                        String str;
                        while((str = br.readLine()) != null){
                            sb.append(str +"\n");
                        }
                    } finally {
                        if (br !=null) br.close();
                    }
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "読み込み失敗", Toast.LENGTH_SHORT).show();
                }

                QLPepper.getInstance().buildAnimate().addAnimationXml(sb.toString()).run();
            }
        });

        QLPepper.getInstance().register(this);
    }

    @Override
    protected void onDestroy() {
        QLPepper.getInstance().unregister(this);
        super.onDestroy();
    }

}

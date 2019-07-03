# qisdk_wrapper

Android Studio対応版PepperではAndroidアプリを開発し、
旨のディスプレイ(Android 6.0)にインストールすることで、
アプリとPepperを連携させることが出来るようになりました。

このライブラリはPepperとアプリを簡単に連携するためのライブラリです。
Pepperとライトに連携することを目的としており、
マニアックに使う必要がある方は本家のQiSDKをご利用ください。

QiSDK本家：https://developer.softbankrobotics.com/pepper-qisdk

## 導入方法
アプリのbuild.gradleにレポジトリと依存関係の記述を追記してください。

```
repositories {
    maven { url 'https://github.com/Abidarma-dev/qisdk_wrapper/raw/master/repository/' }
}

dependencies {

    implementation "jp.pepper-atelier-akihabara:qisdk_wrapper:1.0.0"

}
```

## 使い方

### Activityの登録
Pepperに指示を出すためにはライブラリにActivityの登録・解除をする必要があります。
登録・解除はonCreateとonDestoryで行ってください。
また、Pepperに指示を出せるのはActivityがフォアグラウンドにいる間だけですので注意が必要です。

```
public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        QLPepper.getInstance().register(this);
    }

    @Override
    protected void onDestroy() {
        QLPepper.getInstance().unregister(this);
        super.onDestroy();
    }
}
```

### 発話
発話させる方法です。
```
        QLPepper.getInstance().buildSay("こんにちは").run();
```

### 聞き取り
聞き取りの方法です。
聞き取ったフレーズはコールバックにて受け取れます。
```
        List<String> list = Arrays.asList("おはよう", "グッドモーニング", "おは");
        QLPepper.getInstance().buildListen(list).run(new QLActionCallback<String>() {
            @Override
            public void onSuccess(String s) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(String s) {

            }
        });
```

### アニメーション
アニメーションファイルを作成して、Pepperにアニメーションさせる方法です。
```
          QLPepper.getInstance().buildAnimate(R.raw.anim001).run();
```

### チャット
会話の内容が定義されたTopicファイルを作成して、Pepperに発話させる方法です。
```
          QLPepper.getInstance().buildChat(R.raw.topic_001).run();
```

### 移動
Pepperの現在位置を中心に前1m、右1mの位置に移動する方法です。
```
          QLPepper.getInstance().buildGoTo(1.0,1.0).run();
```

### タッチイベント
各種タッチセンサーを利用する方法です。
```
       QLPepper.getInstance().addOnTouchedListener(new QLTouchedListener() {
            @Override
            public void onTouched(QLSensor qlSensor) {
                if(QLSensor.Head == qlSensor){
                    Toast.makeText(getApplicationContext(), "頭タッチ", Toast.LENGTH_SHORT).show();
                }
            }
        });
```

### 周辺の人の情報
周辺の人の情報を取得する方法です。
```
        QLPepper.getInstance().addQLHumansAroundChangedListener(new QLHumansAroundChangedListener() {
            @Override
            public void onHumansAroundChanged(List<QLHuman> list) {
                if(list.size() > 0){
                    int age = list.get(0).getAge();
                    Toast.makeText(getApplicationContext(), String.format("あなたは%d歳に見えます。", age), Toast.LENGTH_SHORT).show();
                }
            }
        });
```

### キャンセル
各操作を中断する方法です。

#### すべてキャンセル
```
        QLPepper.getInstance().cancelAll();
```

#### 特定の操作をキャンセル
```
        QLSay say = QLPepper.getInstance().buildSay("こんにちは");
        say.run();
        
        say.cancel();
```

### 連続実行
「昔々」、「あるところに」を順番に実行したい場合は、runの引数にfalseを入れてください。
trueの場合、もしくは指定がない場合は、前の操作をキャンセルしてから実行します。
```
        QLPepper.getInstance().buildSay("昔々").run();
        QLPepper.getInstance().buildSay("あるところに").run(false);
```

### その他
あとはjavaDocディレクトリを参照。

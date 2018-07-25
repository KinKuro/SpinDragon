package studies.kinkuro.spindragon;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mp;

    SoundPool sp;
    int sdBtn;

    ImageView ivTitle, btnStart, btnExit;
    int width, height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //new MediaPlayer로 하는거보다 이게 더 쉬워
        mp = MediaPlayer.create(this, R.raw.dragon_flight);
        mp.setLooping(true);    //반복재생여부

        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        sdBtn = sp.load(this, R.raw.ui_button, 1);

    }

    @Override
    protected void onPause() {
        if(mp != null && mp.isPlaying()) {
            mp.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(mp != null){
            mp.stop();
            mp.release();
            mp = null;
        }
        sp.release();       sp = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if(mp != null){
            if(G.isMusic)   mp.setVolume(0.5f, 0.5f);   //소프트웨어 소리 크기. 왼쪽 오른쪽을 다르게 둔다.
            else            mp.setVolume(0,0);      //음소거 = 음악은 계속 나오지만, 소리만 안나오게 한다.
            mp.start();
        }
        super.onResume();
    }

    //GameActivity 화면으로 넘어가기
    public void clickStart(View view){
        if(G.isSound)  sp.play(sdBtn, 1, 1, 1, 0, 1);
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    //Activity 종료
    public void clickExit(View view){
        if(G.isSound)  sp.play(sdBtn, 1, 1, 1, 0, 1);
        finish();
    }
}

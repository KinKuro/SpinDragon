package studies.kinkuro.spindragon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class IntroActivity extends AppCompatActivity {

    ImageView ivLogo;

    //스케줄 관리 객체(비서객체)
    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        ivLogo = findViewById(R.id.iv_logo);

        //View를 Animation 할 수 있는 객체 생성
        /*
        AlphaAnimation ani = new AlphaAnimation(0.0f, 1.0f);
        ani.setDuration(3000);
        */
        //appear_logo.xml 문서를 읽어서 animation객체로 생성(일반적으로 java로 안짜고 xml로 짜서 읽어온다.)
        Animation ani = AnimationUtils.loadAnimation(this, R.anim.appear_logo);

        ivLogo.startAnimation(ani);

        //4초 후에 MainActivity를 실행하기
            //스케줄 관리 객체에게 스케줄 등록
        timer.schedule(task, 4000);

        loadData();

    }//onCreate()...

    //SharedPreference에 저장된 데이터를 불러오는 메소드
    void loadData(){
        SharedPreferences pref = getSharedPreferences("Data", MODE_PRIVATE);

        G.gem = pref.getInt("Gem", 0);
        G.champScore = pref.getInt("ChampScore", 0);
        G.isMusic = pref.getBoolean("Music", true);
        G.isSound = pref.getBoolean("Sound", true);
        G.isVibrate = pref.getBoolean("Vibrate", true);
        G.champImg = pref.getString("ChampImg", null);
    }

    //timer의 스케쥴링 작업을 수행하는 객체 생성
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    };
}

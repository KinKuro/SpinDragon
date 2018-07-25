package studies.kinkuro.spindragon;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class GameActivity extends AppCompatActivity {

    GameView gameView;
    TextView tvScore, tvCoin, tvGem, tvBomb, tvChamp;

    View dialog = null;
    Animation ani;

    MediaPlayer mp;

    ToggleButton tbMusic, tbSound, tbVibe;

    SoundPool sp;
    int sdBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.gameview);

        tvScore = findViewById(R.id.tv_score);
        tvCoin = findViewById(R.id.tv_coin);
        tvGem = findViewById(R.id.tv_gem);
        tvBomb = findViewById(R.id.tv_bomb);
        tvChamp = findViewById(R.id.tv_champ_score);

        mp = MediaPlayer.create(this, R.raw.my_friend_dragon);
        mp.setLooping(true);

        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        sdBtn = sp.load(this, R.raw.ui_button, 1);

        tbMusic = findViewById(R.id.tb_music);
        tbSound = findViewById(R.id.tb_sound);
        tbVibe = findViewById(R.id.tb_vibrate);

        tbMusic.setOnCheckedChangeListener(ccListener);
        tbSound.setOnCheckedChangeListener(ccListener);
        tbVibe.setOnCheckedChangeListener(ccListener);

        tbMusic.setChecked(G.isMusic);
        tbSound.setChecked(G.isSound);
        tbVibe.setChecked(G.isVibrate);

    }

    CompoundButton.OnCheckedChangeListener ccListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            if(G.isSound)  sp.play(sdBtn, 1, 1, 1, 0, 1);
            switch(compoundButton.getId()){
                case R.id.tb_music:
                    G.isMusic = checked;
                    if(G.isMusic) mp.setVolume(0.5f, 0.5f);
                    else          mp.setVolume(0, 0);
                    break;
                case R.id.tb_sound:
                    G.isSound = checked;
                    break;
                case R.id.tb_vibrate:
                    G.isVibrate = checked;
                    break;
            }

        }
    };

    public void clickPause(View v){
        if(dialog != null) return;

        if(G.isSound)  sp.play(sdBtn, 1, 1, 1, 0, 1);

        gameView.pauseGame();
        dialog = findViewById(R.id.dialog_pause);
        dialog.setVisibility(View.VISIBLE);
        Animation ani = AnimationUtils.loadAnimation(this, R.anim.appear_dialog_pause);
        dialog.startAnimation(ani);

    }//clickPause()...

    public void clickQuit(View v){
        if(dialog != null) return;      //다른 다이알로그가 있으면 하지 마

        if(G.isSound)  sp.play(sdBtn, 1, 1, 1, 0, 1);

        dialog = findViewById(R.id.dialog_quit);
        dialog.setVisibility(View.VISIBLE);
        Animation ani = AnimationUtils.loadAnimation(this, R.anim.appear_dialog_quit);
        dialog.startAnimation(ani);
        gameView.pauseGame();


    }//clickQuit()...

    @Override
    protected void onResume() {
        super.onResume();

        if(mp != null){
            if(G.isMusic)   mp.setVolume(0.5f, 0.5f);
            else            mp.setVolume(0, 0);
        }
        mp.start();
    }

    //Activity가 화면에서 보이지 않게 되면 자동으로 실행되는 메소드
    @Override
    protected void onPause() {
        if(mp != null) mp.pause();
        gameView.pauseGame();
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

    //Activity의 뒤로가기 버튼을 클릭했을 때 자동으로 실행되는 메소드
    @Override
    public void onBackPressed() {
        if(dialog != null) return;      //다른 다이알로그가 있으면 하지 마
        dialog = findViewById(R.id.dialog_quit);
        dialog.setVisibility(View.VISIBLE);
        Animation ani = AnimationUtils.loadAnimation(this, R.anim.appear_dialog_quit);
        dialog.startAnimation(ani);
        gameView.pauseGame();
    }//onBackPressed()...

    public void clickShopClass(View v){
        appearDialog(R.id.dialog_shop);
    }//clickShopClass()...

    public void clickShopItem(View v){
        appearDialog(R.id.dialog_shop);
    }//clickShopItem()...

    public void clickSetting(View v){
        appearDialog(R.id.dialog_setting);
    }//clickSetting()...

    //다이얼로그를 보이는 작업 메소드
    void appearDialog(int resId){
        if(dialog != null) return;
        if(G.isSound)  sp.play(sdBtn, 1, 1, 1, 0, 1);
        gameView.pauseGame();

        dialog = findViewById(resId);
        dialog.setVisibility(View.VISIBLE);

        ani = AnimationUtils.loadAnimation(this, R.anim.appear_dialog);
        dialog.startAnimation(ani);

    }//appearDialog()...

    //다이얼로그를 숨기는 작업 메소드
    void disappearDialog(){
        ani = AnimationUtils.loadAnimation(this, R.anim.disappear_dialog);
        ani.setAnimationListener(animationListener);
        dialog.startAnimation(ani);
    }

    public void clickBtn(View v){

        if(G.isSound)  sp.play(sdBtn, 1, 1, 1, 0, 1);

        switch (v.getId()){
            case R.id.dialog_quit_ok:
                //게임 종료
                gameView.stopGame();
                finish();
                break;
            case R.id.dialog_quit_cancel:
                dialog.setVisibility(View.GONE);
                dialog = null;  //무조건 이 View를 떼어놔야한다.
                gameView.resumeGame();
                break;
            case R.id.dialog_pause_play:
                Animation ani = AnimationUtils.loadAnimation(this, R.anim.disappear_dialog_pause);
                ani.setAnimationListener(animationListener);
                dialog.startAnimation(ani);
                break;
            case R.id.dialog_shop_check:
                disappearDialog();
                break;
            case R.id.dialog_setting_check:
                disappearDialog();
                break;
        }
    }//clickBtn()...

    Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            dialog.setVisibility(View.GONE);
            dialog = null;
            gameView.resumeGame();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    };

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            gameView.stopGame();

            Bundle data = msg.getData();
            Intent intent = new Intent(GameActivity.this, GameoverActivity.class);
            intent.putExtra("Data", data);
            startActivity(intent);
            finish();
        }
    };

    Handler blinkHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){
                ani = AnimationUtils.loadAnimation(GameActivity.this, R.anim.blink_gameview);
                gameView.startAnimation(ani);
            }
        }
    };

}//GameActivity class...

package studies.kinkuro.spindragon;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class GameoverActivity extends AppCompatActivity {

    ImageView ivImg;
    TextView tvChamp;
    TextView tvYours;

    boolean isChamp = false;

    SoundPool sp;
    int sdBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameover);

        ivImg = findViewById(R.id.iv_photo);
        tvChamp = findViewById(R.id.tv_champ);
        tvYours = findViewById(R.id.tv_yourscore);

        Intent intent = getIntent();
        Bundle data = intent.getBundleExtra("Data");
        int score = data.getInt("Score", 0);
        int coin = data.getInt("Coin", 0);

        int yourScore = score + coin*10;
        String s = String.format("%07d", yourScore);
        tvYours.setText(s);

        isChamp = false;
        if(yourScore > G.champScore){
            G.champScore = yourScore;
            isChamp = true;
        }
        s = String.format("%07d", G.champScore);
        tvChamp.setText(s);

        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        sdBtn = sp.load(this, R.raw.ui_button, 1);

        //챔피언 이미지가 있는가
        if(G.champImg != null){
            Uri uri = Uri.parse(G.champImg);
            ivImg.setImageURI(uri);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int csp = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(csp == PackageManager.PERMISSION_DENIED){
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, 100);
            }
        }
    }

    public void clickImg(View v){
        if(!isChamp) return;
        if(G.isSound)  sp.play(sdBtn, 1, 1, 1, 0, 1);

        //디바이스의 사진을 선택하도록 할거야 - Gallery앱이나 사진앱을 실행
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");    // 이 글자 그대로 써야되
        startActivityForResult(intent, 0);

    }//clickImg()...

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    protected void onDestroy() {
        sp.release();       sp = null;
        super.onDestroy();
    }

    //G 장부를 SharedPreference로 저장해놓는 메소드
    void saveData(){
        SharedPreferences pref = getSharedPreferences("Data", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt("Gem", G.gem);
        editor.putInt("ChampScore", G.champScore);
        editor.putBoolean("Music", G.isMusic);
        editor.putBoolean("Sound", G.isSound);
        editor.putBoolean("Vibrate", G.isVibrate);
        editor.putString("ChampImg", G.champImg);

        editor.commit();
    }//saveData()

    //퍼미션을 받으면 다이얼로그를 띄우겠다
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 100:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "외장 메모리 사용 가능", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //startActivityForResult로 실행한 Activity가 종료되면 자동으로 실행되는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 0:
                if(resultCode == RESULT_OK){
                    //원래는 Uri를 갖고왔는데, 요즘 기종은 Bitmap을 가지고 오는 경우도 있다고 한다...
                    Uri uri = data.getData();
                    if(uri != null){
                        Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
//                      ivImg.setImageURI(uri); //이러면 원본 이미지로 가져와... Glide 쓰셈
                        Glide.with(this).load(uri).into(ivImg);
                        G.champImg = uri.toString();
                    }else {
                        //Uri가 아니라 Bitmap으로 가져왔을 때...
                        Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
                        Bundle bundle = data.getExtras();
                        Bitmap bm = (Bitmap)bundle.get("data"); //이 이름으로 꾸러미 이름이 정해져있어
//                      ivImg.setImageBitmap(bm);
                        Glide.with(this).load(bm).into(ivImg);
                    }
                }
                break;
        }
    }

    public void clickRetry(View v){
        if(G.isSound)  sp.play(sdBtn, 1, 1, 1, 0, 1);
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        finish();
    }

    public void clickExit(View v){
        if(G.isSound)  sp.play(sdBtn, 1, 1, 1, 0, 1);
        finish();
    }
}

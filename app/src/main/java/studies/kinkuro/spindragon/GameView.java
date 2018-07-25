package studies.kinkuro.spindragon;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.Animation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Random;

/**
 * Created by alfo6-2 on 2018-03-28.
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback{

    Context context;
    SurfaceHolder holder;

    int width, height;

    GameThread gameThread;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        holder = getHolder();
        holder.addCallback(this);

    }//constructor...

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //생성자가 끝나고 GameView가 화면에 보여질 때
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //surfaceCreated()가 실행된 후 자동 실행
        //Game진행 작업 시작
        if(gameThread == null){
            width = getWidth();
            height = getHeight();

            gameThread = new GameThread();
            gameThread.start();
        }else {
            //게임이 한번 멈췄다가 다시 불러진 상태
             //gameThread에게 게임을 재시작하라고 할거야(resume)
            gameThread.resumeThread();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //GameView가 화면에서 보이지 않을 때 자동 실행
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //TODO: Multi-touch 인식하기
        int action = event.getActionMasked();
        int x, y;
        int cnt = event.getPointerCount();
        switch(action){
            case MotionEvent.ACTION_DOWN:
                x = (int)event.getX();                y = (int)event.getY();
                gameThread.touchDown(x, y);
                break;
            case MotionEvent.ACTION_UP:
                x = (int)event.getX();                y = (int)event.getY();
                gameThread.touchUp(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                for(int i = 0; i < cnt; i++){
                    x = (int)event.getX(i);                y = (int)event.getY(i);
                    gameThread.touchMove(x, y);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                for(int i = 0 ; i < cnt ; i++){
                    x = (int)event.getX(i);                y = (int)event.getY(i);
                    gameThread.touchDown(x, y);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                /*
                cnt = event.getPointerCount();
                //일반적으로 조이패드를 누르다가 폭탄을 누를거라고 상정하고
                //먼저 누른 쪽을 조이패드라고 생각할거야
                //그래서 0번을 제외한 나머지만 touchUp이라고 생각되게
                if(cnt > 1){
                    for(int i = 0; i < cnt; i++){
                        x = (int)event.getX(i);                y = (int)event.getY(i);
                        gameThread.touchUp(x, y);
                    }
                }else {
                    for(int i = cnt; i > 0; i--){
                        x = (int)event.getX(i);                y = (int)event.getY(i);
                        gameThread.touchUp(x, y);
                    }
                    x = (int)event.getX(0);                y = (int)event.getY(0);
                    gameThread.touchDown(x, y);
                }
                break;
                */
        }
        return true;
    }

    //게임을 멈추는 메소드
    void stopGame(){
        gameThread.stopThread();
    }
    //게임을 일시정지하는 메소드
    void pauseGame(){
        gameThread.pauseThread();
    }
    //게임을 이어하는 메소드
    void resumeGame(){
        gameThread.resumeThread();
    }


    //실제 게임의 모든 작업을 수행하는 Inner Thread Class...
    /////////////////////////////////////////////////////
    class GameThread extends Thread{

        boolean isRun = true;
        boolean isWait = false;

        Random rnd = new Random();

        Bitmap imgBack;
        int posBack = 0;    //배경 이미지의 x좌표

        //플레이어 객체
        Bitmap[][] imgPlayer = new Bitmap[3][4];    //상태가 3개, 날개짓은 4번을 연속으로(하-중-상-중)
        Player player;
        int playerKind = 0;

        //조이패드
        Bitmap imgJoypad;
        int jpx, jpy;   //조이패드가 그려질 위치의 좌표
        int jpr;   //조이패드 그림의 절반값(원이니까)
        boolean isJoypad = false;   //조이패드를 누르고 있나

        //Bitmap의 투명도(alpha)를 적용하기 위한 Paint객체
        Paint paint = new Paint();

        //미사일 객체
        Bitmap[] imgMissile = new Bitmap[3];
        ArrayList<Missile> missiles = new ArrayList<>();
        int missileGap = 3;

        //적군 객체
        Bitmap[][] imgEnemy = new Bitmap[3][4];     //적군이 3종류, 날개짓은 4번을 연속으로(하-중-상-중)
        ArrayList<Enemy> enemies = new ArrayList<>();
        int level = 1;
        Bitmap[][] imgGauge = new Bitmap[2][];

        //먼지 객체
        Bitmap[] imgDust = new Bitmap[6];
        ArrayList<Dust> dusts = new ArrayList<>();

        //아이템 객체
        Bitmap[] imgItem = new Bitmap[7];
        ArrayList<Item> items = new ArrayList<>();

        //보호막과 스트롱
        Bitmap imgProtect;
        int protectRad;     //보호막 이미지의 반지름
        int protectAng;     //보호막 이미지의 회전각도
        Bitmap imgStrong;

        //아이템 지속시간
        int fastTime = 0;
        int protectTime = 0;
        int magnetTime = 0;
        int strongTime = 0;

        //폭탄버튼
        Bitmap imgBomb;
        Rect rectBomb;  //그냥 사각형 버튼이라고 생각하자
        boolean isBomb = false; //폭탄 버튼을 눌렀는가
        int bomb = 3;   //폭탄의 갯수

        //TextView에 띄울 숫자들(폭탄은 위에 있으니 생략)
        int score = 0;
        int coin = 0;

        //효과음
        SoundPool sp;
        int sdChDie, sdFire, sdCoin, sdGem, sdProtect, sdItem, sdMonDie;

        //진동
        Vibrator vibrator;

        /////////메소드 영역////////////////
        //초기값들을 설정하는 메소드(생성자의 느낌?)
        void init(){
            //Bitmap 객체 생성하기
            createBitmaps();

            //플레이어 객체 생성
            player = new Player(width, height, imgPlayer, playerKind);

            //Joypad의 위치 좌표
            jpx = jpr;      jpy = height - jpr;

            //효과음 변수 작업하기
            sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            sdChDie = sp.load(context, R.raw.ch_die, 1); //우선순위는 나중에 play때 줄꺼니까 여기서는 1
            sdFire = sp.load(context, R.raw.fireball, 1);
            sdCoin = sp.load(context, R.raw.get_coin, 1);
            sdGem = sp.load(context, R.raw.get_gem, 1);
            sdProtect = sp.load(context, R.raw.get_invincible, 1);
            sdItem = sp.load(context, R.raw.get_item, 1);
            sdMonDie = sp.load(context, R.raw.mon_die, 1);

            //진동관리자 객체 얻어오기
            vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        }//init()...

        //GameActivity의 TextView에 값 설정하기
        void setTextView(){
            //Activity 클래스의 runOnUiThread()와 같은 역할을 하는 View의 메소드가 있어
            post(new Runnable() {
                @Override
                public void run() {
                    GameActivity ga = (GameActivity)context;
                    String s = String.format("%07d", score);
                    ga.tvScore.setText(s);
                    s = String.format("%04d", coin);
                    ga.tvCoin.setText(s);
                    s = String.format("%04d", G.gem);
                    ga.tvGem.setText(s);
                    s = String.format("%04d", bomb);
                    ga.tvBomb.setText(s);
                    s = String.format("%07d", G.champScore);
                    ga.tvChamp.setText(s);
                }
            });
        }

            //그림을 Bitmap객체로 만들어내는 작업 메소드 - init()의 분리 1
        void createBitmaps(){
            Resources res = context.getResources();
            //배경 Bitmap 준비
            imgBack = BitmapFactory.decodeResource(res, R.drawable.back_01+rnd.nextInt(6));
            imgBack = Bitmap.createScaledBitmap(imgBack, width, height, true);

            //Player Bitmap 준비
            for(int i = 0; i < 3; i++){
                for(int k = 0; k < 3; k++){
                    imgPlayer[i][k] = BitmapFactory.decodeResource(res, R.drawable.char_a_01+(3*i)+k);
                    imgPlayer[i][k] = Bitmap.createScaledBitmap(imgPlayer[i][k], height/8, height/8, true); //리사이징 할 때는 짧은쪽에 맞춰줘(이 그림은 똑같지만)
                }
                imgPlayer[i][3] = imgPlayer[i][1];
            }

            //조이패드 준비
            imgJoypad = BitmapFactory.decodeResource(res, R.drawable.img_joypad);
            imgJoypad = Bitmap.createScaledBitmap(imgJoypad, height/2, height/2, true);
            jpr = imgJoypad.getWidth()/2;

            //미사일 Bitmap 준비
            for(int i = 0 ; i < 3; i++){
                imgMissile[i] = BitmapFactory.decodeResource(res, R.drawable.bullet_01+i);
                imgMissile[i] = Bitmap.createScaledBitmap(imgMissile[i], height/10, height/10, true);
            }

            //적군'들' Bitmap 준비
            for(int i = 0; i < 3; i++){
                for(int k = 0; k < 3; k++){
                    imgEnemy[i][k] = BitmapFactory.decodeResource(res, R.drawable.enemy_a_01+(3*i)+k);
                    imgEnemy[i][k] = Bitmap.createScaledBitmap(imgEnemy[i][k], height/9, height/9, true);
                }
                imgEnemy[i][3] = imgEnemy[i][1];
            }

            //적군의 체력게이지 만들기
            imgGauge[0] = new Bitmap[5];        //노랑이
            for(int i = 0 ; i < imgGauge[0].length ; i++){
                imgGauge[0][i] = BitmapFactory.decodeResource(res, R.drawable.gauge_step5_01+i);
                imgGauge[0][i] = Bitmap.createScaledBitmap(imgGauge[0][i], height/9, height/36 ,true);
            }
            imgGauge[1] = new Bitmap[3];        //핑크
            for(int i = 0 ; i < imgGauge[1].length ; i++){
                imgGauge[1][i] = BitmapFactory.decodeResource(res, R.drawable.gauge_step3_01+i);
                imgGauge[1][i] = Bitmap.createScaledBitmap(imgGauge[0][i], height/9, height/36 ,true);
            }

            //Dust Bitmap 만들기
            Bitmap imgDustSrc = BitmapFactory.decodeResource(res, R.drawable.dust);
            float[] ratio = new float[]{1.0f, 1.4f, 2.0f, 0.7f, 0.3f, 1.2f};
            for(int i = 0 ; i < 6; i ++){
                int size = (int)(height/9 * ratio[i]);
                imgDust[i] = Bitmap.createScaledBitmap(imgDustSrc, size, size, true);
            }

            //Item Bitmap 만들기
            for(int i = 0 ; i < imgItem.length; i++){
                imgItem[i] = BitmapFactory.decodeResource(res, R.drawable.item_0_coin+i);
                imgItem[i] = Bitmap.createScaledBitmap(imgItem[i], height/16, height/16, true);
            }

            //보호막 이미지
            imgProtect = BitmapFactory.decodeResource(res, R.drawable.effect_protect);
            imgProtect = Bitmap.createScaledBitmap(imgProtect, height/4, height/4, true);
            protectRad = imgProtect.getWidth()/2;

            //스트롱 이미지
            imgStrong = BitmapFactory.decodeResource(res, R.drawable.bullet_04);
            imgStrong = Bitmap.createScaledBitmap(imgStrong, height/10, height/10, true);

            //폭탄버튼 이미지
            int sizeBomb = height/5;    //높이의 1/5만하게 만들거야
            imgBomb = BitmapFactory.decodeResource(res, R.drawable.btn_bomb);
            imgBomb = Bitmap.createScaledBitmap(imgBomb, sizeBomb, sizeBomb,true);
            rectBomb = new Rect(width-sizeBomb, height-sizeBomb, width, height);


            //그런데 Bitmap은 App이 종료되어도 메모리에서 사라지지 않고 찌꺼기로 남아
            //그래서 그걸 프로그래머가 지워주는 경우가 많다.
        }//createBitmaps()...

        //Resource(Bitmap뿐만이 아니야) 제거하기
        void removeResource(){
            //배경 지우기
            imgBack.recycle();
            imgBack = null;

            //Player Bitmap 지우기
            for(int i = 0; i < 3; i++){
                for(int k = 0; k < 3; k++){     //1번방에서 이미 지우니까 3번방에서 지우려고 하면 error
                    imgPlayer[i][k].recycle();
                    imgPlayer[i][k] = null;
                }
                imgPlayer[i][3] = null;
            }

            //Joypad 지우기
            imgJoypad.recycle();        imgJoypad = null;

            //미사일 Bitmap 지우기
            for(int i = 0; i < imgMissile.length ; i++){
                imgMissile[i].recycle();        imgMissile[i] = null;
            }

            //Enemy Bitmap 지우기
            for(int i = 0; i < 3; i++){
                for(int k = 0; k < 3; k++){     //1번방에서 이미 지우니까 3번방에서 지우려고 하면 error
                    imgEnemy[i][k].recycle();
                    imgEnemy[i][k] = null;
                }
                imgEnemy[i][3] = null;
            }

            //적군의 체력게이지 Bitmap 지우기
            for(int i = 0 ; i < imgGauge.length ; i++) {
                for (int k = 0; k < imgGauge[i].length; k++) {
                    imgGauge[i][k].recycle();
                    imgGauge[i][k] = null;
                }
            }

            //먼지 Bitmap 지우기
            for(int i = 0; i < imgDust.length ; i++){
                imgDust[i].recycle();   imgDust[i] = null;
            }

            //Item Bitmap 지우기
            for(int i = 0; i < imgItem.length ; i++){
                imgItem[i].recycle();   imgItem[i] = null;
            }

            //보호막과 스트롱 Bitmap 지우기
            imgProtect.recycle();            imgProtect = null;
            imgStrong.recycle();            imgStrong = null;

            //폭탄 버튼 Bitmap 지우기
            imgBomb.recycle();            imgBomb = null;

            //SoundPool 객체 지우기
            if(sp != null){
                sp.release();           sp = null;
            }

        }//removeResource()...

        //아이템 지속시간 체크 작업 메소드
        void checkItemTime(){
            if(fastTime > 0){
                fastTime--;
                if(fastTime <= 0){
                    player.da = 3;
                }
            }
            if(protectTime > 0) protectTime--;
            if(magnetTime > 0) magnetTime--;
            if(strongTime > 0) strongTime--;
        }

        //아이템 종류에 따라서 효과 만들기
        void actionItem(int kind){
            switch(kind){
                case 0:     //COIN
                    if(G.isSound) sp.play(sdCoin, 1, 1, 2, 0, 1);
                    coin++;
                    setTextView();
                    break;
                case 1:     //GEM
                    if(G.isSound) sp.play(sdGem, 1, 1, 3, 0, 1);
                    G.gem++;
                    setTextView();
                    break;
                case 2:     //FAST
                    if(G.isSound) sp.play(sdItem, 1, 1, 3, 0, 1);
                    player.da = 9;
                    fastTime = 200;
                    break;
                case 3:     //PROTECT
                    if(G.isSound) sp.play(sdProtect, 0.8f, 0.8f, 4, 0, 1);
                    protectTime = 200;
                    break;
                case 4:     //MAGNET
                    if(G.isSound) sp.play(sdItem, 1, 1, 3, 0, 1);
                    magnetTime = 200;
                    break;
                case 5:     //BOMB
                    if(G.isSound) sp.play(sdItem, 1, 1, 3, 0, 1);
                    bomb++;
                    setTextView();
                    break;
                case 6:     //STRONG
                    if(G.isSound) sp.play(sdItem, 1, 1, 3, 0, 1);
                    strongTime = 200;
                    break;
            }
        }//actionItem()...

        //2.1 화면에 보여질 객체 만들기
        void makeAll(){

            //미사일 만들기
            if(fastTime > 0){
                if(G.isSound)   sp.play(sdFire, 0.1f, 0.1f, 0, 0, 1.0f);
                missiles.add(new Missile(width, height, imgMissile, player.x, player.y, player.angle, player.kind));
            } else {
                missileGap--;
                if(missileGap <= 0){
                    if(G.isSound)   sp.play(sdFire, 0.1f, 0.1f, 0, 0, 1.0f);
                    missiles.add(new Missile(width, height, imgMissile, player.x, player.y, player.angle, player.kind));
                    missileGap = 3;
                }
            }

            //적군'들' 만들기 - 레벨에 따라서 나오는 빈도를 높일거야
            int p  = rnd.nextInt(15-level);
            if(p == 0){
                enemies.add(new Enemy(width, height, imgEnemy, player.x, player.y, imgGauge));
            }

        }//makeAll()...

        //2.2 움직이기
        void moveAll(){
            //배경 움직이기
//            posBack--;            //
            posBack -= width/600;   //기종마다 비슷한 속도로 화면이 흐른다. 600인건 가장 작은 해상도를 가진 핸드폰이 600정도라서...
            if(posBack <= -width)   posBack += width;   //그림이 다 돌면 원상복귀

            //플레이어 움직이기
            player.move();

            //미사일'들' 움직이기
            for(int i = missiles.size()-1; i >= 0 ; i--){
                Missile t = missiles.get(i);
                t.move();
                if(t.isDead) missiles.remove(i);
            }

            //적군'들' 움직이기
            for(int i = enemies.size()-1; i >=0 ; i--){
                Enemy t = enemies.get(i);
                t.move(player.x, player.y);
                if(t.isOut) enemies.remove(i);
                if(t.isDead){
                    //점수를 올린다.
                    score += (t.kind +1) * 10;
                    setTextView();
                    //폭발효과 생성
                    dusts.add(new Dust(imgDust, t.x, t.y));
                    //효과음
                    if(G.isSound) sp.play(sdMonDie, 1, 1, 1, 0, 1);
                    //아이템 생성
                    items.add(new Item(width, height, imgItem, t.x, t.y));
                    enemies.remove(i);
                }
            }

            //먼지'들' 움직이기
            for(int i = dusts.size()-1 ; i >= 0 ; i--){
                Dust t = dusts.get(i);
                t.move();
                if(t.isDead)    dusts.remove(i);
            }

            //아이템'들' 움직이기
            for(int i = items.size()-1; i >= 0 ; i--){
                Item t = items.get(i);
                if(magnetTime >0 && t.kind<2){
                    t.move(player.x, player.y);
                }else {
                    t.move();
                }
                if(t.isDead)    items.remove(i);
            }

            //아이템 지속시간 체크 메소드 호출
            checkItemTime();
        }//moveAll()...

        //2.3 충돌체크작업
        void checkCollision(){

            //미사일과 적군의 충돌
            for(Missile t : missiles){
                for(Enemy et : enemies){
                    if(Math.pow(t.x-et.x, 2) + Math.pow(t.y-et.y, 2) <= Math.pow(t.w+et.w, 2)){
                        //미사일은 죽이고, 적군의 HP는 줄이자
                        if(strongTime <= 0) t.isDead = true;
                        et.damaged(t.kind+1);
                        score+=5;
                        //Todo: 난이도 조절
                        if(score % 2000 == 0){
                            level++;
                            if(level >= 14) level = 14;
                        }
                        setTextView();
                        break;
                    }
                }
            }//미사일 vs 적군...

            //플레이어와 아이템의 충돌
            for(Item t : items){
                if(Math.pow(player.x-t.x, 2) + Math.pow(player.y-t.y, 2) <= Math.pow(player.w+t.w, 2)){
                    actionItem(t.kind);
                    t.isDead = true;
                }
            }//플레이어 vs 아이템...

            //플레이어와 적군의 충돌
            for(Enemy t : enemies){
                //보호막의 여부
                if(protectTime >0){
                    if(Math.pow(player.x-t.x, 2) + Math.pow(player.y-t.y, 2) <= Math.pow(protectRad+t.w, 2)){
                        t.isDead = true;
                    }
                } else {
                    if(Math.pow(player.x-t.x, 2) + Math.pow(player.y-t.y, 2) <= Math.pow(player.w+t.w, 2)){
                        t.isDead = true;
                        player.HP--;
                        //맞는 효과(핸들러를 통해서 GameView에 Animation)
                        ((GameActivity)context).blinkHandler.sendEmptyMessage(0);
                        if(G.isVibrate) vibrator.vibrate(1000);
                        if(player.HP<=0){
                            //죽는 효과음
                            if(G.isSound)   sp.play(sdChDie, 1.0f, 1.0f, 5, 0, 1.0f);
                            //GameoverActivity 실행
                            Message msg = new Message();
                            Bundle data = new Bundle();
                            data.putInt("Score", score);
                            data.putInt("Coin", coin);
                            msg.setData(data);
                            ((GameActivity)context).handler.sendMessage(msg);
                        }
                    }
                }
            }//플레이어 vs 적군...
        }//checkCollision()...

        //2.4 그려내기
        void drawAll(Canvas canvas){
            //배경 그리기
            canvas.drawBitmap(imgBack, posBack, 0, null);   //x좌표는 계속 바뀌니까...
            canvas.drawBitmap(imgBack, posBack+width, 0, null); //바뀐만큼 들어와

            //미사일'들' 그리기
            for(Missile t : missiles){
                canvas.save();
                canvas.rotate(t.angle, t.x, t.y);
                canvas.drawBitmap(strongTime > 0 ? imgStrong : t.img, t.x-t.w, t.y-t.h, null);
                canvas.restore();
            }

            //적군'들' 그리기
            for(Enemy t : enemies){
                canvas.save();
                canvas.rotate(t.angle, t.x, t.y);
                canvas.drawBitmap(t.img, t.x-t.w, t.y-t.h, null);
                canvas.restore();
                //적군의 체력게이지 그리기(무조건 적 위에 그릴거야)
                if(t.kind > 0){
                    canvas.drawBitmap(t.imgG, t.x-t.w, t.y-t.h-t.imgG.getHeight()/2, null);
                }
            }

            //아이템'들' 그리기
            for(Item t : items){
                canvas.drawBitmap(t.img, t.x-t.w, t.y-t.h, null);
            }

            //플레이어 그리기
            canvas.save();          //canvas 상태 저장
            canvas.rotate(player.angle, player.x, player.y);        //Spin Dragon이니까 돌려야지
            canvas.drawBitmap(player.img, player.x - player.w, player.y - player.h , null);
            canvas.restore();       //원상복구

            //보호막 이미지 그리기
            if(protectTime > 0){
                protectAng += 15;
                canvas.save();
                canvas.rotate(protectAng, player.x, player.y);
                canvas.drawBitmap(imgProtect, player.x-protectRad, player.y-protectRad, null);
                canvas.restore();
            }

            //먼지들 그리기
            paint.setAlpha(180);
            for(Dust t : dusts){
                for(int i = 0 ; i < 6; i++){
                    canvas.drawBitmap(t.img[i], t.x[i]-t.rad[i], t.y[i]-t.rad[i], paint);
                }
            }

            //Joypad 그리기
            paint.setAlpha(isJoypad ? 180 : 120);   //0 ~ 255(FF). 누르면 240(진하게), 안누르면 120(반투명)
            canvas.drawBitmap(imgJoypad, jpx - jpr, jpy - jpr, paint);

            //폭탄 버튼 그리기
            paint.setAlpha(isBomb ? 180 : 120);
            canvas.drawBitmap(imgBomb, rectBomb.left, rectBomb.top, paint); //사각형 객체한테 빼오면 편해

        }//drawAll()...

        //터치 작업 메소드
        void touchDown(int x, int y){
            //터치다운한 지점 x,y이 조이패드인지
            if(Math.pow(x - jpx, 2) + Math.pow(y - jpy, 2) <= Math.pow(jpr, 2)){
                //터치다운한 x,y와 조이패드의 중심좌표(jpx, jpy) 사이의 각도 계산
                player.radian = Math.atan2(jpy - y, x - jpx);
                isJoypad = true;
                player.canMove = true;
            }//조이패드...

            //터치다운한 지점 x,y가 폭탄버튼인지
            if(rectBomb.contains(x,y)){
                isBomb = true;
                //TODO : 폭발 효과 적용(하고싶으면 해)
                if(bomb>0){     //폭탄 갯수가 1개 이상인가.
                    bomb--;
                    setTextView();
                    for(Enemy t : enemies){
                        if(t.wasShow)       t.isDead = true;        //화면 안에 들어온 애들만 죽이자.
                    }
                }
            }//폭탄버튼...
        }//touchDown()...

        void touchUp(int x, int y){
            isJoypad = false;
            player.canMove = false;
            isBomb = false;
        }//touchUp()...

        void touchMove(int x, int y){
            //조이패드의 각도를 계산
            if(isJoypad){
                player.radian = Math.atan2(jpy - y, x - jpx);
            }
        }//touchMove()...


        @Override
        public void run() {

            //초기값 설정
            init();

            Canvas canvas = null;
            while(isRun){
                //1. canvas 얻어오기
                canvas = holder.lockCanvas();

                //2. canvas에 원하는 작업 수행
                try{
                    synchronized (holder){
                        //2.1 화면에 보여질 객체 만들기
                        makeAll();
                        //2.2 움직이기
                        moveAll();
                        //2.3 충돌체크작업
                        checkCollision();
                        //2.4 그려내기
                        drawAll(canvas);
                    }
                }finally{
                    //3. holder에게 canvas를 MainThread에게 전송하게 하기(post)
                    holder.unlockCanvasAndPost(canvas);
                }

                //pause 체크
                if(isWait){
                    try {
                        synchronized (this){
                            wait();     //gameThread 정지...
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }//while(true)....

            removeResource();

        }//run()...


        //Thread를 멈추는 메소드
        void stopThread(){
            synchronized (this){    this.notify();  }
            isRun = false;
        }

        //Thread를 일시정지하는 메소드
        void pauseThread(){
            isWait = true;
        }

        //Thread를 이어하는 메소드
        void resumeThread(){
            isWait = false;
            synchronized (this){    this.notify();  }
        }

    }//GameThread class...
    /////////////////////////////////////////////////////

}//GameView class....

package studies.kinkuro.spindragon;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by alfo6-2 on 2018-03-30.
 */

public class Enemy {

    int width, height;

    Bitmap img;
    int x, y, w, h;
    int HP;
    boolean isDead = false; //미사일에 맞아서 죽었냐 - 점수
    boolean isOut = false; //화면 밖으로 나가서 죽었냐 - 점수X

    boolean wasShow = false;    //화면으로 들어온 적이 있나
    Rect rect;

    Bitmap[] imgs;
    int index;      //날개짓 순서
    int loop = 0;   //날개짓 빈도
    int kind;       //종류(white, yellow, pink)

    double radian;  //이동 각도(Enemy와 player의 각도 - Radian으로 나와)
    int speed;      //이동 속도

    int angle;      //회전각도

    Bitmap[] imgGs;
    Bitmap imgG;

    public Enemy(int width, int height, Bitmap[][] imgSrc, int px, int py, Bitmap[][] imgGauge) {
        this.width = width;        this.height = height;

        //종류정하기 : 0-white, 1-yellow, 2-pink
            //비율을 5 : 3 : 2로 맞춰주고싶어
        Random rnd = new Random();
        int n = rnd.nextInt(10);
        kind = n<5? 0 : ( n<8 ? 1 : 2 );

        //체력 결정하기 : white-1, yellow-5, pink-3
        HP = kind==0 ? 1 : ( kind==1 ? 5 : 3 );

        //2차원배열(원본)에서 1차원배열 뽑아오기
        imgs = imgSrc[kind];
        img = imgs[index];
        w = img.getWidth()/2;        h = img.getHeight()/2;

        //시작 좌표 구하기 - 중심을 축으로 해서 360를 돌린 후, 화면 바깥으로 빼버리자
        int degree = rnd.nextInt(360);
        double r = Math.toRadians(degree);
        x = (int)(width/2 + Math.cos(r)*width);
        y = (int)(height/2 - Math.sin(r)*height);

        //이동각도&회전각도 결정하기
        radian = Math.atan2( y-py, px-x);
        angle = (int)(270 - Math.toDegrees(radian));

        //이동속도 정하기 - 종류에 따라 다르게
        speed = kind==0 ? w/6 : (kind==1 ? w/8 : w/12);

        //화면 사이즈만한 사각형 객체 생성
        rect = new Rect(0, 0, width, height);

        //이미지 게이지 달아주기
        if(kind > 0){
            imgGs = imgGauge[kind-1];
            imgG = imgGs[index];
        }
    }

    void move(int px, int py){

        //pink인 적군은 각도를 다시 계산하기
        if(kind==2){
            radian = Math.atan2( y-py, px-x);
            angle = (int)(270 - Math.toDegrees(radian));
        }

        //날개짓 애니메이션
        loop++;
        if(loop % 3 ==0){
            index++;
            if( index > 3 ) index = 0;
            img = imgs[index];
        }

        //이동
        x = (int)(x + Math.cos(radian)*speed);
        y = (int)(y - Math.sin(radian)*speed);

        //화면 밖으로 나갔는지 체크
            // 일단 화면에 한번 보인 애들만 없애도록 딱지를 달자
        if(rect.contains(x, y)) wasShow = true;

        if(wasShow){
            if( x < -w || x > width+w || y < -h || y > height+h){
                isOut = true;
            }
        }
    }//move()...

    void damaged(int mPower){
        HP -= mPower;
        if(HP <=0 )  {
            isDead = true;
            return;
        }

        imgG = imgGs[imgGs.length - HP];
    }
}

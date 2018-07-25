package studies.kinkuro.spindragon;

import android.graphics.Bitmap;

/**
 * Created by alfo6-2 on 2018-03-29.
 */

public class Player {

    int width, height;

    Bitmap img;
    int x, y;
    int w, h;
    int HP = 5;
    int loop = 0;
    int angle = 0;  //회전각도
    int da = 3;     //회전각도의 변화량

    boolean canMove = false;    //움직일 수 있는가?

    Bitmap[][] imgs;    //이걸 받아와서 img에 적절한걸 넣을거야

    int index = 0;          //날개짓(하, 중, 상, 중)
    int kind = 0;       //상태(0 : RED, 1 : VIOLET, 2 : BLACK)

    //조이패드로 이동할거야 - 이동각도를 이용해서 그걸 계산하게 할거야
    double radian;     //계산값이 degree가 아니라 radian으로 나오거든...
    int speed;          //이동 속도

    //이 외의 변수는 그때그때 만들거야

    public Player(int width, int height, Bitmap[][] imgs, int kind) {
        this.width = width;
        this.height = height;
        this.imgs = imgs;
        this.kind = kind;

        img = imgs[kind][index];
        w = img.getWidth()/2;     h = img.getHeight()/2;
        x = width/2;            y = height/2;

        //플레이어가 한번 움직일 때 이동할 거리
        speed = w/4;


    }//constructor...

    void move(){

        //날개짓(뺨따귀 안때리게)
        loop++;
        if(loop%3 == 0){
            index++;
            if(index > 3)   index = 0;

            img = imgs[kind][index];
        }

        //회전
        angle += da;

        //이동 - x, y 좌표변경
            //가만히 두면 0도방향(3시방향)으로 가니까, 터치했을 때만 움직이도록 하자
        if(canMove){
            x = (int)(x + Math.cos(radian)*speed);
            y = (int)(y - Math.sin(radian)*speed);
            //벽 넘어 가지 않게 하기
            if(x < w) x = w;
            if(x > width-w) x = width-w;
            if(y < h) y = h;
            if(y > height-h) y = height-h;
        }

    }//move()...

}

package studies.kinkuro.spindragon;

import android.graphics.Bitmap;

/**
 * Created by alfo6-2 on 2018-03-29.
 */

public class Missile {

    int width, height;

    Bitmap img;
    int x, y;
    int w, h;

    double radian;  //이동 각도
    int speed;      //이동 속도

    int angle;     //미사일의 회전각도

    int kind;       //미사일의 종류

    boolean isDead = false;

    public Missile(int width, int height, Bitmap[] imgs, int px, int py, int pAngle, int pKind) {
        this.width = width;        this.height = height;

        x = px;                     y = py;
        kind = pKind;

        img = imgs[kind];
        w = img.getWidth()/2;         h = img.getHeight()/2;

        speed = w/2;

        angle = pAngle;
        radian = Math.toRadians(270 - angle);       //그림이 아래쪽을 보고있어서 270도를 돌렸어

    }

    void move(){
         x = (int)(x + Math.cos(radian)*speed);
         y = (int)(y - Math.sin(radian)*speed);

         if(x < -w || x > width+w || y < -h || y > height+h){
             isDead = true;
         }


    }
}

package studies.kinkuro.spindragon;

import android.graphics.Bitmap;

import java.util.Random;

/**
 * Created by alfo6-2 on 2018-03-30.
 */

public class Dust {

    Bitmap[] img;
    int[] x = new int[6];
    int[] y = new int[6];
    int[] rad = new int[6];

    double[] radian = new double[6];
    int[] speed = new int[6];

    boolean isDead = false;
    int life = 30;

    public Dust(Bitmap[] img, int ex, int ey){
        this.img = img;

        Random rnd = new Random();

        for(int i = 0; i < 6; i++){

            x[i] = ex;            y[i] = ey;
            rad[i] = img[i].getWidth()/2;

            int angle = rnd.nextInt(360);
            radian[i] = Math.toRadians(angle);

            speed[i] = rad[i] / 8;
        }
    }

    void move(){
        for(int i = 0; i < 6; i++){
            x[i] = (int)(x[i] + Math.cos(radian[i])*speed[i]);
            y[i] = (int)(y[i] - Math.sin(radian[i])*speed[i]);
        }

        life--;
        if(life <= 0)   isDead = true;
    }



}

package appnimal2kang.dobe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/**
 * Created by User on 2016-10-01.
 */
public class Graphic extends View {

    float actiontime;

    public Graphic(Context context){
        super(context);
    }

    public void reDraw(int time){
        actiontime = time;
    }

    protected void onDraw(Canvas canvas){

        float degree;

        final float ZERO = -90f; //drawAcr를 이용하면 오른쪽이 0도가 된다. 일반적으로 가장 위를 0으로 보기 때문에 - 90도를 해준다.
        final float DOTONE = 0.5f; //이 소스에서는 12시간이 기준이기 때문에 360/720를 해서 1분당 0.5도를 준다.
        degree = actiontime * DOTONE;


        Paint p = new Paint(); //페인트 객체 p 생성
        p.setAntiAlias(true); //윤곽에 안티알리아싱을 처리해서 부드럽게 할건지 설정
        p.setStyle(Paint.Style.STROKE); //원의 윤곽선만 그리는 페인트 스타일
        p.setStrokeWidth(40); //윤곽선의 두께
        p.setAlpha(0x00); //배경 원의 투명도.

        RectF rectF = new RectF(50, 50, 250, 250); //사각형 객체 rectF를 생성하며 점수 원의 크기를 사각형으로 보고 (좌, 상, 우, 하) 좌표 설정. 좌상이 기준이 된다.

        p.setColor(Color.argb(255,86,55,145));

        canvas.drawArc(rectF, ZERO, degree, false, p);  //점수 원(호)를 그리는 메소드. (정사각형 객체, 시작각도, 끝각도, 시작각도와 끝 각도에서의 중앙으로 선을 그을것이냐, 사용할 페인트 객체). 각도는 시계방향으로 증가한다.


        super.onDraw(canvas);
    }
}

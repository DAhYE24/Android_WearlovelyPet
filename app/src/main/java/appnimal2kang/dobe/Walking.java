package appnimal2kang.dobe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class Walking extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);

        /*첫 프레그먼트 : 지도 부분 표시*/
        Fragment fg = new FragmentWalking();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction(); //전환
        transaction.add(R.id.fg_forWalk, fg); // 프레그먼트 레이아웃 : map_walking
        transaction.commit();
    }

}

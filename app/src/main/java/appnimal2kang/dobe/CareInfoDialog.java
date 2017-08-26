package appnimal2kang.dobe;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class CareInfoDialog extends Dialog implements View.OnClickListener {

    Activity activity;
    String info;

    TextView careInfo;
    Button infoOK;

    public CareInfoDialog(Activity activity, String info){
        super(activity);
        this.activity = activity;
        this.info = info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_care_info);

        careInfo = (TextView)findViewById(R.id.tvCareInfo);
        careInfo.setText(info);
        infoOK = (Button)findViewById(R.id.btInfoOk);
        infoOK.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btInfoOk)
            dismiss();
    }
}

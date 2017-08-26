package appnimal2kang.dobe;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by dh93 on 2016-10-07.
 */
public class Dialog_distance extends Dialog implements View.OnClickListener{
    /* XML Declare */
    EditText editDistance, editNumber; // 반경거리 입력, 비상연락망 입력
    Button btnCurrent, rgDistanceBtn;
    TextView txtNumber; // 반경거리 등록 버튼, 비상연락망 설명글
    ImageButton btnNumberinfo; // 비상연락망 설명 버튼

    /* JAVA Declare */
    double lat = 0.0, lon = 0.0;
    private GpsInfo gps;

    /* TEXTFILE */
    TextFileManager fileM_distance = new TextFileManager("Distance", getContext()); // 반경거리
    TextFileManager fileM_phone = new TextFileManager("EmergencyNum", getContext()); // 연락처
    TextFileManager fileM_latMap = new TextFileManager("latMap", getContext()); // 위도
    TextFileManager fileM_lonMap = new TextFileManager("lonMap", getContext()); // 경도
    TextFileManager fileM_locationCheck = new TextFileManager("LocationRgCheck", getContext()); // Registration : 위치등록했는지 체크

    /* Dialog's context declare */
    public Dialog_distance(Context context) {
        super(context);
    }

    /* Activity life cycle : onCreate() */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_distance);

        /* Init */
        editDistance = (EditText)findViewById(R.id.editDistance);
        btnCurrent = (Button) findViewById(R.id.btnCurrent);
        rgDistanceBtn = (Button) findViewById(R.id.rgDistanceBtn);
        txtNumber = (TextView)findViewById(R.id.txtNumber);
        btnNumberinfo = (ImageButton)findViewById(R.id.btnNumberinfo);
        editNumber = (EditText)findViewById(R.id.editNumber);

        /* Set onClick() */
        btnCurrent.setOnClickListener(this);
        rgDistanceBtn.setOnClickListener(this);
        btnNumberinfo.setOnClickListener(this);

        /* Add - into number */
        editNumber.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        editNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        /* Hide phone explanation */
        txtNumber.setVisibility(View.GONE);

        /* 수정할 때 값 불러오기 */
        if(fileM_locationCheck.load().equals("true")){
            /* 이전에 등록한 값 불러오기*/
            editDistance.setText(fileM_distance.load());
            editNumber.setText(fileM_phone.load());
            lat = Double.parseDouble(fileM_latMap.load());
            lon = Double.parseDouble(fileM_lonMap.load());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /* (1) Registration distance info */
            case R.id.rgDistanceBtn:
                if(lat == 0.0 && lon == 0.0)    Toast.makeText(getContext(), "반경 지점을 등록해주세요", Toast.LENGTH_SHORT).show();
                else{
                    /* Save info into TEXTFILE */
                    fileM_distance.save(editDistance.getText().toString());
                    fileM_phone.save(editNumber.getText().toString());
                    fileM_latMap.save(String.valueOf(lat));
                    fileM_lonMap.save(String.valueOf(lon));
                    fileM_locationCheck.save("true");
                /* Notice registration */
                    Toast.makeText(getContext(), "반경거리가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    dismiss(); // Dismiss dialog

                /* Restart ActivityLocation */
                    Intent intent = new Intent(getContext(), ActivityLocation.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    ((ActivityLocation) ActivityLocation.rContext).finish();
                    ((ActivityLocation) ActivityLocation.rContext).startActivity(intent);
                }
                break;

            /* (2) To explain the function 'PhoneNumber' */
            case R.id.btnNumberinfo:
                txtNumber.setVisibility(View.VISIBLE);
                break;

            /* (3) To get coordinate */
            case R.id.btnCurrent:
                /* Connect to GpsInfo */
                gps = new GpsInfo(getContext());
                if (gps.isGetLocation()) {
                    lat = gps.getLatitude();
                    lon = gps.getLongitude();
                    /* Check GPS connecting status */
                    if(lat == 0.0 && lon == 0.0)    Toast.makeText(getContext(), "현재 위치가 파악되지 않습니다\n" +
                            "버튼을 다시 눌러주세요", Toast.LENGTH_SHORT).show();
                    else   Toast.makeText(getContext(), "반경 지점이 현재 위치로 설정되었습니다", Toast.LENGTH_SHORT).show();
                } else { // When Gps off
                    gps.showSettingsAlert(); // Go to gps setting
                }
                break;
            default:
                break;
        }
    }
}
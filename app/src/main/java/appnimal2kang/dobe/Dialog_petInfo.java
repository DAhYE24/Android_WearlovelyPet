package appnimal2kang.dobe;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


/**
 * Created by dh93 on 2016-09-10.
 */
public class Dialog_petInfo extends Dialog {
    /* XML Declare*/
    Spinner spinPetSize; // Spinner : to show dog types
    EditText editPetName; // EditText : to get dog name
    Button registerBtn; // Button : to register dog info into app
    TextView btnBirth; // TextView : to get dog birth

    /* Init */
    Calendar calendar;
    DatePickerDialog datepicker;
    int nyear, nmonth, nday, year, month, day;
    String sBirth;
    boolean birthValidate = false, checkDate = false;

    /* TEXTFILE */
    TextFileManager fileM_rgCheck = new TextFileManager("AppRgCheck", getContext()); // Registration : to check registration
    TextFileManager fileM_name = new TextFileManager("DogName", getContext()); // Registration : dog name
    TextFileManager fileM_type = new TextFileManager("DogType", getContext()); // Registration : dog type
    TextFileManager fileM_birth = new TextFileManager("DogBirth", getContext()); // Registration : dog birthday
    TextFileManager fileM_properWalking = new TextFileManager("ProperWalking", getContext()); // Registration : dog proper Walking Time

    /* Essential : override Context */
    public Dialog_petInfo(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_register);

        /* Init*/
        editPetName = (EditText)findViewById(R.id.editPetName);
        btnBirth = (TextView)findViewById(R.id.btnBirth);
        spinPetSize = (Spinner)findViewById(R.id.spinPetSize);
        registerBtn = (Button)findViewById(R.id.registerBtn);


        /* Dog name setting */
        int purpleColor = Color.parseColor("#563791");
        editPetName.getBackground().setColorFilter(purpleColor, PorterDuff.Mode.SRC_IN);

        /* Dog type setting */
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(), R.array.petType, android.R.layout.simple_spinner_dropdown_item);
        spinPetSize.setAdapter(adapter);

        if(fileM_rgCheck.load().equals("true")){
            String tempName = fileM_name.load();
            String tempType = fileM_type.load();
            String tempBirth = fileM_birth.load();
            birthValidate=true;
            editPetName.setText(tempName);
            spinPetSize.setSelection(adapter.getPosition(tempType));
            btnBirth.setText("  " + tempBirth);
        }

        /* Dog birth setting */
        btnBirth = (TextView) findViewById(R.id.btnBirth);
        btnBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datepickerDialog();
            }
        });

        /* DatePickerDialog setting*/
        calendar = Calendar.getInstance();
        nyear = calendar.get(Calendar.YEAR);
        nmonth = calendar.get(Calendar.MONTH);
        nday = calendar.get(Calendar.DAY_OF_MONTH);
        datepicker = new DatePickerDialog(getContext(), listener, nyear, nmonth, nday);

        /* Registration Button */
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* DogName, DogBirth Validation */
                String checkName = editPetName.getText().toString();
                if(checkName.getBytes().length <= 0)
                    Toast.makeText(getContext(), "반려견 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                else{
                    if(birthValidate)
                        saveDogInfo();
                    else
                        Toast.makeText(getContext(), "반려견 생일을 설정하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /* DatePickerDialog */
    public void datepickerDialog(){
        datepicker.show();
    }

    /* DatePickerDialog Listener */
    private android.app.DatePickerDialog.OnDateSetListener listener = new android.app.DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int yearOf, int monthOfyear, int dayOfyear) {

            year = yearOf;
            month = monthOfyear;
            day = dayOfyear;

            checkBirth();

            if(checkDate){

                sBirth = String.format("%04d-%02d-%02d", yearOf, monthOfyear+1, dayOfyear);

                btnBirth.setText("  " + sBirth);
                birthValidate = true;
            }
            else
                Toast.makeText(getContext(), "현재 날짜보다 이전의 날짜를 선택하세요.", Toast.LENGTH_SHORT).show();
        }
    };

    /* Birth Date Validation (after now) */
    public void checkBirth(){
        if(nyear < year)
            checkDate = false;
        else if(nyear == year){
            if(nmonth > month)
                checkDate = true;
            else if(nmonth == month){
                if(nday > day)
                    checkDate = true;
                else if(nday == day)
                    checkDate = true;
                else
                    checkDate = false;
            }
            else
                checkDate = false;
        }
        else
            checkDate = true;
    }

    private void saveDogInfo() {
        /* Save dog's info into TEXTFILE */
        fileM_name.save(editPetName.getText().toString());
        fileM_type.save(spinPetSize.getSelectedItem().toString());
        fileM_birth.save(sBirth);

        String[] typeChk = getContext().getResources().getStringArray(R.array.petType); // Array : pet type
        String[] walkTime = getContext().getResources().getStringArray(R.array.petWalking); // Array : pet proper walking time

        /* To get proper walking time */
        for(int i = 0; i < typeChk.length; i++){
            if(fileM_type.load().equals(typeChk[i])) {
                fileM_properWalking.save(walkTime[i]);
            }
        }

        /* Restart StartActivity */
        dismiss(); // End dialog
        Toast.makeText(getContext(), "반려견 정보가 등록되었습니다", Toast.LENGTH_SHORT).show();

        /* 처음 등록과 수정의 경우 */
        if(fileM_rgCheck.load().equals("true")) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ((MainActivity) MainActivity.maContext).finish();
            ((MainActivity) MainActivity.maContext).startActivity(intent);
        }else {
            Intent intent = new Intent(getContext(), StartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ((StartActivity) StartActivity.mContext).finish();
            ((StartActivity) StartActivity.mContext).startActivity(intent);
            fileM_rgCheck.save("true"); // To check registration : when it's value "true", it means "dog's info ia already registered"
        }
    }
}

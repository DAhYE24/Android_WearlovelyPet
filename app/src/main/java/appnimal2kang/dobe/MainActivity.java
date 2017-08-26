package appnimal2kang.dobe;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private String picturePath, DogName, DogBirth, DogAge;
    private static int RESULT_LOAD_IMAGE = 1;
    public static Context maContext;

    /* Dog Age (bBirth : 생일인지 아닌지 체크)*/
    Calendar calendar;
    int year, month, day;
    boolean bBirth = false;

    /* XML */
    TextView infoTxt;
    ImageButton btnCamera, modifyImg;
    ImageView cameraImg;
    Button mapBtn, managementBtn, walkingBtn, careBtn;

    Dialog_petInfo registerDialog;

    TextFileManager fileM_name = new TextFileManager("DogName", ((StartActivity)StartActivity.mContext));
    TextFileManager fileM_birth = new TextFileManager("DogBirth", ((StartActivity)StartActivity.mContext));
    TextFileManager fileM_picPath = new TextFileManager("PicturePath", ((StartActivity)StartActivity.mContext));
    TextFileManager fileM_age = new TextFileManager("DogAge", this);
    TextFileManager fileM_chYear = new TextFileManager("CheckAYear", this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamera = (ImageButton)findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(this);

        infoTxt = (TextView)findViewById(R.id.infoTxt);
        maContext = this;

        /* Load File */
        DogName = fileM_name.load();
        DogBirth = fileM_birth.load();
        picturePath = fileM_picPath.load();

        /* Load now Date */
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);

        /* Set Dog Age */
        loadAge(DogBirth);
        infoTxt.setText(Html.fromHtml("<b>" + DogName + "/" + DogAge + "</b>"));

        walkingBtn = (Button) findViewById(R.id.walkingBtn);
        walkingBtn.setOnClickListener(this);

        careBtn = (Button) findViewById(R.id.careBtn);
        careBtn.setOnClickListener(this);

        mapBtn = (Button)findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(this);

        managementBtn = (Button) findViewById(R.id.managementBtn);
        managementBtn.setOnClickListener(this);

        modifyImg = (ImageButton)findViewById(R.id.modifyImg);
        modifyImg.setOnClickListener(this);

        registerDialog = new Dialog_petInfo(MainActivity.this); // Declare registration dialog
        registerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // To hide Title layer on dialog

        cameraImg = (ImageView)findViewById(R.id.cameraImg);

        /* Init Image */
        if(picturePath!="") {
            int dstWidth = 200;
            int dstHeight = 200;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap src = BitmapFactory.decodeFile(picturePath, options);
            Bitmap resized = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, true);

            cameraImg.setImageBitmap(resized);
        }
    }

    /* Calculate Dog Age */
    public void loadAge(String sAge){
        int dyear, dmonth, dday, age;

        dyear = Integer.parseInt(sAge.substring(0, 4));
        dmonth = Integer.parseInt(sAge.substring(5, 7));
        dday = Integer.parseInt(sAge.substring(8, 10));

        if(year - dyear > 1)
        {
            age = year - dyear;

            if (month > dmonth) {
                DogAge = Integer.toString(age);
            } else if (month == dmonth) {
                if (day > dday) {
                    DogAge = Integer.toString(age);
                } else if (day < dday) {
                    DogAge = Integer.toString(age - 1);
                } else {
                    DogAge = Integer.toString(age);
                    bBirth = true;
                }
            } else
                DogAge = Integer.toString(age - 1);

            fileM_age.save(DogAge);
            DogAge = DogAge + "살";
            fileM_chYear.save("F");

        }
        else if(year - dyear == 1){

            if (month > dmonth) {
                DogAge = "1살";
                fileM_age.save("1");
                fileM_chYear.save("F");
            } else if (month == dmonth) {
                if (day > dday) {
                    DogAge =  "1살";
                    fileM_age.save("1");
                    fileM_chYear.save("F");
                } else if (day < dday) {
                    DogAge = "11개월";
                    fileM_age.save("11");
                    fileM_chYear.save("T");
                } else {
                    DogAge = "1살";
                    bBirth = true;
                    fileM_age.save("1");
                    fileM_chYear.save("F");
                }
            } else{
                age = dmonth - month;
                if (day > dday) {
                    DogAge =  Integer.toString(12-age);
                } else if (day < dday) {
                    DogAge = Integer.toString(12-age-1);
                } else {
                    DogAge = Integer.toString(12-age);
                }
                fileM_age.save(DogAge);
                DogAge = DogAge + "개월";
                fileM_chYear.save("T");
            }


        }
        else //Under 1year
        {
            age = month - dmonth;

            if(month > dmonth){
                DogAge = Integer.toString(age);
            }
            else if(month == dmonth){
                if(day > dday){
                    DogAge = Integer.toString(age);
                }
                else if(day == dday){
                    DogAge = Integer.toString(age);
                    bBirth = true;
                    /* ♥ 생일인 경우에 디자인 변경하도록 설정 */
                }
            }

            DogAge = DogAge + "개월";
            fileM_age.save(DogAge);
            fileM_chYear.save("T");
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnCamera:
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
                break;
            case R.id.modifyImg:
                registerDialog.show();
                break;
            case R.id.careBtn:
                Intent ci = new Intent(MainActivity.this, CareActivity.class);
                startActivity(ci);
                break;
            case R.id.mapBtn:
                Intent mi = new Intent(MainActivity.this, ActivityLocation.class);
                startActivity(mi);
                break;
            case R.id.managementBtn:
                Intent managementIntent = new Intent(MainActivity.this, ManagementActivity.class);
                startActivity(managementIntent);
                break;
            case R.id.walkingBtn:
                Intent wi = new Intent(MainActivity.this, Walking.class);
                startActivity(wi);
                break;
            default: break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data){
            Uri imageUri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(imageUri,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

            //파일에 경로저장
            fileM_picPath.save(picturePath);

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);

//            cameraImg = (ImageView)findViewById(R.id.cameraImg);
//            cameraImg.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }
}

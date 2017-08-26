package appnimal2kang.dobe;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/* The way to save data in internal storage, replacing Inner DB */
public class TextFileManager {
    /* Declare & Init  */
    String FILE_NAME;
    Context mContext = null;
    int mSTATE;

    /* TextFileManager */
    public TextFileManager(String STATE, Context _context){
        mContext = _context;
        FILE_NAME = STATE;
    }

    /* DATA SAVE */
    public void save(String strData){
        if(strData == null || strData.isEmpty()== true){
            return;
        }

        FileOutputStream fosMemo = null;

        try{
            fosMemo = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fosMemo.write(strData.getBytes());
            fosMemo.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /* DATA LOAD */
    public String load(){
        try{
            FileInputStream fisMemo = mContext.openFileInput(FILE_NAME);
            byte[] memoData = new byte[fisMemo.available()];

            while(fisMemo.read(memoData) != -1){}

            fisMemo.close();
            return new String(memoData);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return "";
    }

    /* DATA DELETE */
    public void delete(){
        mContext.deleteFile(FILE_NAME);
    }
}

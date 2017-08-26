package appnimal2kang.dobe;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dh93 on 2016-10-20.
 */
public class SimpleFragment extends Fragment {
    /* 로그 테스트 */
    public static String TAG = "WLP_TEST";

    public SimpleFragment() {
    }

    JSONArray record = null;
    String [] recordTime = null;
    String [] recordDate = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected BarData generateBarData(int dataSets, long range, int count) {

        FragmentWalkRecord fwr = null;
        String myJSON = fwr.getMyJSON();

        try{
            JSONObject jsonObj = new JSONObject(myJSON);
            record = jsonObj.getJSONArray("results");

            /* 배열 초기화 */
            recordTime = new String[record.length()];

            for(int i=0;i<record.length();i++) {
                JSONObject jo = record.getJSONObject(i);
                //배열로 받아옴
                recordTime[i] = jo.getString("time");
                recordTime[i] = String.valueOf((int)(Integer.parseInt(recordTime[i]) / 1000) / 60);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<IBarDataSet> sets = new ArrayList<IBarDataSet>();

        for(int i = 0; i < dataSets; i++) {

            ArrayList<BarEntry> entries = new ArrayList<BarEntry>();

            for(int j = 0; j < count; j++) {
                entries.add(new BarEntry(count-j-1, Float.parseFloat(recordTime[j])));
            }

            BarDataSet ds = new BarDataSet(entries, "산책시간(단위:분)");
            ds.setColors(Color.rgb(86, 55, 145));
            sets.add(ds);
        }

        BarData d = new BarData(sets);
        return d;
    }
}

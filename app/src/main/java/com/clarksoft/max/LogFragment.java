package com.clarksoft.max;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.StackedValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogFragment extends DemoBase implements OnChartValueSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private BarChart chart;

    private String userUUID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Button log_btn_share;

    private Map<String, String> month_number = new HashMap<>();

    TextView log_date_view;
    String max_date, min_date;

    ImageView log_calendar_icon;

    public LogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogFragment newInstance(String param1, String param2) {
        LogFragment fragment = new LogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        month_number.put("Jan", "01");
        month_number.put("Feb", "02");
        month_number.put("Mar", "03");
        month_number.put("Apr", "04");
        month_number.put("May", "05");
        month_number.put("Jun", "06");
        month_number.put("Jul", "07");
        month_number.put("Aug", "08");
        month_number.put("Sep", "09");
        month_number.put("Oct", "10");
        month_number.put("Nov", "11");
        month_number.put("Dec", "12");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userUUID = user.getUid();
        }

        log_btn_share = view.findViewById(R.id.log_btn_share);
        log_date_view = view.findViewById(R.id.log_date_view);
        log_calendar_icon = view.findViewById(R.id.log_calendar_icon);

        chart = view.findViewById(R.id.chart1);
        chart.setOnChartValueSelectedListener(this);

        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(40);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        chart.setDrawValueAboveBar(false);
        chart.setHighlightFullBarEnabled(false);

        // change the position of the y-labels
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setValueFormatter(new MyValueFormatter(" min"));
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        chart.getAxisRight().setEnabled(false);

        XAxis xLabels = chart.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);

        // chart.setDrawXLabels(false);
        // chart.setDrawYLabels(false);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);

        // chart.setDrawLegend(false);

        fetchSessionData(Integer.MIN_VALUE, Integer.MAX_VALUE);

        log_btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share();
            }
        });

        log_date_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date_picker();
            }
        });
        log_calendar_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date_picker();
            }
        });

        return view;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.bar, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.viewGithub: {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse("https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/StackedBarActivity.java"));
//                startActivity(i);
//                break;
//            }
//            case R.id.actionToggleValues: {
//                List<IBarDataSet> sets = chart.getData()
//                        .getDataSets();
//
//                for (IBarDataSet iSet : sets) {
//
//                    BarDataSet set = (BarDataSet) iSet;
//                    set.setDrawValues(!set.isDrawValuesEnabled());
//                }
//
//                chart.invalidate();
//                break;
//            }
//            case R.id.actionToggleIcons: {
//                List<IBarDataSet> sets = chart.getData()
//                        .getDataSets();
//
//                for (IBarDataSet iSet : sets) {
//
//                    BarDataSet set = (BarDataSet) iSet;
//                    set.setDrawIcons(!set.isDrawIconsEnabled());
//                }
//
//                chart.invalidate();
//                break;
//            }
//            case R.id.actionToggleHighlight: {
//                if (chart.getData() != null) {
//                    chart.getData().setHighlightEnabled(!chart.getData().isHighlightEnabled());
//                    chart.invalidate();
//                }
//                break;
//            }
//            case R.id.actionTogglePinch: {
//                if (chart.isPinchZoomEnabled())
//                    chart.setPinchZoom(false);
//                else
//                    chart.setPinchZoom(true);
//
//                chart.invalidate();
//                break;
//            }
//            case R.id.actionToggleAutoScaleMinMax: {
//                chart.setAutoScaleMinMaxEnabled(!chart.isAutoScaleMinMaxEnabled());
//                chart.notifyDataSetChanged();
//                break;
//            }
//            case R.id.actionToggleBarBorders: {
//                for (IBarDataSet set : chart.getData().getDataSets())
//                    ((BarDataSet) set).setBarBorderWidth(set.getBarBorderWidth() == 1.f ? 0.f : 1.f);
//
//                chart.invalidate();
//                break;
//            }
//            case R.id.animateX: {
//                chart.animateX(2000);
//                break;
//            }
//            case R.id.animateY: {
//                chart.animateY(2000);
//                break;
//            }
//            case R.id.animateXY: {
//
//                chart.animateXY(2000, 2000);
//                break;
//            }
//            case R.id.actionSave: {
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    saveToGallery();
//                } else {
//                    requestStoragePermission(chart);
//                }
//                break;
//            }
//        }
//        return true;
//    }

    @Override
    protected void saveToGallery() {
        saveToGallery(chart, "StackedBarActivity");
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        BarEntry entry = (BarEntry) e;

        if (entry.getYVals() != null)
            Log.i("VAL SELECTED", "Value: " + entry.getYVals()[h.getStackIndex()]);
        else
            Log.i("VAL SELECTED", "Value: " + entry.getY());
    }

    @Override
    public void onNothingSelected() {
    }

    private int[] getColors() {

        // have as many colors as stack-values per entry
        int[] colors = new int[3];
        int[] temp = new int[3];

        System.arraycopy(ColorTemplate.MATERIAL_COLORS, 0, colors, 0, 3);

        temp[0] = colors[1];
        temp[1] = colors[0];
        temp[2] = colors[2];

        return temp;
    }

    public void set_data(ArrayList<BarEntry> values) {

        BarDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(values, "");
            set1.setDrawIcons(false);
            set1.setColors(getColors());
            set1.setStackLabels(new String[]{"Below HR", "In HR", "Above HR"});

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueFormatter(new StackedValueFormatter(false, "", 1));
            data.setValueTextColor(Color.WHITE);

            chart.setData(data);
        }

        chart.setFitBars(true);
        chart.invalidate();
    }

    private void fetchSessionData(Integer start_date, Integer end_date) {

        ArrayList<BarEntry> values = new ArrayList<>();

        Query userDataQuery = db.collection("session")
                .whereEqualTo("uid", userUUID)
                .orderBy("date")
                .startAt(start_date)
                .endAt(end_date);
        userDataQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot session_data = task.getResult();
                    int i = 1;
                    for (QueryDocumentSnapshot document : session_data) {
                        float db_below_hr = Integer.parseInt(document.get("below_hr").toString()) / 60.0f;
                        float db_in_hr = Integer.parseInt(document.get("in_hr").toString()) / 60.0f;
                        float db_above_hr = Integer.parseInt(document.get("above_hr").toString()) / 60.0f;
                        String date = document.get("date").toString();

                        values.add(new BarEntry(
                                i,
                                new float[]{db_below_hr, db_in_hr, db_above_hr},
                                getResources().getDrawable(R.drawable.star)));

                        if (i == 1)
                            min_date = date;
                        max_date = date;

                        i++;
                    }

                    set_data(values);
                    min_date = min_date.substring(0, 4) + "/" + min_date.substring(4, 6) + "/" + min_date.substring(6, 8);
                    max_date = max_date.substring(0, 4) + "/" + max_date.substring(4, 6) + "/" + max_date.substring(6, 8);

                    if (start_date == Integer.MIN_VALUE) {
                        log_date_view.setText(min_date + " - " + max_date);
                    }


                } else {
                    Log.e("DB", "", task.getException());
                }
            }
        });
    }

    private void share() {
        Bitmap chart_bmp = chart.getChartBitmap();

        String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                chart_bmp, "Exercise Log", null);

        Uri uri = Uri.parse(path);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_TEXT, "Look at the progress I am making with the Max App!");
        getContext().startActivity(Intent.createChooser(share, "Share your progress"));
    }

    private void date_picker() {

        MaterialDatePicker.Builder<Pair<Long, Long>> builder =
                MaterialDatePicker.Builder.dateRangePicker();
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        builder.setCalendarConstraints(constraintsBuilder.build());
        MaterialDatePicker<?> picker = builder.build();
        picker.show(getActivity().getSupportFragmentManager(), picker.toString());

        picker.addOnPositiveButtonClickListener(
                selection -> {
                    String calendar_out = picker.getHeaderText();
                    String present_year = (new SimpleDateFormat("yyyy", Locale.CANADA).format((new Date())));
                    String end_date_str = extract_date(calendar_out.split("–")[1].trim(), present_year);
                    String start_date_str = extract_date(calendar_out.split("–")[0].trim(), end_date_str.substring(0, 4));

                    int start_date = Integer.parseInt(start_date_str);
                    int end_date = Integer.parseInt(end_date_str);

                    fetchSessionData(start_date, end_date);

                    start_date_str = start_date_str.substring(0, 4) + "/" + start_date_str.substring(4, 6) + "/" + start_date_str.substring(6, 8);
                    end_date_str = end_date_str.substring(0, 4) + "/" + end_date_str.substring(4, 6) + "/" + end_date_str.substring(6, 8);
                    log_date_view.setText(start_date_str + " - " + end_date_str);
                });
    }

    private String extract_date(String date, String cur_year) {

        String year;
        try {
            year = date.split(",")[1].trim();
            date = date.split(",")[0].trim();
        } catch (Exception e) {
            year = cur_year;
        }

        String month = month_number.get(date.split(" ")[0].trim());
        String day = (date.split(" ")[1].trim().length() == 2) ? date.split(" ")[1].trim() : "0" + date.split(" ")[1].trim();
        String date_out = year + month + day;

        return date_out;
    }
}
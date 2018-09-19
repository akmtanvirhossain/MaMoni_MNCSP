package org.icddrb.standard;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Common.Connection;
import Common.Global;
import Utility.MySharedPreferences;
import form_design.module_data_DataModel;
import form_design.module_variable_DataModel;

import static android.view.View.generateViewId;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

public class datafrommaster1 extends AppCompatActivity {
    Bundle IDbundle;
    static String MODULEID = "";
    static String DATAID = "";
    static String VARIABLENAME = "";
    static String DEVICEID = "";
    static String ENTRYUSER = "";

    private List<module_variable_DataModel> variableList = new ArrayList<>();
    private List<module_variable_DataModel> variableList2 = new ArrayList<>();

    public ArrayList<View> toalview = new ArrayList<>();

    Global g;
    Connection C;
    MySharedPreferences sp;
    LinearLayout start;

    public void prepareVariableListData(String Module_Id, String Data_Id) {

        String SQL = "";

        //Populate data for update
        SQL = "Insert into module_data(module_id, variable_name, data_id, variable_data, data_desc, status, entry_date, first_entry_time, final_entry_time, DeviceId, EntryUser, Upload, modifyDate)\n" +
                " select module_id, variable_name, '" + Data_Id + "' data_id, '' variable_data, '' data_desc, 1 status, null entry_date, null first_entry_time, null final_entry_time, '" + DEVICEID + "' DeviceId, '" + ENTRYUSER + "' EntryUser, 2 Upload, '" + Global.DateTimeNowYMDHMS() + "' modifyDate\n" +
                " from module_variable v where module_id='" + Module_Id + "' and not exists(select * from module_data where module_id=v.module_id and variable_name=v.variable_name and " +
                " data_id='" + Data_Id + "')\n" +
                " order by variable_seq";

        String resp = C.SaveData(SQL);
        if (resp.length() > 0) {
            Connection.MessageBox(this, resp.toString());
            return;
        }

        //Populate data for form generate
        SQL = "Select v.module_id,v. variable_name,v. variable_desc,v. variable_seq,v. control_type,\n" +
                " v. variable_option,v. variable_length,v. data_type,v. skip_rule,v. color,v.active,v. variable_image,v. variable_audio,v. variable_video,\n" +
                " ifnull(d.variable_data,'')variable_data,ifnull(d.data_desc,'')data_desc, ifnull(d.data_id,'')data_id, ifnull(d.status,'')status\n" +
                " from module_variable v\n" +
                " left outer join module_data d on v.module_id=d.module_id and v.variable_name=d.variable_name \n" +
                " and d.data_id='" + Data_Id + "'\n" +
                " where v.module_id='" + Module_Id + "'\n" +
                " order by v.variable_seq";


        module_variable_DataModel d = new module_variable_DataModel();
        List<module_variable_DataModel> data = d.SelectAll_WithVariableData(this, SQL);

        variableList.clear();

        variableList.addAll(data);

    }

    public void saveData(module_variable_DataModel varlist, String data) {
        module_data_DataModel module_data_dataModel = new module_data_DataModel();
        module_data_dataModel.setmodule_id(MODULEID);
        module_data_dataModel.setvariable_name(varlist.getvariable_name());
        module_data_dataModel.setdata_id(DATAID);
        module_data_dataModel.setvariable_data(data);
        module_data_dataModel.setdata_desc(varlist.getdata_desc());
        module_data_dataModel.setstatus(varlist.getstatus());
        module_data_dataModel.setentry_date(Global.DateNowYMD());
        module_data_dataModel.setfirst_entry_time(millsToDateFormat(new Date().getTime()));
        module_data_dataModel.setfinal_entry_time(millsToDateFormat(new Date().getTime()));

        module_data_dataModel.setDeviceID(DEVICEID);
        module_data_dataModel.setEntryUser(ENTRYUSER);

        module_data_dataModel.setmodifyDate(Global.DateTimeNowYMDHMS());

        module_data_dataModel.SaveUpdateData(datafrommaster1.this);

    }

    public String millsToDateFormat(long mills) {

        Date date = new Date(mills);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String dateFormatted = formatter.format(date);
        return dateFormatted; //note that it will give you the time in GMT+0
    }


    String[] skip_rule;
    private List<MyMapping> letsmapskip_rule = new ArrayList<>();

    public List<module_variable_DataModel> sortmodel(List<module_variable_DataModel> models) {

        List<module_variable_DataModel> variableListagain = new ArrayList<>();

        for (int i = 0; i < models.size(); i++) {
            module_variable_DataModel varlist = models.get(i);

            if (!varlist.getskip_rule().toString().equals("")) {

                Log.d("My value ===", varlist.getvariable_name()+" == " +
                        ""+varlist.getskip_rule().toString() + "");
                if (varlist.getskip_rule().trim().contains(",")) {
                    skip_rule = varlist.getskip_rule().split(",");
                } else {
                    skip_rule[0] = varlist.getskip_rule().trim();
                }
                for (int j = 0; j < skip_rule.length; j++) {
                    String skip_variable_list[] = skip_rule[j].split(":");

                    if (skip_variable_list[1].contains("-")) {
                        String[] skipfrom_to = skip_variable_list[1].split("-");
                        boolean run = false;

                        for (int k = 0; k < models.size(); k++) {

                            if (skipfrom_to[0].toString().equals(variableList.get(k).getvariable_name().toString())) {
                                run = true;
                            }
                            if (run) {
                                letsmapskip_rule.add(new MyMapping(
                                        models.get(i).getvariable_name(),
                                        skip_variable_list[0] + "",
                                        variableList.get(k).getvariable_name().toString()
                                ));
                            }

                            if (skipfrom_to[1].equals(variableList.get(k).getvariable_name())) {
                                run = false;

                            }
                        }
                    } else {
                        for (int k = 0; k < models.size(); k++) {
                            if (skip_variable_list[1].equals(variableList.get(k).getvariable_name().toString())) {
                                letsmapskip_rule.add(new MyMapping(
                                        models.get(i).getvariable_name(),
                                        skip_variable_list[0] + "", variableList.get(k).getvariable_name().toString()
                                ));

                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < models.size(); i++) {
            boolean insert = true;
            for (int j = 0; j < letsmapskip_rule.size(); j++) {
                if (models.get(i).getvariable_name().toString().equals(letsmapskip_rule.get(j).getRemovalvalue().toString())) {
                    models.remove(i);
                }
            }
        }
        return models;
    }


    @SuppressLint({"ResourceType", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datafrommaster);
        g = Global.getInstance();
        C = new Connection(this);
        DEVICEID = sp.getValue(this, "deviceid");
        ENTRYUSER = sp.getValue(this, "userid");

        IDbundle = getIntent().getExtras();
        MODULEID = IDbundle.getString("moduleid");
        DATAID = IDbundle.getString("dataid");
        prepareVariableListData(MODULEID, DATAID);

        //-----------------------------------------------------------------------------DESIGN FOR LAYOUT
        //-----------------------------------------------------------------PRACICE TO PULL OFF
        start = findViewById(R.id.start);

        List<module_variable_DataModel> listCopy = new ArrayList<>(variableList);


        variableList2 = sortmodel(listCopy);

        //------------------------------------
        for (int i = 0; i < variableList.size(); i++) {
            boolean insert = true;
            for (int j = 0; j < letsmapskip_rule.size(); j++) {
                if (variableList.get(i).getvariable_name().toString().equals(letsmapskip_rule.get(j).getRemovalvalue().toString())) {
                    insert = false;
                }
            }
            if (insert) {

                View cardchild = generate(variableList.get(i), i );
//
                start.addView(cardchild);
//                Toast.makeText(this, i+"MYMY", Toast.LENGTH_SHORT).show();
            }
        }
        //------------------------------------
//        for (int i = 0; i < variableList2.size(); i++) {
//            View cardchild = generate(variableList2.get(i), i + 1);
//
//            start.addView(cardchild);
//        }
        for (int i = 0; i < variableList.size(); i++) {
            if (variableList.get(i).getstatus().toString().equals("2")) {
//                Toast.makeText(this, i+"", Toast.LENGTH_SHORT).show();
                View skipchild = generate(variableList.get(i), i);
                start.addView(skipchild, i );
            }
        }

    }

    public View generate(module_variable_DataModel varlist, int position) {
        CardView crd = new CardView(this);
        crd.setTag(varlist.getvariable_name());

        LinearLayout.LayoutParams crdparam = new LinearLayout.LayoutParams(0, 0);
        crdparam.width = LinearLayout.LayoutParams.MATCH_PARENT;
        crdparam.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        crdparam.setMargins(10, 10, 10, 10);
        crd.setLayoutParams(crdparam);

        LinearLayout linearLayout_87 = new LinearLayout(this);
        linearLayout_87.setTag(varlist.getvariable_name());

        linearLayout_87.setOrientation(VERTICAL);
        LinearLayout.LayoutParams layout_467 = new LinearLayout.LayoutParams(0, 0);
        layout_467.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layout_467.height = LinearLayout.LayoutParams.MATCH_PARENT;
        linearLayout_87.setLayoutParams(layout_467);

        LinearLayout linearLayout_356 = new LinearLayout(this);
        linearLayout_356.setTag(varlist.getvariable_name());

        linearLayout_356.setOrientation(VERTICAL);
        LinearLayout.LayoutParams layout_319 = new LinearLayout.LayoutParams(0, 0);
        layout_319.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layout_319.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        linearLayout_356.setLayoutParams(layout_319);

        TextView textView_978 = new TextView(this);
        textView_978.setTag(varlist.getvariable_name());

        textView_978.setText((position+1) + " . " + varlist.getvariable_desc());
        LinearLayout.LayoutParams layout_115 = new LinearLayout.LayoutParams(0, 0);
        layout_115.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        layout_115.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        textView_978.setLayoutParams(layout_115);
        linearLayout_356.addView(textView_978);
        linearLayout_87.addView(linearLayout_356);

        LinearLayout linearLayout_519 = new LinearLayout(this);
        linearLayout_519.setTag(varlist.getvariable_name());

        linearLayout_519.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layout_611 = new LinearLayout.LayoutParams(0, 0);
        layout_611.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layout_611.height = LinearLayout.LayoutParams.MATCH_PARENT;
        layout_611.weight = 2;
        linearLayout_519.setLayoutParams(layout_611);

        LinearLayout linearLayout_223 = new LinearLayout(this);
        linearLayout_223.setTag(varlist.getvariable_name());

        LinearLayout.LayoutParams layout_120 = new LinearLayout.LayoutParams(0, 0);
        layout_120.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layout_120.height = LinearLayout.LayoutParams.MATCH_PARENT;
        layout_120.weight = 1;
        linearLayout_223.setLayoutParams(layout_120);

        if (varlist.getcontrol_type().equals("1")) {

            linearLayout_223.addView(makeedittext(varlist, position));//------------------------------------
        } else if (varlist.getcontrol_type().equals("2")) {

            linearLayout_223.addView(makeradiogroup(varlist, position));//------------------------------------
        } else if (varlist.getcontrol_type().equals("3")) {

            linearLayout_223.addView(makespinner(varlist, position));//------------------------------------
        } else if (varlist.getcontrol_type().equals("4")) {

            linearLayout_223.addView(makececkbox(varlist, position));//------------------------------------
        } else if (varlist.getcontrol_type().equals("5")) {

//            linearLayout_223.addView(makedate(varlist, position));//------------------------------------
        } else if (varlist.getcontrol_type().equals("6")) {

//            linearLayout_223.addView(maketime(varlist, position));//------------------------------------
        } else if (varlist.getcontrol_type().equals("7")) {
//                relativeLayout_323.addView(mak());
        }

//        linearLayout_223.addView(radioGroup_684);
        linearLayout_519.addView(linearLayout_223);

        LinearLayout linearLayout_582 = new LinearLayout(this);
        linearLayout_582.setTag(varlist.getvariable_name());

        linearLayout_582.setOrientation(VERTICAL);
        LinearLayout.LayoutParams layout_511 = new LinearLayout.LayoutParams(0, 0);
        layout_511.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layout_511.height = LinearLayout.LayoutParams.MATCH_PARENT;
        layout_511.weight = 1;
        linearLayout_582.setLayoutParams(layout_511);

        LinearLayout linearLayout_921 = new LinearLayout(this);
        linearLayout_921.setTag(varlist.getvariable_name());

        LinearLayout.LayoutParams layout_721 = new LinearLayout.LayoutParams(0, 0);
        layout_721.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layout_721.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        layout_721.weight = 1;
        linearLayout_921.setLayoutParams(layout_721);

        ImageView img = new ImageView(this);
        img.setImageResource(R.drawable.logo_icddrb);
        LinearLayout.LayoutParams layout_777 = new LinearLayout.LayoutParams(0, 0);
        layout_777.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layout_777.height = LinearLayout.LayoutParams.MATCH_PARENT;
        img.setLayoutParams(layout_777);
        linearLayout_921.addView(img);


        //IMAGE
        linearLayout_582.addView(linearLayout_921);


        LinearLayout linearLayout_75 = new LinearLayout(this);
        linearLayout_75.setTag(varlist.getvariable_name());

        LinearLayout.LayoutParams layout_490 = new LinearLayout.LayoutParams(0, 0);
        layout_490.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layout_490.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        layout_490.weight = 1;
        linearLayout_75.setLayoutParams(layout_490);
        //--------------VIDEO

        linearLayout_582.addView(linearLayout_75);
        linearLayout_519.addView(linearLayout_582);
        linearLayout_87.addView(linearLayout_519);

        crd.addView(linearLayout_87);
        return crd;

    }

    //-----------------ALL VIEW CONTROLLER
    //-------------------------------------------------VIEW COTROLLER
    public View makespinner(final module_variable_DataModel varlist, int position) {

        String option_list[] = varlist.getvariable_option().split(",");

        final List<String> spinnerArray = new ArrayList<String>();

        for (int i = 0; i < option_list.length; i++) {
            spinnerArray.add(option_list[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);


        Spinner spn = new Spinner(this);
        LinearLayout.LayoutParams layout_256 = new LinearLayout.LayoutParams(0, 0);
        layout_256.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        layout_256.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        spn.setId(generateViewId());
        spn.setAdapter(adapter);
        spn.setLayoutParams(layout_256);

//        int spinnerPosition = adapter.getPosition(varlist.getvariable_data());
//        spn.setSelection(spinnerPosition);

        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
//                saveData(varlist, spinnerArray.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        return spn;
    }

    public View makedate(module_variable_DataModel varlist, int position) {

        TextView tvdpicker = new TextView(this);
        LinearLayout.LayoutParams layout_256 = new LinearLayout.LayoutParams(0, 0);
        layout_256.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        layout_256.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        tvdpicker.setId(generateViewId());
        tvdpicker.setLayoutParams(layout_256);


        tvdpicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        return tvdpicker;
    }

    public View maketime(module_variable_DataModel varlist, int position) {
        TimePicker dpicker = new TimePicker(this);
        LinearLayout.LayoutParams layout_256 = new LinearLayout.LayoutParams(0, 0);
        layout_256.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        layout_256.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        dpicker.setId(generateViewId());
        dpicker.setLayoutParams(layout_256);

        return dpicker;
    }

    public View makeedittext(final module_variable_DataModel varlist, final int position) {

        final EditText editText_384 = new EditText(this);

        editText_384.setText(varlist.getvariable_data().toString());

        LinearLayout.LayoutParams layout_192 = new LinearLayout.LayoutParams(0, 0);
        layout_192.width = 150;
        layout_192.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        editText_384.setId(generateViewId());
        editText_384.setLayoutParams(layout_192);

        editText_384.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                skip_rules(varlist, position, editText_384.getText().toString() + "");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                saveData(varlist, editText_384.getText().toString());
            }
        });
//        skip_rules(varlist, position, varlist.getvariable_data().toString() + "");
        return editText_384;
    }

    private List<View> viewlist = new ArrayList<>();

    public View makeradiogroup(final module_variable_DataModel varlist, final int postion) {

        String option_list[] = varlist.getvariable_option().split(",");

        final RadioGroup radioGroup_441 = new RadioGroup(this);
        LinearLayout.LayoutParams layout_256 = new LinearLayout.LayoutParams(0, 0);
        layout_256.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        layout_256.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        radioGroup_441.setId(generateViewId());
        radioGroup_441.setLayoutParams(layout_256);

        RadioButton rb;
//        View str = generate(varlist,postion);
//        start.addView(str);
        for (int j = 0; j < option_list.length; j++) {

            final RadioButton[] radbutton = {new RadioButton(this)};
            radbutton[0].setText(option_list[j].trim());
            radbutton[0].setId(generateViewId());
            radbutton[0].setTag("rad" + j);
            radioGroup_441.addView(radbutton[0]);
            if (option_list[j].trim().toString().equals(varlist.getvariable_data())) {

                RadioButton btn = (RadioButton) radioGroup_441.getChildAt(j);
                btn.setChecked(true);
            }
        }

        radioGroup_441.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                int selectedId = radioGroup_441.getCheckedRadioButtonId();
                RadioButton rdobtn = (RadioButton) findViewById(selectedId);
                int idx = radioGroup_441.indexOfChild(rdobtn);
                skip_rules(varlist, postion, (idx + 1) + "");
//                String skip_chheck = (idx + 1) + "";
                saveData(varlist, rdobtn.getText().toString());

            }
        });
        return radioGroup_441;
    }

    public View makeimageview(module_variable_DataModel varlist, int position) {
        ImageView img_445 = new ImageView(this);
        LinearLayout.LayoutParams layout_188 = new LinearLayout.LayoutParams(0, 0);
        layout_188.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        layout_188.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        img_445.setImageResource(R.drawable.logo_icddrb);
        img_445.setLayoutParams(layout_188);
        img_445.setId(generateViewId());

        img_445.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

        return img_445;
    }

    public View makececkbox(final module_variable_DataModel varlist, final int position) {
        final CheckBox checkBox_758 = new CheckBox(this);
        LinearLayout.LayoutParams layout_642 = new LinearLayout.LayoutParams(0, 0);
        layout_642.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        layout_642.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        checkBox_758.setLayoutParams(layout_642);
        checkBox_758.setText(varlist.getvariable_desc());
        checkBox_758.setId(generateViewId());
        if (varlist.getvariable_data().equals("1")) {
            checkBox_758.setChecked(true);
        }

        checkBox_758.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (checkBox_758.isChecked()) {
//                        Toast.makeText(datafrommaster.this, varlist.getvariable_name()+"", Toast.LENGTH_SHORT).show();
                        skip_rules(varlist,position,varlist.getvariable_name().toString());
                        saveData(varlist, "1");
                    } else {
                        saveData(varlist, "");
                    }
                }
            }
        );

        return checkBox_758;
    }

    public View maketextview(int count, module_variable_DataModel varlist, int position) {
        TextView textView_801 = new TextView(this);
        textView_801.setText((count + 1) + ". " + varlist.getvariable_desc());
        LinearLayout.LayoutParams layout_523 = new LinearLayout.LayoutParams(0, 0);
        layout_523.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layout_523.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        textView_801.setLayoutParams(layout_523);

        return textView_801;
    }

    //-------------------------------------------------------------------------SKIP RULES
    public void skip_rules(final module_variable_DataModel varlist, int postion, String value) {

        for (int i = 0; i < letsmapskip_rule.size(); i++) {
            if (letsmapskip_rule.get(i).getVariablename().equals(varlist.getvariable_name().toString())) {

                if (letsmapskip_rule.get(i).getClause().equals(value)) {
                    Log.d("My value ===REMOVE", varlist.getvariable_name().toString() + "==" +
                            letsmapskip_rule.get(i).getRemovalvalue());
                    removevariableview(letsmapskip_rule.get(i).getRemovalvalue());
                } else {
                    Log.d("My value ===ADD", varlist.getvariable_name().toString() + "==" +
                            letsmapskip_rule.get(i).getRemovalvalue());
                    addvariableview(letsmapskip_rule.get(i).getRemovalvalue());
                }

            }
        }
    }

    public void addvariableview(String variable) {

        int count = start.getChildCount();
        boolean is_exist = false;
        for (int i = 0; i < count; i++) {
            if (start.getChildAt(i).getTag().toString().equals(variable.toString())) {

                is_exist = true;
                break;
            }
        }

        if (!is_exist) {

            for (int i = 0; i < variableList.size(); i++) {
                if (variableList.get(i).getvariable_name().toString().equals(variable.toString())) {
                    variableList.get(i).set_status("2");
                    View skipadd = generate(variableList.get(i), i);
                    start.addView(skipadd, i );
                    String response = C.SaveData("Update module_data set status='2' where module_id='" + MODULEID + "' and variable_name='" + variableList.get(i).getvariable_name().toString() + "' and data_id='" + DATAID + "'");
                }
            }
        }
    }

    public void removevariableview(String variable) {
        int count = start.getChildCount();
        View removeview = null;
        for (int i = 0; i < count; i++) {
            if (start.getChildAt(i).getTag().toString().equals(variable.toString())) {
                removeview = start.getChildAt(i);
            }
        }
        for (int i = 0; i < variableList.size(); i++) {
            if (variable.toString().equals(variableList.get(i).getvariable_name().toString())) {
                saveData(variableList.get(i), "");
                variableList.get(i).set_status("1");
                String response = C.SaveData("Update module_data set status='1' where module_id='" + MODULEID + "' and variable_name='" + variableList.get(i).getvariable_name().toString() + "' and data_id='" + DATAID + "'");
                break;
            }
        }
        start.removeView(removeview);
    }
}

//------------------------------------------GARABGE

//        for(int i=0;i< )
//        View cardchhild2 = generate(variableList.get(4), 5);
//        start.addView(cardchhild2, 5);

//        int count = start.getChildCount();
//        for (int i = 0; i < count; i++) {
//            View removeview = start.getChildAt(i);
//            if(removeview.getTag().toString().equals(variable.toString())){
//                start.removeView(removeview);
//            }
////            if(start.getChildAt(i).getTag().toString().equals(variable.toString())){
////                removeview = start.getChildAt(i);
////                start.removeView(removeview);
////            }
//        }


//        View cardchhild2 = generate(variableList.get(4), 5);
//        start.addView(cardchhild2, 5);
//        for(int i =0; i < variableList.size();i++){
//            if(variableList.get(i).getvariable_name().toString().equals(variable.toString())){
//                View skipadd = generate(variableList.get(i), i);
//                start.addView(skipadd,(i+1));
//            }
//        }
//        start.removeViewAt(4);


//                Log.d("My value ===", varlist.getvariable_name().toString() + "==" +
//                        letsmapskip_rule.get(i).getRemovalvalue());

//        for (int i = 0; i < radioGroup_441.getChildCount(); i++) {
//            rb = (RadioButton) radioGroup_441.getChildAt(i);
//            if (varlist.getvariable_data().equals(varlist.getvariable_option().split(",")[i].split("-")[0])) {
//                rb.setChecked(true);
//            }
//        }
//        final ArrayList<Integer> removeviewnow = new ArrayList<>();

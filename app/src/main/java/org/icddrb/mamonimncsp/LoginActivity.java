package org.icddrb.mamonimncsp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import Common.Connection;
import Common.Global;
import Common.ProjectSetting;
import Utility.MySharedPreferences;

public class LoginActivity extends AppCompatActivity {
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    Connection C;
    Global g;
    boolean networkAvailable = false;
    int count = 0;
    TextView lblStaffType;
    String   SystemUpdateDT="";
    private ProgressDialog dialog;
    private  String Password="";
    MySharedPreferences sp;
    TextView Country;
    TextView Facility;
    EditText pass;

    public static final String SECURITY_TAG = "Security Permission";
    private static final int REQUEST_Code = 0;
    private static String[] PERMISSIONS_LIST = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA};

    private void checkPermission()
    {
        Log.e(SECURITY_TAG,"Checking Permission.");
        if (
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) &
                        (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) &
                        (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                ) {
            Log.e(SECURITY_TAG,"Calling Requesting Permission!!!");
            requestPermission();
        } else {
            Log.e(SECURITY_TAG,"Your permission has already been granted.");

            Activity_Load();
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Log.e(SECURITY_TAG,"Requesting Permission to User.");
            ActivityCompat.requestPermissions(this,PERMISSIONS_LIST,REQUEST_Code);
        } else {
            Log.e(SECURITY_TAG,"Requesting Permission Directly.");
            ActivityCompat.requestPermissions(this,PERMISSIONS_LIST,REQUEST_Code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length == PERMISSIONS_LIST.length && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //********* Granted ********
            Activity_Load();
        } else {
            //********* Not Granted ********
            ActivityCompat.requestPermissions(this,PERMISSIONS_LIST,REQUEST_Code);
        }
    }

    @Override
    public boolean onKeyDown(int iKeyCode, KeyEvent event) {
        if (iKeyCode == KeyEvent.KEYCODE_BACK || iKeyCode == KeyEvent.KEYCODE_HOME) {
            AlertDialog.Builder adb = new AlertDialog.Builder(LoginActivity.this);
            adb.setTitle("Close");
            adb.setMessage("Do you want to exist from the system?");
            adb.setNegativeButton("No", null);
            adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            adb.show();
            return false;
        } else {
            return true;
        }
    }

    private void Activity_Load(){
        try
        {
            C = new Connection(this);
            g = Global.getInstance();
            sp = new MySharedPreferences();
            sp.save(this,"deviceid","");
            sp.save(this,"userid","");

            final TextView UniqueUserId      = (TextView)findViewById(R.id.UniqueUserId);
            final Spinner uid      = (Spinner)findViewById(R.id.userId);
            final EditText pass    = (EditText)findViewById(R.id.pass);
            TextView lblSystemDate = (TextView)findViewById(R.id.lblSystemDate);

            SystemUpdateDT = ProjectSetting.VersionDate;
            lblSystemDate.setText("Version: 1.0, Built on: "+ SystemUpdateDT);

            //Check for Internet connectivity
            networkAvailable = Connection.haveNetworkConnection(LoginActivity.this);

            //Rebuild Database
            String TotalTab = C.ReturnSingleValue("SELECT count(*) FROM sqlite_master WHERE type = 'table' AND name != 'android_metadata' AND name != 'sqlite_sequence'");

            if(Integer.valueOf(TotalTab) == 0)
            {
                if (networkAvailable)
                {
                    //Call Setting Form
                    finish();
                    Intent f1 = new Intent(getApplicationContext(), SettingForm.class);
                    startActivity(f1);
                    return;
                }
                else
                {
                    Connection.MessageBox(LoginActivity.this,"Internet connection is not available for building initial database.");
                    return;
                }
            }

            //Device Unique ID
            final String UniqueID = C.ReturnSingleValue("Select DeviceId from DeviceList");
            UniqueUserId.setText("Unique ID :"+ UniqueID);
            sp.save(this,"deviceid",UniqueID);

            //**************************************************************************************
            if (networkAvailable)
            {
                Intent syncService = new Intent(this, Sync_Service.class);
                startService(syncService);
            }

            //Prepare Index Table for Data Sync: 26 Aug 2018
            /*String IndexSQL = "Insert into sync_index_id(DeviceID,TableName,indexid) Select '"+ UniqueID +"',t.TableName,'' indexid from DatabaseTab t\n" +
                    "where Sync_Download='Y' and not exists(select * from sync_index_id where TableName=t.TableName)";
            try {
                C.SaveData(IndexSQL);
            }catch (Exception ex){

            }*/

            //**************************************************************************************
            uid.setAdapter(C.getArrayAdapter("select UserId||'-'||UserName User from DataCollector order by UserName"));
            String[] CL = uid.getSelectedItem().toString().split("-");

            /*Button btnClose=(Button)findViewById(R.id.btnClose);
            btnClose.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    finish();
                    System.exit(0);
                }
            });*/

            //Login -----------------------------------------------------------------------
            Button loginButton = (Button) findViewById(R.id.btnLogin);
            loginButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    try
                    {
                        String[] U = Connection.split(uid.getSelectedItem().toString(),'-');
                        sp.save(LoginActivity.this,"userid",U[0]);

                        if (!C.Existence("Select * from DataCollector where UserId='" + U[0] + "' and Pass='" + pass.getText().toString() + "'"))
                        {
                            Connection.MessageBox(LoginActivity.this,"This is not a valid user id or password");
                            return;
                        }

                        //Store Last Login information
                        String response = C.SaveData("Delete from LastLogin");
                        String response1 = C.SaveData("Insert into LastLogin(UserId)Values('"+ U[0] +"')");

                        //Download Updated System
                        //...................................................................................
                        if (networkAvailable == true)
                        {
                            //Retrieve data from server for checking local device
                            String[] ServerVal  = Connection.split(C.ReturnResult("ReturnSingleValue","sp_ServerCheck '"+ UniqueID +"'"),',');
                            String ServerDate   = ServerVal[0].toString();
                            String UpdateDT     = ServerVal[1].toString();

                            //Check for New Version
                            if (!UpdateDT.equals(SystemUpdateDT)) {
                                LoginActivity.SystemDownload d = new LoginActivity.SystemDownload();
                                d.setContext(getApplicationContext());
                                d.execute(Global.UpdatedSystem);
                            }
                            else
                            {
                                //check for system date
                                if(ServerDate.equals(Global.TodaysDateforCheck())==false)
                                {
                                    Connection.MessageBox(LoginActivity.this, "System date is incorrect ["+ Global.DateNowDMY() +"]");
                                    startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                                    return;
                                }

                                final ProgressDialog progDailog = ProgressDialog.show(LoginActivity.this, "", "Please Wait . . .", true);

                                new Thread() {
                                    public void run() {
                                        try {
                                            finish();
                                            Intent f1 = new Intent(getApplicationContext(),MainActivity.class);
                                            startActivity(f1);
                                        } catch (Exception e) {

                                        }
                                        progDailog.dismiss();
                                    }
                                }.start();
                            }
                        }
                        else
                        {
                            final ProgressDialog progDailog = ProgressDialog.show(LoginActivity.this, "", "Please Wait . . .", true);

                            new Thread() {
                                public void run() {
                                    try {
                                        finish();
                                        Intent f1 = new Intent(getApplicationContext(),MainActivity.class);
                                        startActivity(f1);
                                    } catch (Exception e) {

                                    }
                                    progDailog.dismiss();
                                }
                            }.start();
                        }
                    }
                    catch(Exception ex)
                    {
                        //Connection.MessageBox(LoginActivity.this, ex.getMessage());
                        //return;
                        final ProgressDialog progDailog = ProgressDialog.show(LoginActivity.this, "", "Please Wait . . .", true);

                        new Thread() {
                            public void run() {
                                try {
                                    finish();
                                    Intent f1 = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(f1);
                                } catch (Exception e) {

                                }
                                progDailog.dismiss();
                            }
                        }.start();
                    }
                }
            });
        }
        catch(Exception ex)
        {
            Connection.MessageBox(LoginActivity.this, ex.getMessage());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  // Removes notification bar

            setContentView(R.layout.login_activity);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            checkPermission();

        } catch (Exception ex) {
            Connection.MessageBox(LoginActivity.this, ex.getMessage());
        }
    }

    //Install application
    private void InstallApplication()
    {
        File apkfile = new File(Environment.getExternalStorageDirectory() + File.separator + ProjectSetting.NewVersionName +".apk");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
        intent.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");

        startActivity(intent);
    }

    //Downloading updated system from the central server
    class SystemDownload extends AsyncTask<String, String, Void> {
        private Context context;

        public void setContext(Context contextf){
            context = contextf;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setMessage("Downloading Updated System...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.show();
        }


        protected void onProgressUpdate(String... progress) {
            dialog.setProgress(Integer.parseInt(progress[0]));
            //publishProgress(progress);

        }

        //@Override
        protected void onPostExecute(String unused) {
            dialog.dismiss();
        }


        @Override
        protected Void doInBackground(String... arg0) {
            try {
                URL url = new URL(arg0[0]);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.connect();
                int lenghtOfFile = c.getContentLength();

                File file=Environment.getExternalStorageDirectory();

                file.mkdirs();
                File outputFile = new File(file.getAbsolutePath()+ File.separator + ProjectSetting.NewVersionName +".apk");

                if(outputFile.exists()){
                    outputFile.delete();
                }
                else
                {
                    outputFile.createNewFile();
                }

                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = c.getInputStream();


                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                    count++;
                }
                fos.close();
                is.close();


                InstallApplication();

                dialog.dismiss();

            } catch (IOException e) {
                //Log.e("UpdateAPP", "Update error! " + e.getMessage());
            }
            return null;
        }
    }
}
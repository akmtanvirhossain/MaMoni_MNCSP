package org.icddrb.standard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import Common.Connection;
import Common.Global;
import Utility.MySharedPreferences;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottom_nav_view;
    Boolean netwoekAvailable = false;
    Bundle IDbundle;
    Connection C;
    static String DEVICEID  = "";
    static String ENTRYUSER = "";
    ProgressDialog progDailog;
    int jumpTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.standard_navigation_drawer);

        C = new Connection(this);
        DEVICEID    = MySharedPreferences.getValue(this,"deviceid");
        ENTRYUSER   = MySharedPreferences.getValue(this,"userid");

        IDbundle = new Bundle();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        GridView gv = (GridView) findViewById(R.id.gridview);
        gv.setAdapter(new menuAdapter(this));

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                try
                {
                    //Data Collection
                    if(position==0)
                    {
                        /*IDbundle.putString("moduleid", "1");
                        IDbundle.putString("dataid", "1235");
                        IDbundle.putString("name", "Sakib");
                        IDbundle.putString("age", "5 month");
                        IDbundle.putString("id", "#123");
                        IDbundle.putString("moduleName", "test form");
                        Intent intent = new Intent(getApplicationContext(), data_form_master.class);
                        intent.putExtras(IDbundle);
                        startActivity(intent);*/
                    }

                    //Monitoring
                    else if(position==1)
                    {

                    }

                    //Data Search
                    else if(position==2)
                    {

                    }

                    //Data Sync
                    //*******************************************************************************
                    /*else if(position==3)
                    {
                        if (Connection.haveNetworkConnection(MainActivity.this)) {
                            netwoekAvailable=true;

                        } else {
                            netwoekAvailable=false;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder
                                .setTitle("Data Sync")
                                .setMessage("Do you want to synchronize data to server[Y/N]?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE:

                                                if (Connection.haveNetworkConnection(MainActivity.this)) {
                                                } else {
                                                    Connection.MessageBox(MainActivity.this,"Internet connection is not available for Data Sync.");
                                                    return;
                                                }

                                                new DataSyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,DEVICEID);


                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                break;
                                        }
                                    }
                                })
                                .setNegativeButton("No", null)	//Do nothing on no
                                .show();
                    }*/
                    else if(position==3)
                    {
                        if (Connection.haveNetworkConnection(MainActivity.this)) {
                            netwoekAvailable=true;

                        } else {
                            netwoekAvailable=false;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder
                                .setTitle("Data Sync")
                                .setIcon(R.drawable.data_sync)
                                .setMessage("Do you want to synchronize data to server[Y/N]?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE:

                                                if(netwoekAvailable==false)
                                                {
                                                    Connection.MessageBox(MainActivity.this, "Internet connection is not avialable.");
                                                    return;
                                                }

                                                try
                                                {
                                                    String ResponseString="Status:";

                                                    progDailog = new ProgressDialog(MainActivity.this);
                                                    progDailog.setMessage("Synchronizing database, Please Wait . . .");
                                                    progDailog.setIndeterminate(false);
                                                    progDailog.setCancelable(false);
                                                    progDailog.setProgress(0);
                                                    progDailog.show();

                                                    new Thread() {
                                                        public void run() {
                                                            try {
                                                                C.DataSync(DEVICEID, progDailog, progressHandler);
                                                            } catch (Exception e) {

                                                            }
                                                            progDailog.dismiss();

                                                        }
                                                    }.start();
                                                }
                                                catch(Exception ex)
                                                {
                                                    Connection.MessageBox(MainActivity.this, ex.getMessage());
                                                }

                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                break;
                                        }
                                    }
                                })
                                .setNegativeButton("No", null)	//Do nothing on no
                                .show();
                    }

                    //Exit from the system
                    //*******************************************************************************
                    else if(position==4)
                    {

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder
                                .setTitle("Exit")
                                .setIcon(R.drawable.exit)
                                .setMessage("Do you want to exit from the system[Y/N]?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE:
                                                finish();
                                                System.exit(0);
                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                break;
                                        }
                                    }
                                })
                                .setNegativeButton("No", null)	//Do nothing on no
                                .show();
                    }

                }
                catch(Exception ex)
                {
                    Connection.MessageBox(MainActivity.this, ex.getMessage());
                }
            }
        });


    }
    Handler progressHandler = new Handler() {
        public void handleMessage(Message msg) {
            progDailog.setMessage(Global.getInstance().getProgressMessage());
            progDailog.incrementProgressBy(jumpTime);
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_DataSync) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder
                    .setTitle("Data Sync")
                    .setIcon(R.drawable.data_sync)
                    .setMessage("Do you want to sync data to server[Y/N]?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    })
                    .setNegativeButton("No", null)	//Do nothing on no
                    .show();
        } else if (id == R.id.nav_Exit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder
                    .setTitle("Exit")
                    .setIcon(R.drawable.exit)
                    .setMessage("Do you want to exit from the system[Y/N]?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    finish();
                                    System.exit(0);
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    })
                    .setNegativeButton("No", null)	//Do nothing on no
                    .show();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    public class menuAdapter extends BaseAdapter {
        private Context mContext;

        public menuAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View MyView = convertView;
            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                MyView = li.inflate(R.layout.mainmenugriditem, null);

                try {
                    // Add The Image!!!
                    ImageView iv = (ImageView) MyView.findViewById(R.id.itemImage);
                    iv.setImageResource(mThumbIds[position]);

                    // Add The Text!!!
                    TextView tv = (TextView) MyView.findViewById(R.id.itemDesc);
                    tv.setTextSize(20);
                    tv.setText(desc[position]);
                }catch (Exception ex){
                    //String a = ex.getMessage();
                }
            }
            return MyView;
        }

        private String[] desc={
                "New",
                "Monitoring",
                "Data Search",
                "Data Sync",
                "Exit"};


        //references to our images
        private Integer[] mThumbIds = {
                R.drawable.new_entry,
                R.drawable.reports,
                R.drawable.data_search,
                R.drawable.data_sync,
                R.drawable.exit
        };
    }


    private class DataSyncTask extends AsyncTask<String, Void, Void> {
        ProgressDialog dialog;
        private Context context;
        String resp = "";
        public void setContext(Context contextf){
            context = contextf;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("Data Sync");
            dialog.setMessage("Data Sync in Progress, Please wait ...");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.show();
        }

        //@Override
        protected void onProgressUpdate(String... values) {
            //super.onProgressUpdate(values);
            dialog.setProgress(Integer.parseInt(values[0].toString().split(",")[1]));
        }

        @Override
        protected Void doInBackground(String... params) {
            final String[] ID = params[0].toString().split("-");
            final String DEVICEID    = ID[0].toString();

            try {

                new Thread() {
                    public void run() {
                        try {

                            //Upload
                            List<String> tableList = new ArrayList<String>();
                            tableList.add("DataCollector");

                            int progressCount = 50/tableList.size();
                            int count = 0;
                            for (int i = 0; i < tableList.size(); i++) {
                                try {
                                    C.Sync_Upload_Process(tableList.get(i).toString());
                                    count +=progressCount;
                                    onProgressUpdate(tableList.get(i).toString()+","+String.valueOf(count));
                                }catch(Exception ex){

                                }
                            }

                            //Download
                            progressCount = 50/tableList.size();
                            for (int i = 0; i < tableList.size(); i++) {
                                try {
                                    C.Sync_Download(tableList.get(i).toString(), DEVICEID,"");
                                    count +=progressCount;
                                    onProgressUpdate(tableList.get(i).toString()+","+String.valueOf(count));
                                }catch(Exception ex){

                                }
                            }

                            dialog.dismiss();

                        } catch (Exception e) {
                            resp = e.getMessage();
                            dialog.dismiss();
                        }
                        finally {
                            dialog.dismiss();
                        }
                    }
                }.start();

            } catch (Exception e) {

            }
            // do stuff!
            return null;
        }

        //@Override
        protected void onPostExecute(String result) {
            if(result.length()!=0) {
                Connection.MessageBox(MainActivity.this, "Data Sync successfully completed.");
                dialog.dismiss();
            }
        }
    }
}

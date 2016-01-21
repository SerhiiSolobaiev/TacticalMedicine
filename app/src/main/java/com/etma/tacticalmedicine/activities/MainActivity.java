package com.etma.tacticalmedicine.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.etma.tacticalmedicine.DBHelper;
import com.etma.tacticalmedicine.R;
import com.etma.tacticalmedicine.fragments.ImageToZoomFragment;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//  TODO
//1. Сделать больше картинку
//2. Настройки (добавить язык, размер шрифта)
//3. Добавить фрагменты!!!
//4. Баг: выбор части тела - выбор травмы - нажимаем назад - выводится список всех травм (а не для выбранной части тела)


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    public Drawer drawerResult;

    private DBHelper mDataBaseHelper;
    private SQLiteDatabase db;
    private Spinner spinner;
    private ArrayAdapter<String> dataAdapter;

    private ImageToZoomFragment myFragment;
    //static boolean active = false; //for check if this activity is running

    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setNavigationDrawer(toolbar);

        selectBodyPart();

        myFragment = new ImageToZoomFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container_for_images,
                    myFragment).commit();
        }

        sp = PreferenceManager.getDefaultSharedPreferences(this);

    }

    private void selectBodyPart(){
        try {
            spinner = (Spinner) findViewById(R.id.spinnerList);

            mDataBaseHelper = new DBHelper(this);
            db = mDataBaseHelper.getReadableDatabase();
            Cursor c = db.query(mDataBaseHelper.TABLE_NAME_PART_BODY,
                    null, null, null, null, null, null);

            List<String> listBodyParts = new ArrayList<>();
            listBodyParts.add(getResources().getString(R.string.select_part_body));
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndex(mDataBaseHelper.PART_BODY_NAME));
                listBodyParts.add(name);
            }
            c.close();

            dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, listBodyParts);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dataAdapter.notifyDataSetChanged();
            spinner.setAdapter(dataAdapter);
        }
        catch (Exception e){
            Log.v(TAG,"Exception while added values to spinner: "+e.getMessage());
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                String nameBodyPart = parentView.getItemAtPosition(position).toString();
                if ( position!= 0) {
                    Log.v(TAG,"nameBodyPart = " + nameBodyPart);
                    Intent intent = new Intent(MainActivity.this, Search.class);
                    intent.putExtra("idPartBodyToShow", returnIdByNameBodyPart(nameBodyPart));
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    @Override
    protected void onResume() {

        String language = sp.getString("prefAppLanguage","1");
        super.onResume();
        switch (language){
            case "1":
                Locale locale = new Locale("ua");
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                break;
            case "3":
                Locale locale_ru = new Locale("eu-US");
                Locale.setDefault(locale_ru);
                Configuration config_ru = new Configuration();
                config_ru.locale = locale_ru;
                getBaseContext().getResources().updateConfiguration(config_ru, getBaseContext().getResources().getDisplayMetrics());
                break;
            default:
                Toast.makeText(getApplicationContext(),"=(",Toast.LENGTH_SHORT).show();
        }
    }

    private int returnIdByNameBodyPart(String nameBodyPart){
        int id = 0;
        mDataBaseHelper = new DBHelper(this);
        db = mDataBaseHelper.getReadableDatabase();
        String selection = mDataBaseHelper.PART_BODY_NAME + " = '" + nameBodyPart + "'";
        Cursor c = db.query(mDataBaseHelper.TABLE_NAME_PART_BODY,
                null, selection,
                null, null, null, null);
        while (c.moveToNext()){
            id = c.getInt(c.getColumnIndex(mDataBaseHelper.PART_BODY_UID));
        }
        return id;
    }
    public void setNavigationDrawer(Toolbar toolbar){
        AccountHeader accountHeaderResult = initializeAccountHeader();

        drawerResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeaderResult)
                .withDisplayBelowStatusBar(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(initializeDrawerItems())
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        setOnDrawerItemClickListener(position);
                        return false;
                    }
                })
                 .build();
    }

    private void setOnDrawerItemClickListener(int position) {
        switch (position){
            case 1:
                break;
            case 2:
                callSearchActivity();
                break;
            case 3:
                Log.v(TAG,"clicked on DividerDrawerItem = IMPOSSIBLE!!!");
                break;
            case 4:
                showInformationAboutAssistance();
                break;
            case 5:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                finish();
                break;
            case 6:
                showInformationAboutAbout();
                break;
            default:
                Toast.makeText(getBaseContext(),"Position = " + position,Toast.LENGTH_SHORT).show();
        }
    }

    private void showInformationAboutAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.about_item));
        builder.setMessage(getResources().getString(R.string.about_for_app)); // сообщение

        builder.setPositiveButton(getResources().getString(R.string.yes_for_one_way), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    private void showInformationAboutAssistance() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.help_item));
        builder.setMessage(getResources().getString(R.string.assistance_for_app)); // сообщение

        builder.setPositiveButton(getResources().getString(R.string.yes_for_one_way), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    @NonNull
    private IDrawerItem[] initializeDrawerItems() {
        return new IDrawerItem[]{
                new PrimaryDrawerItem()
                        .withName(R.string.home_item)
                        .withIdentifier(1)
                        .withIcon(R.drawable.home),
                new SecondaryDrawerItem()
                    .withName(R.string.show_all_traumas)
                    .withIdentifier(2)
                    .withIcon(R.drawable.show_all),
                new DividerDrawerItem(),
                new SecondaryDrawerItem()
                        .withName(R.string.help_item)
                        .withIdentifier(3)
                        .withIcon(R.drawable.help_circle),
                new SecondaryDrawerItem()
                        .withName(R.string.settings_item)
                        .withIdentifier(4)
                        .withIcon(R.drawable.settings),
                new SecondaryDrawerItem()
                        .withName(R.string.about_item)
                        .withIdentifier(5)
                        .withIcon(R.drawable.information)};
    }

    private AccountHeader initializeAccountHeader() {
        return new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.drawable.medical_logo)
                    .build();
    }

    @Override
    public void onBackPressed() {
        if (drawerResult != null && drawerResult.isDrawerOpen()){
            drawerResult.closeDrawer();
        }
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.sort_by) {
            return true;
        }*/
        if (id == R.id.action_search){
            callSearchActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void callSearchActivity(){
        Intent intent_search = new Intent(this, Search.class);
        startActivity(intent_search);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageView2:
                showAlertDialogForBodyPart(getResources().getString(R.string.head),4);
                Log.v(TAG,"Clicked on "+R.id.imageView2);
                break;
            case R.id.imageView4:
                showAlertDialogForBodyPart(getResources().getString(R.string.hand),2);
                Log.v(TAG, "Clicked on " + R.id.imageView4);
                break;
            case R.id.imageView6:
                showAlertDialogForBodyPart(getResources().getString(R.string.hand),2);
                Log.v(TAG, "Clicked on " + R.id.imageView4);
                break;
            case R.id.imageView5:
                showAlertDialogForBodyPart(getResources().getString(R.string.body),3);
                Log.v(TAG, "Clicked on " + R.id.imageView4);
                break;
            case R.id.imageView8:
                showAlertDialogForBodyPart(getResources().getString(R.string.body),3);
                Log.v(TAG, "Clicked on " + R.id.imageView4);
                break;
            case R.id.imageView11:
                showAlertDialogForBodyPart(getResources().getString(R.string.leg),5);
                Log.v(TAG, "Clicked on " + R.id.imageView4);
                break;
            case R.id.imageView14:
                showAlertDialogForBodyPart(getResources().getString(R.string.leg),5);
                Log.v(TAG, "Clicked on " + R.id.imageView4);
                break;
        }
    }
    private void showAlertDialogForBodyPart(String bodyPart, final int id_body_part_to_show){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.alert_title) +" "+ bodyPart + "?");
        //builder.setMessage("asdad"); // сообщение

        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, Search.class);
                intent.putExtra("idPartBodyToShow", id_body_part_to_show);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(true);
        builder.show();
    }

}


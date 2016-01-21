package com.etma.tacticalmedicine.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.etma.tacticalmedicine.DBHelper;
import com.etma.tacticalmedicine.R;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.Collections;

public class Search extends AppCompatActivity {

    private static final String TAG = "Search";

    EditText editTextSearch;
    Button buttonDeleteText;
    ListView listTraumas;
    ArrayAdapter<String> adapter;

    private Parcelable state; //for saving current state of listview
    private ArrayList<String> results = new ArrayList<>();
    private DBHelper mDataBaseHelper;
    private SQLiteDatabase db;

    private static final String STATE_PART = "partLevel";
    private static int idBodyPart;

    public Drawer drawerResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setNavigationDrawer(toolbar);
        //MainActivity.setNavigationDrawer();
        editTextSearchListener();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                idBodyPart = 0;
            } else {
                idBodyPart = extras.getInt("idPartBodyToShow");
                Log.v(TAG,"idPartBodyToShow = " + idBodyPart);
            }
        } else {
            idBodyPart = savedInstanceState.getInt(STATE_PART);
        }
        showTraumasFromBD(idBodyPart);
        setOnNameClickListener();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_PART,idBodyPart);
        state = listTraumas.onSaveInstanceState();
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(state != null) {
            listTraumas.onRestoreInstanceState(state);
        }
    }

    private void editTextSearchListener(){
        editTextSearch = (EditText)findViewById(R.id.editTextSearch);
        buttonDeleteText = (Button)findViewById(R.id.delete_all_text);

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isVisiblebuttonDeleteText(s);
                Search.this.adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void isVisiblebuttonDeleteText(CharSequence s) {
        if (s.toString().trim().length() == 0)
            buttonDeleteText.setVisibility(View.INVISIBLE);
        else
            buttonDeleteText.setVisibility(View.VISIBLE);
    }

    public void deleteTextFromSearchField(View view){
        editTextSearch = (EditText)findViewById(R.id.editTextSearch);
        editTextSearch.setText("");
    }

    private void showTraumasFromBD(int idBodyPart){
        try {
            Log.v(TAG, "in showTraumasFromBD");
            mDataBaseHelper = new DBHelper(this);
            db = mDataBaseHelper.getReadableDatabase();

            Cursor c;
            if (idBodyPart != 0) {
                String selection = mDataBaseHelper.TRAUMAS_ID_PART_BODY + "=" + idBodyPart;
                c = db.query(mDataBaseHelper.TABLE_NAME_TRAUMAS,
                        null, selection,
                        null, null, null, null);
            }
            else {
                c = db.query(mDataBaseHelper.TABLE_NAME_TRAUMAS,
                        null, null, null, null, null, null);
            }
            if (c != null) {
                if (c.moveToFirst()) {
                    Log.v(TAG, "Cursor c.getCount() = " + c.getCount());
                    do {
                        String Name = c.getString(c.getColumnIndex(mDataBaseHelper.TRAUMAS_NAME));
                        results.add(Name);
                    } while (c.moveToNext());
                }
                c.close();
            }
            Collections.sort(results);
            listTraumas = (ListView)findViewById(R.id.listViewTraumas);
            adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, results);
            listTraumas.setAdapter(adapter);
        }
        catch (Exception e){
            Log.v(TAG, "Exception in showTraumasFromBD = " + e.getMessage());
            //Toast.makeText(this, "Cannot read values from BD :(", Toast.LENGTH_SHORT).show();
        }
    }
    private void setOnNameClickListener(){
        mDataBaseHelper = new DBHelper(this);
        db = mDataBaseHelper.getReadableDatabase();
        listTraumas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String nameTrauma = ((TextView) view).getText().toString();
                Log.v(TAG, "nameTrauma = " + nameTrauma);

                String selection = mDataBaseHelper.TRAUMAS_NAME + " = '" + nameTrauma + "'";
                Cursor c = db.query(mDataBaseHelper.TABLE_NAME_TRAUMAS,
                        null, selection,
                        null, null, null, null
                );
                int idArticle = 0;
                if (c != null) {
                    if (c.moveToFirst()) {
                        do {
                            idArticle = c.getInt(c.getColumnIndex(mDataBaseHelper.TRAUMAS_ID_ARTICLE));
                            Log.v(TAG, "idArticle  = " + idArticle);
                        } while (c.moveToNext());
                    }
                    c.close();
                }
                Intent intent = new Intent(Search.this, ShowArticle.class);
                intent.putExtra("idArticleToShow", idArticle);
                startActivity(intent);
            }
        });
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

    private void setOnDrawerItemClickListener(int position) {
        switch (position){
            case 1:
                Intent intent_main = new Intent(this, MainActivity.class);
                startActivity(intent_main);
                break;
            case 2:
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
                break;
            case 6:
                showInformationAboutAbout();
                break;
            default:
                Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
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
}

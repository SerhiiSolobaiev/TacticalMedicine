package com.etma.tacticalmedicine.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AlignmentSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.io.FileInputStream;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowArticle extends AppCompatActivity {

    private static final String TAG = "ShowArticle";
    private DBHelper mDataBaseHelper;
    private SQLiteDatabase db;

    private static final String STATE_ARTICLE = "articleID";
    private int idArticleToShow;

    TextView nameArticle;
    TextView article;

    public Drawer drawerResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_article);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                idArticleToShow = 0;
            } else {
                idArticleToShow = extras.getInt("idArticleToShow");
                Log.v(TAG,"idArticleToShow = " + idArticleToShow);
            }
        } else {
            idArticleToShow = savedInstanceState.getInt(STATE_ARTICLE);
        }
        showArticleWithId(idArticleToShow);


        setNavigationDrawer(toolbar);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_ARTICLE,idArticleToShow);
        super.onSaveInstanceState(outState);
    }

    private void showArticleWithId(int idArticleToShow){
        mDataBaseHelper = new DBHelper(this);
        db = mDataBaseHelper.getReadableDatabase();

        String nameArticleFromBD = null;
        String articleFromBD = null;

        String selection = mDataBaseHelper.ARTICLES_UID + "=" + idArticleToShow;
        Cursor cursor = db.query(mDataBaseHelper.TABLE_NAME_ARTICLES,
                null, selection,
                null, null, null, null
        );

        while (cursor.moveToNext()) {
            nameArticleFromBD = cursor.getString(cursor.getColumnIndex(mDataBaseHelper.ARTICLES_NAME));
            articleFromBD = cursor.getString(cursor.getColumnIndex(mDataBaseHelper.ARTICLES_ARTICLE));

            Log.v(TAG,"nameArticleFromBD = "+nameArticleFromBD);
        }
        cursor.close();

        nameArticle = (TextView)findViewById(R.id.textViewName);
        article = (TextView)findViewById(R.id.textViewArticle);
        nameArticle.setText(nameArticleFromBD);

        article.setText(findImageInText(articleFromBD));
    }

    private SpannableStringBuilder findImageInText(String notParsedArticle){

        SpannableStringBuilder builder = new SpannableStringBuilder();
        String[] arrayMessage = notParsedArticle.split("<image>");
        for (int i = 0; i < arrayMessage.length; i++){
            //image name !contains spaces
            if (!arrayMessage[i].contains(" ")){
                Log.v(TAG,"image on position = " + i);
                ImageSpan imageSpan = null;

                if (getResources().getIdentifier(arrayMessage[i], "drawable", getPackageName()) != 0){
                    Drawable d = getResources().getDrawable(getResources().
                                    getIdentifier(arrayMessage[i], "drawable", getPackageName()));
                    d.setBounds(0, 0, 400,400);
                    imageSpan = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                }
                else
                    Log.v(TAG,"Wrong image name!!!\n");

                builder.append(" "); // without this image NOT SHOWN!!!!!???????
                builder.setSpan(imageSpan, builder.length() - 1, builder.length(), 0);
                AlignmentSpan.Standard as = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER);
                builder.setSpan(as, builder.length() - 1, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else
                builder.append(arrayMessage[i]);
        }
        return builder;
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
                Intent intent_search = new Intent(this, Search.class);
                startActivity(intent_search);
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

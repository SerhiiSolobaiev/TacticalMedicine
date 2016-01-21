package com.etma.tacticalmedicine;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DBHelper extends SQLiteAssetHelper{

    private static final String DATABASE_NAME = "ETMA";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_ARTICLES = "Articles";
    public static final String ARTICLES_UID = "_id";
    public static final String ARTICLES_NAME = "name";
    public static final String ARTICLES_ARTICLE = "article";

    public static final String TABLE_NAME_PART_BODY = "PartBody";
    public static final String PART_BODY_UID = "_id";
    public static final String PART_BODY_NAME = "name";

    public static final String TABLE_NAME_TRAUMAS = "Traumas";
    public static final String TRAUMAS_UID = "_id";
    public static final String TRAUMAS_NAME = "name";
    public static final String TRAUMAS_ID_PART_BODY = "id_part_body";
    public static final String TRAUMAS_ID_ARTICLE = "id_article";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}

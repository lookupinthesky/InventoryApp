package com.example.android.inventoryapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.InventoryContract.InventoryEntry;

/**
 * Created by Shubham on 2/11/2017.
 */

public class DbHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "inventory_data.db";
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE "
                + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry.COLOUMN_ID + " INTEGER PRIMARY KEY, "
                + InventoryEntry.COLOUMN_IMAGE + " TEXT, "
                + InventoryEntry.COLOUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLOUMN_PRICE + " INTEGER NOT NULL, "
                + InventoryEntry.COLOUMN_TOTAL_QUANTITY + " INTEGER NOT NULL, "
                + InventoryEntry.COLOUMN_SOLD_QUANTITY + " INTEGER, "
                + InventoryEntry.COLOUMN_CURRENT_QUANTITY + " INTEGER, "
                + InventoryEntry.COLOUMN_SUPPLIER_EMAIL + " TEXT NOT NULL, "
                + InventoryEntry.COLOUMN_PRODUCT_DESCRIPTION + " TEXT)";

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //Do nothing for now
    }
}

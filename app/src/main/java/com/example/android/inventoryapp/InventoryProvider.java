package com.example.android.inventoryapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventoryapp.InventoryContract.InventoryEntry;

import static android.R.attr.id;
import static android.R.attr.name;
import static android.R.attr.x;

/**
 * Created by Shubham on 2/11/2017.
 */

public class InventoryProvider extends ContentProvider {

    //LOGTAG for Logging
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    //defining types of URI
    private static final int INVENTORY = 100;

    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", PRODUCT_ID);
    }

    //Projection with all the coloumns of the table
    public static String[] projection = new String[]{InventoryEntry.COLOUMN_ID,
            InventoryEntry.COLOUMN_PRODUCT_NAME,
            InventoryEntry.COLOUMN_IMAGE,
            InventoryEntry.COLOUMN_PRICE,
            InventoryEntry.COLOUMN_TOTAL_QUANTITY,
            InventoryEntry.COLOUMN_SOLD_QUANTITY,
            InventoryEntry.COLOUMN_CURRENT_QUANTITY,
            InventoryEntry.COLOUMN_SUPPLIER_EMAIL,
            InventoryEntry.COLOUMN_PRODUCT_DESCRIPTION};

    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        mDbHelper = new DbHelper(getContext());
        return true;
    }

    //implementing the query method with two different actions based on the URI state
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String s1) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;
        projection = InventoryProvider.projection;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, null, null, null, null, null);
                break;
            case PRODUCT_ID:
                selection = InventoryEntry.COLOUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;


    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    //method to insert data to database
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);

        if (contentValues.containsKey(InventoryEntry.COLOUMN_PRODUCT_NAME)) {
            String name = contentValues.getAsString(InventoryEntry.COLOUMN_PRODUCT_NAME);
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }
        if (contentValues.containsKey(InventoryEntry.COLOUMN_PRICE)) {
            Integer price = contentValues.getAsInteger(InventoryEntry.COLOUMN_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires a price");
            }
        }
        if (contentValues.containsKey(InventoryEntry.COLOUMN_PRODUCT_DESCRIPTION)) {
            String description = contentValues.getAsString(InventoryEntry.COLOUMN_PRODUCT_DESCRIPTION);
            if (description.isEmpty()) {
                throw new IllegalArgumentException("Product requires a description");
            }
        }
        if (contentValues.containsKey(InventoryEntry.COLOUMN_SUPPLIER_EMAIL)) {
            String email = contentValues.getAsString(InventoryEntry.COLOUMN_SUPPLIER_EMAIL);
            if (email.isEmpty()) {
                throw new IllegalArgumentException("Product requires an email of supplier");
            }
        }
        if (contentValues.containsKey(InventoryEntry.COLOUMN_IMAGE)) {
            String image = contentValues.getAsString(InventoryEntry.COLOUMN_IMAGE);
            if (image.isEmpty()) {
                throw new IllegalArgumentException("Product requires an Image");
            }
        }
        switch (match) {
            case INVENTORY:
                DbHelper mDbHelper = new DbHelper(getContext());
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(InventoryEntry.TABLE_NAME, null, contentValues);
                if (id == -1) {
                    Log.e(LOG_TAG, "Failed to insert row for " + uri);
                    return null;
                }
                // Notify all listeners that the data has changed for the  URI
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }



        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);

    }

    //method to delete data from database
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        DbHelper mDbHelper = new DbHelper(getContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INVENTORY:
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = InventoryEntry.COLOUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;

    }


    //method to update data in database
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        int rowsUpdated;
        DbHelper mDbHelper = new DbHelper(getContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);


        if (contentValues.containsKey(InventoryEntry.COLOUMN_PRODUCT_NAME)) {
            String name = contentValues.getAsString(InventoryEntry.COLOUMN_PRODUCT_NAME);
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }
        if (contentValues.containsKey(InventoryEntry.COLOUMN_PRICE)) {
            Integer price = contentValues.getAsInteger(InventoryEntry.COLOUMN_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires a price");
            }
        }
        if (contentValues.containsKey(InventoryEntry.COLOUMN_PRODUCT_DESCRIPTION)) {
            String description = contentValues.getAsString(InventoryEntry.COLOUMN_PRODUCT_DESCRIPTION);
            if (description.isEmpty()) {
                throw new IllegalArgumentException("Product requires a description");
            }
        }
        if (contentValues.containsKey(InventoryEntry.COLOUMN_SUPPLIER_EMAIL)) {
            String email = contentValues.getAsString(InventoryEntry.COLOUMN_SUPPLIER_EMAIL);
            if (email.isEmpty()) {
                throw new IllegalArgumentException("Product requires an email of supplier");
            }
        }
        if (contentValues.containsKey(InventoryEntry.COLOUMN_IMAGE)) {
            String image = contentValues.getAsString(InventoryEntry.COLOUMN_IMAGE);
            if (image.isEmpty()) {
                throw new IllegalArgumentException("Product requires an Image");
            }
        }


        switch (match) {
            case INVENTORY:
                rowsUpdated = db.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = InventoryEntry.COLOUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = db.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsUpdated;
    }

}

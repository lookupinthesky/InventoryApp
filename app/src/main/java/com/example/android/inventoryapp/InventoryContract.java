package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Shubham on 2/11/2017.
 */

public class InventoryContract {

    //empty constructor so it can't  be instantiated
    private InventoryContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INVENTORY = "inventory";


    public static final class InventoryEntry implements BaseColumns {

        //Content Uri
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //Table Name Constant
        public static final String TABLE_NAME = "inventory";


        //all the name constants for coloumn names

        public static final String COLOUMN_IMAGE = "Image";

        public static final String COLOUMN_PRODUCT_NAME = "Product_Name";

        public static final String COLOUMN_ID = BaseColumns._ID;

        public static final String COLOUMN_PRICE = "Price";

        public static final String COLOUMN_TOTAL_QUANTITY = "Initial_Quantity";

        public static final String COLOUMN_SOLD_QUANTITY = "Sold_Quantity";

        public static final String COLOUMN_CURRENT_QUANTITY = "Current_Quantity";

        public static final String COLOUMN_SUPPLIER_EMAIL = "Supplier_email_Id";

        public static final String COLOUMN_PRODUCT_DESCRIPTION = "Product_Description";

    }


}

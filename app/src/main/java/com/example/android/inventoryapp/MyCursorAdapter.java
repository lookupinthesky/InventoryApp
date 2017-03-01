package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.content.ContentUris;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.InventoryContract.InventoryEntry;

import static android.R.attr.id;
import static com.example.android.inventoryapp.InventoryProvider.LOG_TAG;

import static com.example.android.inventoryapp.R.id.sellButton;
import static java.security.AccessController.getContext;

/**
 * Created by Shubham on 2/12/2017.
 */

public class MyCursorAdapter extends CursorAdapter {

    //constructor

    public MyCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    int soldQuantity;
    int currentQuantity;
    int totalQuantity;

    //inflate the listItem
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_detail, parent, false);
    }

    //assign Values
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //store the inflated view in respective variables
        ImageView product_image = (ImageView) view.findViewById(R.id.product_image);
        TextView product_name = (TextView) view.findViewById(R.id.product_name);
        TextView current_quantity = (TextView) view.findViewById(R.id.current_quantity);
        TextView product_price = (TextView) view.findViewById(R.id.product_price);
        final Button sellButton = (Button) view.findViewById(R.id.sellButton);


        //get coloumn indices from Database
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_PRODUCT_NAME);
        int quantityIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_CURRENT_QUANTITY);
        int priceIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_PRICE);
        int soldQuantityIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_SOLD_QUANTITY);
        int currentQuantityIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_CURRENT_QUANTITY);
        int totalQuantityIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_TOTAL_QUANTITY);

        //set text on the textViews in listItem..
        product_name.setText(cursor.getString(nameColumnIndex));
        String current = "In Stock: " + cursor.getString(quantityIndex);
        current_quantity.setText(current);
        String priceText = "Price: " + cursor.getString(priceIndex);
        product_price.setText(priceText);

        //retrieve the values from Database
        soldQuantity = cursor.getInt(soldQuantityIndex);
        currentQuantity = cursor.getInt(currentQuantityIndex);
        totalQuantity = cursor.getInt(totalQuantityIndex);


        //get id to append with CONTENTURI to call the update method

        int position = cursor.getPosition();
        final long id = getItemId(position);

        //on clicking the sale button - change the values of soldQuantity and currentQuantity and update the database
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentQuantity>0){
                soldQuantity = soldQuantity + 1;
                currentQuantity = totalQuantity - soldQuantity;
                Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLOUMN_SOLD_QUANTITY, soldQuantity);
                values.put(InventoryEntry.COLOUMN_CURRENT_QUANTITY, currentQuantity);
                Context context = view.getContext();
                context.getContentResolver().update(uri, values, null, null);}
                else Toast.makeText(view.getContext(), "Stock Empty! Please purchase more items", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

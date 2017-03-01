package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.InventoryContract.InventoryEntry;

import java.io.FileNotFoundException;

import static android.R.attr.x;
import static com.example.android.inventoryapp.R.id.product_total_quantity;


public class ProductDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //variables for all the views
    ImageView product_image;
    EditText product_name;
    EditText product_price;
    TextView product_id;
    TextView product_id_number;
    TextView total_quantity;
    TextView current_quantity;
    TextView sold_quantity;
    EditText supplier_email;
    EditText product_description;
    LinearLayout new_order_view;
    LinearLayout product_sold_quantity;
    LinearLayout product_current_quantity;
    LinearLayout product_total_quantity;
    LinearLayout product_id_view;
    EditText new_quantity;
    Button place_order_button;
    Button edit_button;

    //quantity variables
    int soldQuantity = 0;
    int currentQuantity;
    int totalQuantity = 0;

    Uri selectedImageUri;

    //String variables to be put on display
    String productPrice;
    String productDescription;
    String productName;
    String newQuantity;
    String stringUri;

    String supplierEmail;

    Uri currentProductUri;
    int price;

    //Activity Modes
    private final int MODE_CREATE = 1;
    private final int MODE_EDIT = 0;
    private final int MODE_VIEW = -1;
    public static int ACTIVITY_MODE;
    private final int INVALID_DATA = -1;
    private final int VALID_DATA = 1;
    private static int SELECT_PICTURE = 1;
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;
    private boolean mImageSelected = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        /* Putting all the inflated views into respective View Variables
        */
        product_name = (EditText) findViewById(R.id.product_name);
        product_price = (EditText) findViewById(R.id.product_price);
        product_id = (TextView) findViewById(R.id.product_id);
        product_id_number = (TextView) findViewById(R.id.product_id_number);
        total_quantity = (TextView) findViewById(R.id.value_total_quantity);
        current_quantity = (TextView) findViewById(R.id.current_quantity);
        sold_quantity = (TextView) findViewById(R.id.sold_quantity);
        supplier_email = (EditText) findViewById(R.id.supplier_email);
        product_description = (EditText) findViewById(R.id.product_description);
        new_quantity = (EditText) findViewById(R.id.new_quantity);
        place_order_button = (Button) findViewById(R.id.place_order_button);

        /*on click event for place_order_button
        * modify the quantity variables
        * save the data to database in MODE _ VIEW
        * send an email to the supplier
        */
        place_order_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newQuantity = new_quantity.getText().toString().trim();
                int quantity = convert2Int(newQuantity);
                totalQuantity = totalQuantity + quantity;
                currentQuantity = totalQuantity - soldQuantity;
                total_quantity.setText(Integer.toString(totalQuantity));
                saveProduct(MODE_VIEW);

                String subject = "New Order";
                String body = "Please send " + quantity + " items";

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", supplierEmail, null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(emailIntent, "Send email..."));


            }
        });

        //continue inflating the views
        product_total_quantity = (LinearLayout)findViewById(R.id.product_total_quantity);
        new_order_view = (LinearLayout) findViewById(R.id.new_order_view);
        product_sold_quantity = (LinearLayout) findViewById(R.id.product_sold_quantity);
        product_current_quantity = (LinearLayout) findViewById(R.id.product_current_quantity);
        product_id_view = (LinearLayout) findViewById(R.id.product_id_view);
        edit_button = (Button) findViewById(R.id.edit_button);

        //onClickEvent for edit_button - Take the activity in Edit Mode
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityModeEdit();
            }
        });

        product_image = (ImageView) findViewById(R.id.product_image);

        //On Clicking the image icon, take the user to gallery and pick an image

        product_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, SELECT_PICTURE);
            }
        });

        //Retrieve the intent when this  activity is opened after list Item Click

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        //if currentProductURi is null it means, we have come through add new product not list item

        if (currentProductUri == null)
            activityModeCreate();
        else
            activityModeView();

        //setting touch listeners to editable fields
        product_name.setOnTouchListener(mTouchListener);
        product_price.setOnTouchListener(mTouchListener);
        product_description.setOnTouchListener(mTouchListener);
        product_id_number.setOnTouchListener(mTouchListener);

    }

    //On Activity result - what to do once an image has been chosen from gallery - scale down the image, convert the uri to string
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            mImageSelected = true;
            try {
                product_image.setImageBitmap(decodeUri(selectedImageUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            stringUri = selectedImageUri.toString();

        }
    }
    //Method to scale down the image picked from gallery to avoid using extra memory

    private Bitmap decodeUri(Uri selectedImageUri) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(
                getContentResolver().openInputStream(selectedImageUri), null, o);

        final int REQUIRED_SIZE = 100;

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (!(width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)) {
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(
                getContentResolver().openInputStream(selectedImageUri), null, o2);
    }

    //Method to be called in MODE_CREATE - when activity is opene from add new product button
    private void activityModeCreate() {
        setTitle(R.string.activity_title_create_new_product);
        invalidateOptionsMenu();
        new_order_view.setVisibility(View.INVISIBLE);
        product_current_quantity.setVisibility(View.INVISIBLE);
        product_sold_quantity.setVisibility(View.INVISIBLE);
        product_id_view.setVisibility(View.INVISIBLE);
        product_total_quantity.setVisibility(View.INVISIBLE);
        edit_button.setVisibility(View.INVISIBLE);

        product_id_view.setVisibility(View.INVISIBLE);
        ACTIVITY_MODE = MODE_CREATE;
    }

    //Method to be called when entering activity by list item click
    private void activityModeView() {
        setTitle(R.string.activity_title_edit_product);
        invalidateOptionsMenu();
        editTextClickable(product_name, false);
        editTextClickable(product_price, false);
        editTextClickable(supplier_email, false);
        editTextClickable(product_description, false);
        product_image.setClickable(false);
        getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        ACTIVITY_MODE = MODE_VIEW;

    }

    //Method to change clickable property of EditTexts
    private void editTextClickable(EditText editText, Boolean bool) {
        editText.setClickable(bool);
        editText.setCursorVisible(bool);
        editText.setFocusable(bool);
        editText.setFocusableInTouchMode(bool);
    }

    //Method to be called from VIEW_MODE when user wants to edit the details
    private void activityModeEdit() {
        invalidateOptionsMenu();
        new_order_view.setVisibility(View.INVISIBLE);
        setTitle(R.string.activity_title_edit_product);
        edit_button.setVisibility(View.INVISIBLE);
        editTextClickable(product_name, true);
        editTextClickable(product_price, true);
        editTextClickable(supplier_email, true);
        editTextClickable(product_description, true);
        product_image.setClickable(true);
        ACTIVITY_MODE = MODE_EDIT;

    }

    //Method to create a content values object for all the variables to be stored
    public ContentValues getContentValues() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryEntry.COLOUMN_IMAGE, stringUri);
        contentValues.put(InventoryEntry.COLOUMN_PRICE, price);
        contentValues.put(InventoryEntry.COLOUMN_PRODUCT_NAME, productName);
        contentValues.put(InventoryEntry.COLOUMN_CURRENT_QUANTITY, currentQuantity);
        contentValues.put(InventoryEntry.COLOUMN_TOTAL_QUANTITY, totalQuantity);
        contentValues.put(InventoryEntry.COLOUMN_SOLD_QUANTITY, soldQuantity);
        contentValues.put(InventoryEntry.COLOUMN_SUPPLIER_EMAIL, supplierEmail);
        contentValues.put(InventoryEntry.COLOUMN_PRODUCT_DESCRIPTION, productDescription);
        return contentValues;
    }


    public int convert2Int(String str) {
        int x;
        if (!str.isEmpty()) {
            try {
                x = Integer.parseInt(str);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, str + " is not a valid number. Please enter a valid number", Toast.LENGTH_SHORT).show();
                x=0;
            }
        } else {
            Toast.makeText(this, "Please enter a value!", Toast.LENGTH_SHORT).show();
            x=0;
        }

        return x;
    }


    //creating the save button on toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu


        switch (item.getItemId()) {
             // Respond to a click on the "Save" menu option
            case R.id.action_save:
                int x;  //save product to database
                switch (ACTIVITY_MODE) {
                    case MODE_CREATE:
                     x = saveProduct(MODE_CREATE);
                        break;
                    case MODE_EDIT:
                      x =  saveProduct(MODE_EDIT);
                        break;
                    default: x = 0;
                }
                // Exit activity if valid data else stay
                if (x == VALID_DATA)
                    finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(ProductDetailActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(ProductDetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //  hide the "Delete" menu item while creating or editing
        if (ACTIVITY_MODE == MODE_EDIT || ACTIVITY_MODE == MODE_CREATE) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        if (ACTIVITY_MODE == MODE_VIEW) {
            MenuItem menuItem = menu.findItem(R.id.action_save);
            menuItem.setVisible(false);
        }
        return true;
    }

    public int saveProduct(int mode)  {

        productName = product_name.getText().toString().trim();
        if (productName.isEmpty()) {
            Toast.makeText(this, "Please enter a name for product", Toast.LENGTH_SHORT).show();
            return INVALID_DATA;
        }


        productPrice = product_price.getText().toString().trim();
        if (productPrice.isEmpty()) {
            Toast.makeText(this, "Please enter the price for product", Toast.LENGTH_SHORT).show();
            return INVALID_DATA;
        } else price = convert2Int(productPrice);


        supplierEmail = supplier_email.getText().toString().trim();
        if (supplierEmail.isEmpty()) {
            Toast.makeText(this, "Please enter the supplier emailID", Toast.LENGTH_SHORT).show();
            return INVALID_DATA;
        }

        productDescription = product_description.getText().toString().trim();
        if (productDescription.isEmpty()) {
            Toast.makeText(this, "Please enter a description for product", Toast.LENGTH_SHORT).show();
            return INVALID_DATA;
        }

        if (mImageSelected) {
            if (stringUri.isEmpty()) {
                Toast.makeText(this, "Please select another image for the product", Toast.LENGTH_SHORT).show();
                return INVALID_DATA;
            }
        } else {
            Toast.makeText(this, "Please select an image for the product", Toast.LENGTH_SHORT).show();
            return INVALID_DATA;
        }


        ContentValues values = getContentValues();
        Uri uri;
        switch (mode) {
            case MODE_CREATE:
                getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
                break;
            case MODE_EDIT:
                uri = currentProductUri;
                getContentResolver().update(uri, values, null, null);
                break;
            case MODE_VIEW:
                uri = currentProductUri;
                getContentResolver().update(uri, values, null, null);
                break;
        }
        return VALID_DATA;
    }


    private void deleteProduct() {

        getContentResolver().delete(currentProductUri, null, null);

        finish();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = InventoryProvider.projection;

        return new CursorLoader(this,
                currentProductUri,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {


            int imageUriColoumnIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_PRODUCT_NAME);
            int currentQuantityIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_CURRENT_QUANTITY);
            int soldQuantityIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_SOLD_QUANTITY);
            int totalQuantityIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_TOTAL_QUANTITY);
            int supplierEmailIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_SUPPLIER_EMAIL);
            int productDescriptionIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_PRODUCT_DESCRIPTION);
            int priceIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_PRICE);
            int idIndex = cursor.getColumnIndex(InventoryEntry.COLOUMN_ID);


            product_name.setText(cursor.getString(nameColumnIndex));
            current_quantity.setText(Integer.toString(cursor.getInt(currentQuantityIndex)));
            total_quantity.setText(Integer.toString(cursor.getInt(totalQuantityIndex)));
            sold_quantity.setText(Integer.toString((cursor.getInt(soldQuantityIndex))));
            soldQuantity = cursor.getInt(soldQuantityIndex);
            totalQuantity = cursor.getInt(totalQuantityIndex);
            currentQuantity = totalQuantity - soldQuantity;


            product_price.setText(Integer.toString((cursor.getInt(priceIndex))));
            product_description.setText(cursor.getString(productDescriptionIndex));
            product_id_number.setText(Integer.toString((cursor.getInt(idIndex))));
            supplier_email.setText(cursor.getString(supplierEmailIndex));

            mImageSelected = true;
            stringUri = cursor.getString(imageUriColoumnIndex);
            try {
                Uri uri = Uri.parse(stringUri);
                product_image.setImageBitmap(decodeUri(uri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        product_name.setText("");
        current_quantity.setText("");
        sold_quantity.setText("");
        total_quantity.setText("");
        product_price.setText("");
        product_description.setText("");
        product_id_number.setText("");

    }


}

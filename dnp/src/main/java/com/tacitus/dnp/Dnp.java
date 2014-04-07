package com.tacitus.dnp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tacitus.dnp.widget.DrawingView;

import junit.framework.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class Dnp extends Activity implements View.OnClickListener {

    private DrawingView mDrawView;
    private SeekBar mBrushSizeChooser;
    private TextView mBrushSizeChooserText;
    private TextView mBrushSizeChooserTitle;
    private SeekBar mAlphaChooser;
    private TextView mAlphaChooserText;
    private TextView mAlphaChooserTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private int RESULT_LOAD_IMAGE = 1;
    private Dialog mColorChooserDialog;
    private AlertDialog.Builder mColorChooser;

    private static final int[] STATE_DARK_GREEN = {R.attr.state_dark_green};
    private static final int[] STATE_GREEN = {R.attr.state_green};
    private static final int[] STATE_LIGHT_GREEN = {R.attr.state_light_green};
    private static final int[] STATE_RED = {R.attr.state_red};
    private static final int[] STATE_LIGHT_BROWN = {R.attr.state_light_brown};
    private static final int[] STATE_DARK_BROWN = {R.attr.state_dark_brown};
    private static final int[] STATE_BLUE = {R.attr.state_blue};
    private static final int[] STATE_YELLOW = {R.attr.state_yellow};
    private static final int[] STATE_GREY = {R.attr.state_grey};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dnp);

        // Used to set height of color chooser dynamically
        RadioGroup radioGroupColor = (RadioGroup) findViewById(R.id.toggleGroup);
        RelativeLayout colorChooser = (RelativeLayout) findViewById(R.id.color_chooser);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        colorChooser.measure(View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(metrics.heightPixels, View.MeasureSpec.AT_MOST));
        int realWidth = colorChooser.getMeasuredWidth();

        ViewGroup.LayoutParams layoutParams = colorChooser.getLayoutParams();
        Assert.assertNotNull(layoutParams);
        layoutParams.height = realWidth / radioGroupColor.getChildCount();
        colorChooser.setLayoutParams(layoutParams);
        colorChooser.invalidate();
        //----------------------------------------

        // Initialize the drawer menu
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );

        // Set the drawer toggle as the DrawerListener:
        drawerLayout.setDrawerListener(mDrawerToggle);

        // Disable swipe gesture to open and close the drawer:
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        ActionBar actionBar = getActionBar();
        Assert.assertNotNull(actionBar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        //----------------------------------------

        mDrawView = (DrawingView)findViewById(R.id.drawing);

        mBrushSizeChooserText = (TextView)findViewById(R.id.brush_size_chooser_text);
        mBrushSizeChooserTitle = (TextView)findViewById(R.id.brush_size_chooser_title);
        mBrushSizeChooserText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        mBrushSizeChooserTitle.setTextColor(getResources().getColor(android.R.color.darker_gray));
        mBrushSizeChooser = (SeekBar)findViewById(R.id.brush_size_chooser);
        mBrushSizeChooser.setEnabled(false);
        mBrushSizeChooser.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDrawView.setBrushSize(progress * 2);
                mBrushSizeChooserText.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBrushSizeChooser.setProgress(getResources().getInteger(R.integer.initial_size));


        mAlphaChooserText = (TextView)findViewById(R.id.alpha_chooser_text);
        mAlphaChooserTitle = (TextView)findViewById(R.id.alpha_chooser_title);
        mAlphaChooser = (SeekBar)findViewById(R.id.alpha_chooser);
        mAlphaChooser.setEnabled(true);
        mAlphaChooser.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDrawView.setPaintAlpha(progress);
                mAlphaChooserText.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mAlphaChooser.setProgress(getResources().getInteger(R.integer.initial_alpha));

        mColorChooser = new AlertDialog.Builder(this);
        initDnpColorChooser();
    }

    public void paintClickedToggle(View view) {
        String color = view.getTag().toString();
        mDrawView.setColor(color);
    }


    public void hollowClicked(View view) {
        boolean on = ((Switch) view).isChecked();
        if (on) {
            mDrawView.setHollowMode(true);
        } else {
            mDrawView.setHollowMode(false);
        }
    }

     public void eraseClicked(View view) {
        boolean on = ((Switch) view).isChecked();
        if (on) {
            mDrawView.setEraseMode(true);
        } else {
            mDrawView.setEraseMode(false);
        }
    }

    public void touchSizeModeClicked(View view) {
        boolean on = ((Switch) view).isChecked();
        if (on) {
            mDrawView.setTouchSizeMode(true);
            mBrushSizeChooser.setEnabled(false);
            mBrushSizeChooserText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            mBrushSizeChooserTitle.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            mDrawView.setTouchSizeMode(false);
            mBrushSizeChooser.setEnabled(true);
            mBrushSizeChooserText.setTextColor(getResources().getColor(android.R.color.black));
            mBrushSizeChooserTitle.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    public void pressureModeClicked(View view) {
        boolean on = ((Switch) view).isChecked();
        if (on) {
            mDrawView.setPressureMode(true);
            mAlphaChooser.setEnabled(false);
            mAlphaChooserText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            mAlphaChooserTitle.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            mDrawView.setPressureMode(false);
            mAlphaChooser.setEnabled(true);
            mAlphaChooserText.setTextColor(getResources().getColor(android.R.color.black));
            mAlphaChooserTitle.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    @Override
    public void onClick(View view){
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            if (selectedImage != null) {
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                    if (bitmap != null) {
                        mDrawView.loadImage(bitmap);
                        bitmap.recycle();
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.new_btn:
                newDrawing();
                return true;
            case R.id.save_btn:
                saveDrawing();
                return true;
            case R.id.load_btn:
                loadDrawing();
                return true;
            case R.id.sun_btn:
                mColorChooserDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initDnpColorChooser() {
        View dialogView = getLayoutInflater().inflate(R.layout.dnp_color_chooser, null);
        Assert.assertNotNull(dialogView);
        mColorChooser.setView(dialogView);
        mColorChooser.setTitle(R.string.color_chooser_dialog_title);
//        colorChooser.setMessage(R.string.color_chooser_dialog_message);
        final ImageView imageView = (ImageView) dialogView.findViewById(R.id.dnp_color_chooser);
        final ImageView imageViewBackground = (ImageView) dialogView.findViewById(R.id.dnp_color_chooser_background);
        Assert.assertNotNull(imageView);
        mColorChooser.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float touchX = MotionEventCompat.getX(event, MotionEventCompat.getActionIndex(event));
                float touchY = MotionEventCompat.getY(event, MotionEventCompat.getActionIndex(event));

                // Index of multiple touch event:
                int index = MotionEventCompat.getActionIndex(event);
                // Id of multiple touch event
                int id = MotionEventCompat.getPointerId(event, index);

                switch (MotionEventCompat.getActionMasked(event)) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_DOWN:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_CANCEL:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:
                        imageView.setDrawingCacheEnabled(true);
                        Bitmap bitmap = Bitmap.createBitmap(imageView.getDrawingCache());
                        imageView.setDrawingCacheEnabled(false);
                        int pixel = bitmap.getPixel((int) touchX, (int) touchY);
                        bitmap.recycle();
                        int color = Color.argb(255, Color.red(pixel), Color.green(pixel), Color.blue(pixel));
                        String stringColor = String.format("#%06X", 0xFFFFFF & color);
                        switch (color) {
                            case -14391260:
                                // Dark green
                                chooseColor(stringColor, id);
                                imageViewBackground.setImageState(STATE_DARK_GREEN, false);
                                break;
                            case -13463502:
                                // Green
                                chooseColor(stringColor, id);
                                imageViewBackground.setImageState(STATE_GREEN, false);
                                break;
                            case -12137658:
                                // Light green
                                chooseColor(stringColor, id);
                                imageViewBackground.setImageState(STATE_LIGHT_GREEN, false);
                                break;
                            case -2675411:
                                // Red
                                chooseColor(stringColor, id);
                                imageViewBackground.setImageState(STATE_RED, false);
                                break;
                            case -3837106:
                                // Light brown
                                chooseColor(stringColor, id);
                                imageViewBackground.setImageState(STATE_LIGHT_BROWN, false);
                                break;
                            case -7515592:
                                // Dark brown
                                chooseColor(stringColor, id);
                                imageViewBackground.setImageState(STATE_DARK_BROWN, false);
                                break;
                            case -13156710:
                                // Blue
                                chooseColor(stringColor, id);
                                imageViewBackground.setImageState(STATE_BLUE, false);
                                break;
                            case -465067:
                                // Yellow
                                chooseColor(stringColor, id);
                                imageViewBackground.setImageState(STATE_YELLOW, false);
                                break;
                            case -4079167:
                                // Grey
                                chooseColor(stringColor, id);
                                imageViewBackground.setImageState(STATE_GREY, false);
                                break;
                            default:
                                break;
                        }


                        break;

                    default:
                        // Do not consume other events.
                        return false;
                }
                // Consume handled event.
                return true;
            }
        });
        mColorChooserDialog = mColorChooser.create();
    }

    private void chooseColor(String stringColor, int id) {
        mDrawView.setColor(stringColor, id);
//        mColorChooserDialog.dismiss();
    }

    private void loadDrawing() {
        //load drawing
        AlertDialog.Builder loadDialog = new AlertDialog.Builder(this);
        loadDialog.setTitle(R.string.load_dialog_title);
        loadDialog.setMessage(R.string.load_dialog_message);
        loadDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent, RESULT_LOAD_IMAGE);

            }
        });
        loadDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        loadDialog.show();
    }

    private void saveDrawing() {
        //save drawing
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
        saveDialog.setTitle(R.string.save_dialog_title);
        saveDialog.setMessage(R.string.save_dialog_message);
        saveDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                mDrawView.setDrawingCacheEnabled(true);

                String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + "Pictures";
                String fileTitle = UUID.randomUUID().toString();
                String fileName = fileTitle + ".png";
                File image = new File(dirPath, fileName);
                Context applicationContext = getApplicationContext();
                Assert.assertNotNull(applicationContext);
                Bitmap drawingCache = mDrawView.getDrawingCache();
                Assert.assertNotNull(drawingCache);
                boolean error = false;
                try {
                    // Ensure the file has an unique name:
                    while (!image.createNewFile()) {
                        fileTitle = UUID.randomUUID().toString();
                        fileName = fileTitle + ".png";
                        image = new File(dirPath, fileName);
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(image);
                    drawingCache.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();

                    // Update content database:
                    ContentValues values = new ContentValues(7);

                    values.put(MediaStore.Images.Media.TITLE, "DNP-drawing-" + fileTitle);
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.ORIENTATION, 0);
                    values.put(MediaStore.Images.Media.DATA, image.getAbsolutePath());
                    values.put(MediaStore.Images.Media.SIZE, image.length());

                    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                } catch (Exception e) {
                    e.printStackTrace();
                    error = true;
                }

                if (error) {
                    Toast unsavedToast = Toast.makeText(applicationContext,
                            R.string.save_dialog_ko, Toast.LENGTH_SHORT);
                    unsavedToast.show();
                } else {
                    Toast savedToast = Toast.makeText(applicationContext,
                            R.string.save_dialog_ok, Toast.LENGTH_SHORT);
                    savedToast.show();
                }
                mDrawView.destroyDrawingCache();
            }
        });
        saveDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        saveDialog.show();
    }

    private void newDrawing() {
        //new button
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle(R.string.new_dialog_title);
        newDialog.setMessage(R.string.new_dialog_message);
        newDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mDrawView.startNew();
                dialog.dismiss();
            }
        });
        newDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        newDialog.show();

    }

}
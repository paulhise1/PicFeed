package paulhise.picfeed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.w3c.dom.Text;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class NewUserSelfie extends AppCompatActivity {
    // assigning a 'request code' ints to activity for intents
    static final int REQUEST_TAKE_PHOTO = 1;
    private static final String TAG = "NewUserSelfie";

    // assigning member variables
    private Button mTakeSelfie;
    private TextView mInfo;
    private TextView mWelcomeText;
    private File mPhotoFile;
    private String mCurrentPhotoPath;
    private ImageView mSelfie;
    private Button mSetSelfie;
    private Bitmap bmp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_selfie);

        // assign class member button and views
        mTakeSelfie = (Button) findViewById(R.id.user_Selfie_Button);
        mInfo = (TextView) findViewById(R.id.info_NUS);
        mSelfie = (ImageView) findViewById(R.id.selfie_Picture);
        mSetSelfie = (Button) findViewById(R.id.capture_Selfie);

        // setting post picture visibility to gone until a photo is available to post
        mSetSelfie.setVisibility(View.GONE);

        // sets clicklistener to selfie button
        attachOnClickListener();
    }

    // buttton clickListeners
    private void attachOnClickListener() {

        // take selfie photo button clicklistener
        mTakeSelfie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String intentString = "testIntent";
//                sendIntent(intentString);
                dispatchTakePictureIntent();
            }
        });

        // move userSelfie image to photoPickerActivity
        mSetSelfie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String intentString = "testIntent";
//                sendIntent(intentString);
                packageBitmapForIntent(bmp);
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            createPhotoFile();      // Create the File where the photo should go

            // Continue only if the File was successfully created
            if (mPhotoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", mPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // this method provides the logic for the camera's intent that comes back
    // the image is built into a bitmap and displayed if possible
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_TAKE_PHOTO) {

                // Image captured and set to the imageview at the bottom on PhotoPickerActivity
                makeBitmap();

                // Changing button text to redo as opposed to "lets take a selfie" that originally shows to user
                String newSelfieButtonText = "Redo Selfie";
                mTakeSelfie.setText(newSelfieButtonText);
                mSetSelfie.setVisibility(View.VISIBLE);     //displays button to post selfie



            } else {
                // error loading image sets info text image load error
                Log.d(TAG, "onActivityResult: request code error");
                mInfo.setText(R.string.request_code_error);
            }

        } else if (resultCode == RESULT_CANCELED) {
            // User cancelled the image capture, set info text to user cancled.
            Log.d(TAG, "onActivityResult: User canceled request");
            mInfo.setText(R.string.intent_canceled);

        } else {
            // error loading image sets info text image load error
            Log.d(TAG, "onActivityResult: Image load error");
            mInfo.setText(R.string.image_load_error);
        }
    }

    // Try catch block method to create the File where the photo should go
    private void createPhotoFile() {
        mPhotoFile = null;
        try {
            mPhotoFile = createUniqueImageFile();
        } catch (IOException e) {
            String error = "Error: " + e.getMessage();
            Log.d("FILE ERROR", error);
            mInfo.setText(error);
        }
    }

    // provides unique file names for our images to be put into
    private File createUniqueImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());;
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = file.getAbsolutePath();
        return file;
    }

    // method that takes a picture from a file path and decodes it into a bitmap
    // that bitmap is assigned to the member mImageResult to displayed in the imageview
    private void makeBitmap() {
        bmp = BitmapFactory.decodeFile(mCurrentPhotoPath);
        mSelfie.setImageBitmap(bmp);

    }

    // intent to move to PhotoPickerActivity
    private void goToPhotoPickerActivity(){
        Intent toPhotoPicker = new Intent(this, PhotoPickerActivity.class);
        startActivity(toPhotoPicker);
    }

    private void packageBitmapForIntent(Bitmap bmp) {
        try {
            // Write file to put Bitmap into
            String filename = "userImageBitmap";
            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

            // Cleanup stream
            stream.close();
            bmp.recycle();

            // intent to put bitmap into extra to send to PhotoPickerActivity
            Intent sendBitmap = new Intent(this, PhotoPickerActivity.class);
            sendBitmap.putExtra("image", filename);
            startActivity(sendBitmap);
        } catch (Exception e) {
            mInfo.setText(TAG + "errror: " + e.getMessage());
        }
    }

    // test intent to see if i can send a succesful string extra between activities.
    // not being currently used
    private void sendIntent (String s) {
        Intent in1 = new Intent(this, PhotoPickerActivity.class);
        in1.putExtra("intentString", s);
        startActivity(in1);
    }
}

package paulhise.picfeed;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class PhotoPickerActivity extends AppCompatActivity {

    // assigning TAG for PhotoPickerActivity
    private static final String TAG = "PhotoPickerActivity";

    // assigning a 'request code' ints to activity for intents
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_PHOTO_GALLERY = 2;

    // assigning member variables to PhotoPickerActivity class
    private String mCurrentPhotoPath;
    private TextView mInfo;
    private ImageView mUserImage;
    private Button mTakePhoto;
    private Button mPostPicture;
    private Button mSelectFromGallery;
    private ImageView mImageResult;
    private Boolean mShowButton;
    private Intent mGoToPhotoFeedActivity;
    private File mPhotoFile;
    private Intent mGalleryIntent;

    // onCreate method for PhotoPickerActivity class
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);

        // setting the member views for this activity class
        mInfo = (TextView) findViewById(R.id.info);
        mTakePhoto = (Button) findViewById(R.id.takePhotoButton);
        mSelectFromGallery = (Button) findViewById(R.id.selectFromGalleryButton);
        mUserImage = (ImageView) findViewById(R.id.userPicture);
        mImageResult = (ImageView) findViewById(R.id.photoView);
        mPostPicture = (Button) findViewById(R.id.postPictureButton);

        mGoToPhotoFeedActivity = new Intent(this, PhotoFeedActivity.class);
        mGalleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // hiding the post button until there is a picture to post
        mPostPicture.setVisibility(View.INVISIBLE);

        // calling method to activate on click listeners for the buttons in this activity
        attachOnClickListener();

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
                changeButtonVisibility(mPostPicture);

            } else if (requestCode == REQUEST_PHOTO_GALLERY) {
                // intent to get a photo from gallery.
                getPhotoFromGallery(data);
                changeButtonVisibility(mPostPicture);

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

    // this is my clicklistener method that will give the logic for the different buttons
    private void attachOnClickListener(){
        // take photo button
        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        // Post picture button
        mPostPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // needs logic to save the current photo to app to be viewed in photo feed activity

                // initiates intent to go to photo feed activity to view posted photos
                startActivity(mGoToPhotoFeedActivity);
            }
        });

        // Select photo from phone gallery button
        mSelectFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //needs logic to get photos from gallery and pull them into app
                startActivityForResult(mGalleryIntent, REQUEST_PHOTO_GALLERY);
            }
        });

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

    // This activates the intent to take a picture with the camera device
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            createPhotoFile();
            // Continue only if the File was successfully created
            if (mPhotoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", mPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // sets up input stream to be read from returning gallery intent
    // takes the input string and makes a bitmap out of it
    private void getPhotoFromGallery (Intent data) {
        try {
            InputStream stream = this.getContentResolver().openInputStream(data.getData());
            Bitmap galleryPicture = BitmapFactory.decodeStream(stream);
            mImageResult.setImageBitmap(galleryPicture);
        } catch (FileNotFoundException e) {
            mInfo.setText("Error: " + e.getMessage());
        }
    }

    // method that takes a picture from a file path and decodes it into a bitmap
    // that bitmap is assigned to the member mImageResult to displayed in the imageview
    private void makeBitmap() {
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        mImageResult.setImageBitmap(bitmap);
    }

    // method that makes the button to post pictures only visible if there is a
    // photo available to post.
    private void changeButtonVisibility(Button button) {
        button.setVisibility(View.VISIBLE);
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


}



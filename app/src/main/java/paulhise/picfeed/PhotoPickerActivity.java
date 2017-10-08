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
import java.io.IOException;
import java.util.Date;

public class PhotoPickerActivity extends AppCompatActivity {

    // assigning TAG for PhotoPickerActivity
    private static final String TAG = "PhotoPickerActivity";

    // assigning a 'request code' int to request_take_photo
    static final int REQUEST_TAKE_PHOTO = 1;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    // assigning member variables to PhotoPickerActivity class
    private String mCurrentPhotoPath;
    private TextView mInfo;
    private ImageView mUserImage;
    private Button mTakePhoto;
    private Button mPostPicture;
    private Button mSelectFromGallery;
    private ImageView mImageResult;
    private Bitmap mBitmap;
    private Boolean mShowButton;

    // onCreate method for PhotoPickerActivity class
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);

        mInfo = (TextView) findViewById(R.id.info);
        mTakePhoto = (Button) findViewById(R.id.takePhotoButton);
        mSelectFromGallery = (Button) findViewById(R.id.selectFromGalleryButton);
        mUserImage = (ImageView) findViewById(R.id.userPicture);
        mImageResult = (ImageView) findViewById(R.id.photoView);
        mPostPicture = (Button) findViewById(R.id.postPictureButton);
        mShowButton = false;

        attachOnClickListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_TAKE_PHOTO) {
           // Image captured and set to the imageview at the bottom on PhotoPickerActivity
            makeBitmap();
            changeButtonVisibility(mPostPicture);

        } else if (resultCode == RESULT_CANCELED) {
            // User cancelled the image capture
            Log.d(TAG, "onActivityResult: User canceled request");
            mInfo.setText(R.string.intent_canceled);

        } else {
            Log.d(TAG, "onActivityResult: Image load error");
            mInfo.setText(R.string.image_load_error);
        }
    }

    private void attachOnClickListener(){
        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dispatchTakePictureIntent();
            }
        });

        mPostPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // need logic to give photo to new activity called PhotoFeedActivity
            }
        });

        mSelectFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // need logic to take mImageResult and put it into phone photo gallery
            }
        });

    }

    // provides unique file names for our images to be put into
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());;
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // This activates the intent to take a picture with the camera device
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                String error = "Error: " + e.getMessage();
                Log.d("FILE ERROR", error);
                mInfo.setText(error);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void makeBitmap() {
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        mImageResult.setImageBitmap(bitmap);
    }

    private void changeButtonVisibility(Button button) {
        if (mShowButton) {
            button.setVisibility(View.INVISIBLE);
            mShowButton = false;
        } else {
            button.setVisibility(View.VISIBLE);
            mShowButton = true;
        }
    }


}



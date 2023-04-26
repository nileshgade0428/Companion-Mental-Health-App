package com.example.companion.ui.camera;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.companion.R;
import com.google.gson.Gson;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CameraActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;

    Button process, takePicture;
    ImageView imageView, hidden;

    private FaceServiceClient faceServiceClient;
    Bitmap mBitmap;
    Boolean ready = false;
    CoordinatorLayout rel;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        faceServiceClient = new FaceServiceRestClient("https://matrix12345.cognitiveservices.azure.com/", "b0e9110c0f4148e6a3115277b9b648d3");

        takePicture = findViewById(R.id.takePic);
        imageView = findViewById(R.id.imageView);
        hidden = findViewById(R.id.hidden);
        imageView.setVisibility(View.INVISIBLE);

        process = findViewById(R.id.processClick);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                } else {

                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, "New Picture");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                        imageUri = getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent, 100);
                    } else {
                        if (counter == 2 || (counter > 2 && counter % 2 == 0)) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, 1);
                        } else                         //Requesting storage permissions so we can get a high quality image.
                            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        counter++;

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, 100);
                    }
                }
            }
        });

        process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ready) {
                    detectandFrame(mBitmap);
                } else {
                    makeToast("Please take a picture.");
                }
            }
        });
        rel = findViewById(R.id.rel);
    }

    Uri imageUri;
    ContentValues values;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) { //High Quality picture using URI and storage
            if (resultCode == RESULT_OK) {
                imageView.setVisibility(View.VISIBLE);
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imageUri);

                    Bitmap rotatedBitmap = mBitmap;
                    ExifInterface ei = new ExifInterface(getRealPathFromURI(imageUri));
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    switch (orientation) {

                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotatedBitmap = rotateImage(mBitmap, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotatedBitmap = rotateImage(mBitmap, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotatedBitmap = rotateImage(mBitmap, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotatedBitmap = mBitmap;
                    }
                    imageView.setImageBitmap(rotatedBitmap);

                } catch (IOException e) {
                    makeToast("Error: " + e.toString());
                    mBitmap = (Bitmap) data.getExtras().get("data");
                    e.printStackTrace();
                    imageView.setImageBitmap(mBitmap);
                }
                ready = true;
                hidden.setVisibility(View.INVISIBLE);
            }
        } else if (requestCode == CAMERA_REQUEST  && resultCode == RESULT_OK && data != null) {
            imageView.setVisibility(View.VISIBLE);
            mBitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(mBitmap);
            ready = true;
            hidden.setVisibility(View.INVISIBLE);
        }
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void detectandFrame(final Bitmap mBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream((outputStream.toByteArray()));

        @SuppressLint("StaticFieldLeak") AsyncTask<InputStream, String, Face[]> detectTask = new AsyncTask<InputStream, String, Face[]>() {
            final ProgressDialog pd = new ProgressDialog(CameraActivity.this);

            @SuppressLint("DefaultLocale")
            @Override
            protected Face[] doInBackground(InputStream... inputStreams) {

                publishProgress("Detecting...");
                //This is where you specify the FaceAttributes to detect. You can change this for your own use.
                FaceServiceClient.FaceAttributeType[] faceAttr = new FaceServiceClient.FaceAttributeType[]{
                        FaceServiceClient.FaceAttributeType.HeadPose,
                        FaceServiceClient.FaceAttributeType.Age,
                        FaceServiceClient.FaceAttributeType.Gender,
                        FaceServiceClient.FaceAttributeType.Emotion,
                        FaceServiceClient.FaceAttributeType.FacialHair
                };

                try {
                    Face[] result = faceServiceClient.detect(inputStreams[0],
                            true,
                            false,
                            faceAttr);

                    if (result == null) {
                        publishProgress("Detection failed. Nothing detected.");
                    }

                    assert result != null;
                    publishProgress(String.format("Detection Finished. %d face(s) detected", result.length));
                    return result;
                } catch (Exception e) {
                    publishProgress("Detection Failed: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                pd.show();
            }

            @Override
            protected void onProgressUpdate(String... values) {
                pd.setMessage(values[0]);
            }

            @SuppressLint("WrongThread")
            @Override
            protected void onPostExecute(Face[] faces) {
                pd.dismiss();
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                Gson gson = new Gson();
                String data = gson.toJson(faces);
                if (faces == null || faces.length == 0) {
                    makeToast("No faces detected. You may not have added the API Key or try retaking the picture.");
                } else {
                    intent.putExtra("list_faces", data);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    intent.putExtra("image", byteArray);
                    startActivity(intent);
                }

            }
        };
        detectTask.execute(inputStream);
    }

    private void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

}
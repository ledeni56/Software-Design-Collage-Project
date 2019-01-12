package com.example.lovro.myapplication.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.lovro.myapplication.R;
import com.example.lovro.myapplication.domain.Dimension;
import com.example.lovro.myapplication.domain.Offer;
import com.example.lovro.myapplication.domain.Style;
import com.example.lovro.myapplication.network.ScalingUtilities;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.lovro.myapplication.network.InitApiService.apiService;

public class AddOfferActivity extends BasicActivity {

    private static final int REQUEST_CODE_PERMISSION_GALLERY = 1;
    private static final int REQUEST_CODE_PICTURE_FROM_GALLERY = 2;
    private static final int REQUEST_CODE_PERMISSION_CAMERA = 3;
    private static final int REQUEST_IMAGE_CAPTURE = 4;
    private ImageView offerPhoto;
    private Uri offerUriPicture=null;
    private Offer currentOffer=new Offer();

    private EditText offerName;
    private EditText offerSpecification;
    private EditText offerDescription;
    private EditText offerPrice;

    private EditText styleName;
    private EditText stylePrice;
    private Button addStyleButton;

    private EditText dimensionName;
    private Button addDimensionButton;

    private Button uploadOfferButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_offer);
        offerPhoto=findViewById(R.id.imageHolder);
        offerName=findViewById(R.id.offerName);
        offerSpecification=findViewById(R.id.offerSpecivication);
        offerDescription=findViewById(R.id.offerDescription);
        offerPrice=findViewById(R.id.offerPrice);
        stylePrice=findViewById(R.id.stylePrice);
        addStyleButton=findViewById(R.id.addStyleButton);
        dimensionName=findViewById(R.id.dimensionName);
        addDimensionButton=findViewById(R.id.addDimensionButton);
        styleName=findViewById(R.id.styleName);
        uploadOfferButton=findViewById(R.id.uploadOffer);

        initListeners();


    }

    private void initListeners() {
        addStyleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(styleName.getText().toString().equals("") || stylePrice.getText().toString().equals("")){
                    Toast.makeText(AddOfferActivity.this, "Please fill in style details", Toast.LENGTH_SHORT).show();
                }else{
                    if(currentOffer.getStyles()==null){
                        currentOffer.setStyles(new ArrayList<Style>());
                    }
                    currentOffer.addStyle(new Style(styleName.getText().toString(),Double.parseDouble(stylePrice.getText().toString())));
                    Toast.makeText(AddOfferActivity.this, "Style added", Toast.LENGTH_SHORT).show();
                    stylePrice.setText("");
                    styleName.setText("");
                }
            }
        });
        addDimensionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dimensionName.getText().toString().equals("")){
                    Toast.makeText(AddOfferActivity.this, "Please fill in dimension details", Toast.LENGTH_SHORT).show();
                }else{
                    if(currentOffer.getDimensions()==null){
                        currentOffer.setDimensions(new ArrayList<Dimension>());
                    }
                    currentOffer.addDimension(new Dimension(dimensionName.getText().toString()));
                    Toast.makeText(AddOfferActivity.this, "Dimension added", Toast.LENGTH_SHORT).show();
                    dimensionName.setText("");
                }
            }
        });
        uploadOfferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (offerName.getText().toString().equals("") || offerDescription.getText().toString().equals("") || offerSpecification.getText().toString().equals("") || offerPrice.getText().toString().equals("")){
                    Toast.makeText(AddOfferActivity.this, "Please fill in offer details", Toast.LENGTH_SHORT).show();
                }else{
                    currentOffer.setName(offerName.getText().toString());
                    currentOffer.setDescription(offerDescription.getText().toString());
                    currentOffer.setSpecification(offerSpecification.getText().toString());
                    currentOffer.setPrice(Double.valueOf(offerPrice.getText().toString()));
                    if (offerUriPicture==null){
                        Toast.makeText(AddOfferActivity.this, "Please add photo", Toast.LENGTH_SHORT).show();
                    }else if (currentOffer.nothingNull()){
                        show_loading("Uploading offer");
                        //Toast.makeText(AddOfferActivity.this, "everything ok can upload", Toast.LENGTH_SHORT).show();
                        apiService.postOffer(getUserAuth(),currentOffer).enqueue(new Callback<Offer>() {
                            @Override
                            public void onResponse(Call<Offer> call, Response<Offer> response) {
                                stop_loading();
                                if(response.isSuccessful()){
                                    currentOffer=response.body();
                                    //Toast.makeText(AddOfferActivity.this, "Offer uploaded", Toast.LENGTH_SHORT).show();
                                    uploadPhoto();
                                }else{
                                    try {
                                        showError(response.errorBody().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }

                            @Override
                            public void onFailure(Call<Offer> call, Throwable t) {
                                stop_loading();
                                showError(t.getMessage());
                                t.printStackTrace();

                            }
                        });
                    }else{
                        Toast.makeText(AddOfferActivity.this, "Please add styles and dimensions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        offerPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(AddOfferActivity.this);
                dialog.setContentView(R.layout.camra_gallery_picker);
                Button galleryButton = dialog.findViewById(R.id.galleryButton);
                Button cameraButton = dialog.findViewById(R.id.cameraButton);

                cameraButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (ActivityCompat.checkSelfPermission(AddOfferActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_CAMERA);
                        } else {
                            loadFromCamera();
                        }
                    }
                });

                galleryButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (ActivityCompat.checkSelfPermission(AddOfferActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_GALLERY);
                        } else {
                            loadFromGallery();
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    private void uploadPhoto() {
        show_loading("Uploading image");
        File file = new File(decodeFile(offerUriPicture.getPath(),500,500));
        apiService.uploadAdvertisementImage(getUserAuth(),String.valueOf(currentOffer.getId()),RequestBody.create(MediaType.parse("image/jpg"), file)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                stop_loading();
                if(response.isSuccessful()){
                    Toast.makeText(AddOfferActivity.this, "Offer uploaded", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        showError(response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                stop_loading();
                showError(t.getMessage());
                t.printStackTrace();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICTURE_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri result = data.getData();
            Picasso.get().load(result).into(offerPhoto);
            offerUriPicture = Uri.fromFile(new File(getRealPathFromUri(AddOfferActivity.this, result)));

        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Picasso.get().load(offerUriPicture).into(offerPhoto);
        }
    }

    private void loadFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK);

        String picturesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        Uri data = Uri.parse(picturesPath);

        i.setDataAndType(data, "image/*");
        startActivityForResult(i, REQUEST_CODE_PICTURE_FROM_GALLERY);
    }

    private void loadFromCamera() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(AddOfferActivity.this.getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(AddOfferActivity.this, "com.example.android.crafteryFileProvider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(pictureIntent,
                        REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        offerUriPicture = Uri.fromFile(image);
        return image;
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CODE_PERMISSION_GALLERY) {
                loadFromGallery();
            } else if (requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
                loadFromCamera();
            }
        } else {
            Toast.makeText(AddOfferActivity.this, "Need permission to do that action", Toast.LENGTH_SHORT);
        }
    }

    private String decodeFile(String path,int DESIREDWIDTH, int DESIREDHEIGHT) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);

            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
            } else {
                unscaledBitmap.recycle();
                return path;
            }

            // Store to tmp file

            String extr = Environment.getExternalStorageDirectory().toString();
            File mFolder = new File(extr + "/TMMFOLDER");
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }

            String s = "tmp.png";

            File f = new File(mFolder.getAbsolutePath(), s);

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (Exception e) {

                e.printStackTrace();
            }

            scaledBitmap.recycle();
        } catch (Throwable e) {
        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;

    }



}
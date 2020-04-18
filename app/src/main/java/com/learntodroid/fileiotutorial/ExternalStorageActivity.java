package com.learntodroid.fileiotutorial;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ExternalStorageActivity extends AppCompatActivity {
    private static final int CREATE_FILE = 1;

    private ToggleButton fileType;
    private EditText fileName, fileContents, imageUri;

    private RecyclerView imagesRecyclerView;
    private GalleryRecyclerAdapter galleryRecyclerAdapter;
    private List<Image> imageList;

    private Uri documentUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_externalstorage);

        fileName = findViewById(R.id.activity_externalstorage_filename);
        fileContents = findViewById(R.id.activity_externalstorage_filecontents);
        imageUri = findViewById(R.id.activity_externalstorage_imageUri);

        fileType = findViewById(R.id.activity_externalstorage_filetype);
        fileType.setChecked(true);

        imagesRecyclerView = findViewById(R.id.activity_externalstorage_imagesRecyclerView);
        galleryRecyclerAdapter = new GalleryRecyclerAdapter();
        imagesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imagesRecyclerView.setAdapter(galleryRecyclerAdapter);

        findViewById(R.id.activity_externalstorage_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeAppSpecificExternalFile(getApplicationContext(), fileType.isChecked());
            }
        });

        findViewById(R.id.activity_externalstorage_read).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readAppSpecificExternalFile(getApplicationContext(), fileType.isChecked());
            }
        });

        findViewById(R.id.activity_externalstorage_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileContents.setText("");
            }
        });

        findViewById(R.id.activity_externalstorage_saveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeMediaStoreFile(getApplicationContext());
            }
        });

        findViewById(R.id.activity_externalstorage_readImages).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readMediaStoreFile(getApplicationContext());
            }
        });

        findViewById(R.id.activity_externalstorage_createDocument).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeStorageAccessFrameworkFile(getApplicationContext());
            }
        });

        findViewById(R.id.activity_externalstorage_readDocument).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readStorageAccessFrameworkFile(getApplicationContext());
            }
        });
    }

    private void writeAppSpecificExternalFile(Context context, boolean isPersistent) {
        File file;
        if (isPersistent) {
            file = new File(context.getExternalFilesDir(null), fileName.getText().toString());
        } else {
            file = new File(context.getExternalCacheDir(), fileName.getText().toString());
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(fileContents.getText().toString().getBytes(Charset.forName("UTF-8")));
            Toast.makeText(context, String.format("Write to %s successful", fileName.getText().toString()), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, String.format("Write to file %s failed", fileName.getText().toString()), Toast.LENGTH_SHORT).show();
        }
    }

    private void readAppSpecificExternalFile(Context context, boolean isPersistent) {
        File file;
        if (isPersistent) {
            file = new File(context.getExternalFilesDir(null), fileName.getText().toString());
        } else {
            file = new File(context.getExternalCacheDir(), fileName.getText().toString());
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
            List<String> lines = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            fileContents.setText(TextUtils.join("\n", lines));
            Toast.makeText(context, String.format("Read from file %s successful", fileName.getText().toString()), Toast.LENGTH_SHORT).show();
       } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, String.format("Read from file %s failed", fileName.getText().toString()), Toast.LENGTH_SHORT).show();
            fileContents.setText("");
        }
    }

    private void writeMediaStoreFile(final Context context) {
        Glide.with(context)
                .asBitmap()
                .load(imageUri.getText().toString())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName.getText().toString());
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                        Uri localImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                        if (localImageUri == null) {
                            Toast.makeText(context, "Failed to create new MediaStore record",  Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Created new MediaStore record",  Toast.LENGTH_SHORT).show();
                            try {
                                OutputStream outputStream = getContentResolver().openOutputStream(localImageUri);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                outputStream.close();
                                Toast.makeText(context, "Bitmap created",  Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Failed to store bitmap",  Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void readMediaStoreFile(Context context) {
        imageList = new ArrayList<Image>();

        String[] projection = new String[] {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.SIZE
        };
        String selection = MediaStore.Images.Media.DATE_ADDED + " >= ?";
        String[] selectionArgs = new String[] { String.valueOf(1586180938) };
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );

        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
        int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(idColumn);
            String name = cursor.getString(nameColumn);
            int size = cursor.getInt(sizeColumn);
            Log.i("image", name);
            Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            imageList.add(new Image(contentUri, name, size));
        }
        galleryRecyclerAdapter.setImages(imageList);
    }

    private void writeStorageAccessFrameworkFile(Context context) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, fileName.getText().toString());
        startActivityForResult(intent, CREATE_FILE);
    }

    private void readStorageAccessFrameworkFile(Context context) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(documentUri);
            List<String> lines = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            fileContents.setText(TextUtils.join("\n", lines));
            Toast.makeText(context, String.format("Read from file %s successful", fileName.getText().toString()), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_FILE && data != null) {
            Toast.makeText(getApplicationContext(), "Document successfully created", Toast.LENGTH_SHORT).show();
            try {
                documentUri = data.getData();
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(data.getData(), "w");
                FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                fileOutputStream.write(("Overwritten at " + System.currentTimeMillis() + "\n").getBytes());
                fileOutputStream.close();
                pfd.close();
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Document not written", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

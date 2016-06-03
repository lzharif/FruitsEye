package com.luzharif.fruitseye;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String folderCitra = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/Fruits Eye";
    private static final String TAG = "MainActivity";
    private String imageName;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPrefEditor;

    private boolean isFirstTime;

    int jenisBuah;

    static {
//        System.loadLibrary("opencv_java3");
//        System.loadLibrary("gnustl_shared");
    }


    static final int REQUEST_IMAGE_CAPTURE = 1;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restorePreferences();

        if (isFirstTime) {
            //TODO cek apakah berhasil atau tidak
            copyAssets();
        }

        File file = new File(folderCitra);
        if (!file.exists())
            if (!file.mkdirs())
                Log.e(TAG, "Gagal membuat folder");

        jenisBuah = 0;

        Spinner spinnerFruit = (Spinner) findViewById(R.id.spinner_fruits);
        ArrayAdapter<CharSequence> adapterFruit = ArrayAdapter.createFromResource(this,
                R.array.fruits_array, android.R.layout.simple_spinner_item);
        adapterFruit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFruit.setAdapter(adapterFruit);
        spinnerFruit.setOnItemSelectedListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabambilcitra);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera(jenisBuah);
            }
        });
    }

    /**
     * Mengkopi beragam file yang ada di folder Assets ke memori eksternal Android
     */
    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(folderCitra, filename);

                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;

                //Menandakan penyimpanan sudah selesai
                sharedPrefEditor.putBoolean("isfirsttime", false);
                sharedPrefEditor.commit();
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    }

    /**
     * Fungsi untuk buffer penulisan file
     * @param in inputstream dari Assets
     * @param out outputstream ke folder memori eksternal
     */
    private void copyFile(InputStream in, OutputStream out) {
        byte[] buffer = new byte[1024];
        int read;
        try {
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restorePreferences() {
        sharedPreferences = getSharedPreferences("data_fruits_eye", 0);
        sharedPrefEditor = sharedPreferences.edit();
        isFirstTime = sharedPreferences.getBoolean("isfirsttime", true);
    }

    private void openCamera(int buah) {
        Intent bukaKameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (bukaKameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                bukaKameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(bukaKameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//
//
//            Mat citraKamera = new Mat();
//            Utils.bitmapToMat(imageBitmap, citraKamera);
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
//            String waktu = sdf.format(new Date());
//
//            String filename = folderCitra + "/" + waktu + ".jpg";
//            if(Imgcodecs.imwrite(filename, citraKamera))
//                Log.d(TAG, "Bitmap saved");
            Bundle info = new Bundle();
            info.putString("filename", imageName);
            info.putInt("jenis_buah", jenisBuah);
            Intent startFruitsCamera = new Intent(this, CaptureFruitActivity.class);
            startFruitsCamera.putExtra("bundle", info);
            startActivity(startFruitsCamera);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        imageName = Environment.getExternalStorageDirectory() + File.separator
                + "/Fruits Eye/" + timeStamp + ".jpg";
        File image = new File(imageName);
//        File storageDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "legit";
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
        return image;
    }

    public boolean hasPermissionInManifest(Context context, String permissionName) {
        final String packageName = context.getPackageName();
        try {
            final PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            final String[] declaredPermisisons = packageInfo.requestedPermissions;
            if (declaredPermisisons != null && declaredPermisisons.length > 0) {
                for (String p : declaredPermisisons) {
                    if (p.equals(permissionName)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {

        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        jenisBuah = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}

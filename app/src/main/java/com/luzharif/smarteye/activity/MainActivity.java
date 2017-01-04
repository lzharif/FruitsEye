package com.luzharif.smarteye.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
//import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.luzharif.smarteye.helper.DatabaseHandler;
import com.luzharif.smarteye.helper.DividerItemDecoration;
import com.luzharif.smarteye.R;
import com.luzharif.smarteye.helper.RealPathUtil;
import com.luzharif.smarteye.model.Shots;
import com.luzharif.smarteye.adapter.ShotsAdapter;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private List<Shots> shotsList = new ArrayList<>();
    private RecyclerView recyclerViewShots;
    private ShotsAdapter shotsAdapter;
    private DatabaseHandler db;
    private String folderCitra = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/Fruits Eye";
    private static final String TAG = "MainActivity";
    private String imageName;
    private String name;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPrefEditor;

    private boolean isFirstTime;

    int jenisBuah;

//    static {
////        System.loadLibrary("opencv_java3");
////        System.loadLibrary("gnustl_shared");
//    }


    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_LOAD_FILE = 2;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private int condition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restorePreferences();

        db = new DatabaseHandler(this);
        recyclerViewShots = (RecyclerView) findViewById(R.id.recycler_view_fruits_list);
        shotsList = db.getAllShots();
        shotsAdapter = new ShotsAdapter(shotsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewShots.setLayoutManager(mLayoutManager);
        recyclerViewShots.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerViewShots.setItemAnimator(new DefaultItemAnimator());
        recyclerViewShots.setAdapter(shotsAdapter);

        if (isFirstTime) {
            //Set listener
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }

        //Check Permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File file = new File(folderCitra);
            if (!file.exists())
                if (!file.mkdirs())
                    Log.e(TAG, "Gagal membuat folder");
            copyAssets();
        }
        else
            Toast.makeText(MainActivity.this,
                    "Please approve the permission first", Toast.LENGTH_SHORT).show();


        jenisBuah = 0;

        Spinner spinnerFruit = (Spinner) findViewById(R.id.spinner_fruits);
        ArrayAdapter<CharSequence> adapterFruit = ArrayAdapter.createFromResource(this,
                R.array.fruits_array, android.R.layout.simple_spinner_item);
        adapterFruit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFruit.setAdapter(adapterFruit);
        spinnerFruit.setOnItemSelectedListener(this);

        FloatingActionsMenu fabaction = (FloatingActionsMenu) findViewById(R.id.start_action);
        FloatingActionButton fabTake = (FloatingActionButton) findViewById(R.id.action_takepicture);
        FloatingActionButton fabLoad = (FloatingActionButton) findViewById(R.id.action_loadfile);
        fabTake.setOnClickListener(new View.OnClickListener()

                                   {
                                       @Override
                                       public void onClick(View v) {
                                           condition = 1;
                                           openCamera();
                                       }
                                   }

        );
        fabLoad.setOnClickListener(new View.OnClickListener()

                                   {
                                       @Override
                                       public void onClick(View v) {
                                           condition = 1;
                                           loadFile();
                                       }
                                   }

        );
    }

    @Override
    protected void onResume() {
        if (condition == 0) {
            //Get list of shots
            shotsList = db.getAllShots();
            recyclerViewShots.setAdapter(new ShotsAdapter(shotsList));
        } else {
        }
        super.onResume();
    }

    /**
     * Mengkopi beragam file yang ada di folder Assets ke memori eksternal Android
     */
    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = {"model_fruit.xml", "tomato_6_fann.net"};
        //            files = assetManager.list("");
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
                //Menandakan penyimpanan masih belum selesai
                sharedPrefEditor.putBoolean("isfirsttime", true);
                sharedPrefEditor.commit();
            }
        }
    }

    /**
     * Fungsi untuk buffer penulisan file
     *
     * @param in  inputstream dari Assets
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
        condition = 0;
        sharedPreferences = getSharedPreferences("data_fruits_eye", 0);
        sharedPrefEditor = sharedPreferences.edit();
        isFirstTime = sharedPreferences.getBoolean("isfirsttime", true);
    }

    private void openCamera() {
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

    private void loadFile() {
        Intent bukaFileIntent = new Intent();
        bukaFileIntent.setType("image/*");
        bukaFileIntent.setAction(Intent.ACTION_GET_CONTENT);
        bukaFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(bukaFileIntent, REQUEST_LOAD_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            boolean valid = true;
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                valid = true;
            } else if (requestCode == REQUEST_LOAD_FILE) {
                String realPath;
                    // SDK >= 11 && SDK < 19
                if (Build.VERSION.SDK_INT < 19)
                    realPath = RealPathUtil.getRealPathFromURI_API11to18(this, data.getData());

                    // SDK > 19 (Android 4.4)
                else
                    realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());
//                Uri file = data.getData();
//                if (file.getLastPathSegment().endsWith("png")) {
                if (realPath.endsWith("png")) {
                    imageName = realPath;
                    name = realPath;
                    valid = true;
                } else
                    valid = false;
            }

            if (valid) {
                Bundle info = new Bundle();
                info.putString("filename", imageName);
                info.putInt("jenis_buah", jenisBuah);
                info.putString("name", name);
                Intent startFruitsCamera = new Intent(this, CaptureImageActivity.class);
                startFruitsCamera.putExtra("bundle", info);
                condition = 0;
                startActivity(startFruitsCamera);
            } else
                Toast.makeText(MainActivity.this, "Not an png file!", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        name = timeStamp;
        imageName = Environment.getExternalStorageDirectory() + File.separator
                + "/Fruits Eye/" + timeStamp + ".png";
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
        if (id == R.id.action_reset) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Reset Database?");
            alert.setMessage("If you click yes, previous data will be erased. Are you sure?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.resetTables();
                            shotsList = db.getAllShots();
                            recyclerViewShots.setAdapter(new ShotsAdapter(shotsList));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = alert.create();
            alertDialog.show();

            return true;
        }
        if (id == R.id.action_delete) {
            final File check = new File(folderCitra + "/Fruit Data.txt");
            if(check.exists()) {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Erase Database?");
                alert.setMessage("This will erase Database in \"Fruit Data.txt\". Are you sure?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean delete = check.delete();
                                Toast.makeText(MainActivity.this, "Fruit Data.txt erased successfully",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alert.create();
                alertDialog.show();
            }
            else {
                Toast.makeText(MainActivity.this, "There is no Database in this storage.",
                        Toast.LENGTH_SHORT).show();
            }


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

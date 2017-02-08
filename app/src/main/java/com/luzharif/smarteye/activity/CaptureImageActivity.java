package com.luzharif.smarteye.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.luzharif.smarteye.helper.DatabaseHandler;
import com.luzharif.smarteye.helper.FeatureExtraction;
import com.luzharif.smarteye.helper.FloodFillDetector;
import com.luzharif.smarteye.R;
import com.luzharif.smarteye.helper.KlasifikasiKemasakan;
import com.luzharif.smarteye.model.Shots;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * Created by LuZharif on 24/04/2016.
 */
public class CaptureImageActivity extends AppCompatActivity implements View.OnTouchListener {
    private boolean mIsColorSelected = false;
    private Mat mRgba;
    private Mat mRgbaOri;
    private Mat mProcess;
    private Mat mLab;
    private Mat RgbAndroid;
    private Mat mask;
    private Mat hasil;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private FloodFillDetector mDetector;
    private String filename;
    private String name;
    private int valueChannelH;
    private int valueChannelS;
    private int valueChannelV;
    private String kemasakan;
    private int jenisBuah;
    private boolean isSegmented;
    private float[] dataHasil;

    private ImageView ivCitra;
    private SeekBar seekBarH;
    private SeekBar seekBarS;
    private SeekBar seekBarV;
    private TextView textViewH;
    private TextView textViewS;
    private TextView textViewV;

    private Bitmap citraBitmap;
    private Bitmap orientedBitmap;
    private String folderCitra = Environment.getExternalStorageDirectory().getPath() +
            "/Fruits Eye";
    private String dataTemplateFruit = "MLP_Data_Template.arff";

    private static final String TAG = "Fruits Eye -> CaptureFruitActivity";

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
        try {
            System.loadLibrary("opencv_java3");
            System.loadLibrary("gnustl_shared");
            System.loadLibrary("fann-test");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private EditText edittxtBrix;
    private String nameFix;

    public native int Fann(float[] dataFitur, int jenisBuah);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capturefruit);

        //initialization
        kemasakan = "";
        valueChannelH = 0;
        valueChannelS = 0;
        valueChannelV = 0;

        ivCitra = (ImageView) findViewById(R.id.citra_edit);
        seekBarH = (SeekBar) findViewById(R.id.seekBar_H);
        seekBarS = (SeekBar) findViewById(R.id.seekBar_S);
        seekBarV = (SeekBar) findViewById(R.id.seekBar_V);
        textViewH = (TextView) findViewById(R.id.textView_seek_H);
        textViewS = (TextView) findViewById(R.id.textView_seek_S);
        textViewV = (TextView) findViewById(R.id.textView_seek_V);

        Bundle ambilInfo = getIntent().getBundleExtra("bundle");
        filename = ambilInfo.getString("filename");
        jenisBuah = ambilInfo.getInt("jenis_buah");
        name = ambilInfo.getString("name");

//        try {
//            mRgba = Utils.loadResource(this, R.drawable.tomatlegit, Imgcodecs.CV_LOAD_IMAGE_COLOR);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        isSegmented = false;

        mRgba = new Mat();
        mProcess = Mat.zeros(mRgba.rows(), mRgba.cols(), CvType.CV_8UC3);
        mLab = new Mat();
        RgbAndroid = new Mat();
        mRgbaOri = new Mat();
        hasil = Mat.zeros(mRgba.rows(), mRgba.cols(), CvType.CV_8UC3);

        mDetector = new FloodFillDetector();
        if (filename != null) {
            mRgba = Imgcodecs.imread(filename);

            Imgproc.resize(mRgba, mRgba, new Size(480, 640), 0, 0, Imgproc.INTER_CUBIC); //Size itu untuk Portrait
            citraBitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGRA2BGR);
            Imgproc.cvtColor(mRgba, mLab, Imgproc.COLOR_BGR2HSV);
            mRgba.copyTo(mRgbaOri);
            Imgproc.cvtColor(mRgba, RgbAndroid, Imgproc.COLOR_BGRA2RGB);
            Utils.matToBitmap(RgbAndroid, citraBitmap);
            ivCitra.setImageBitmap(citraBitmap);
            ivCitra.setOnTouchListener(this);
        }

        seekBarH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valueChannelH = progress;
                thresholdImage(valueChannelH, valueChannelS, valueChannelV);
                textViewH.setText("" + valueChannelH);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valueChannelS = progress;
                thresholdImage(valueChannelH, valueChannelS, valueChannelV);
                textViewS.setText("" + valueChannelS);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valueChannelV = progress;
                thresholdImage(valueChannelH, valueChannelS, valueChannelV);
                textViewV.setText("" + valueChannelV);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void thresholdImage(int L, int A, int B) {
        Mat mask = new Mat(mRgba.rows(), mRgba.cols(), CvType.CV_8UC1, new Scalar(0));
        int sensitivity = 15;
        int LHigh = L + sensitivity;
        if (LHigh > 180)
            LHigh = 180;
        Scalar scalarLower = new Scalar(L, A, B);
        Scalar scalarHigher = new Scalar(LHigh, 255, 255);
        Core.inRange(mLab, scalarLower, scalarHigher, mask);
        mRgbaOri.copyTo(hasil, mask);
        mRgbaOri.copyTo(mProcess, mask);
        Imgproc.cvtColor(mProcess, RgbAndroid, Imgproc.COLOR_BGRA2RGB);
        Utils.matToBitmap(RgbAndroid, citraBitmap);
        ivCitra.setImageBitmap(citraBitmap);
        mProcess = Mat.zeros(mRgba.rows(), mRgba.cols(), CvType.CV_8UC3);
        isSegmented = true;
//        String filenamebaru = folderCitra + "/Mask loh.png";
//        Imgcodecs.imwrite(filenamebaru, mask);
    }

    @Override
    public void onBackPressed() {
        String buah;
        if (nameFix != null) {
            switch (jenisBuah) {
                case 0:
                    buah = "Tomato";
                    break;
                case 1:
                    buah = "Watermelon";
                    break;
                case 2:
                    buah = "Orange";
                    break;
                default:
                    buah = "Tomato";
            }
            DatabaseHandler db = new DatabaseHandler(this);
            db.addShot(new Shots(nameFix, buah, kemasakan, filename));
        }
        CaptureImageActivity.super.onBackPressed();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        int cols = mRgba.cols();
        int rows = mRgba.rows();
        int x = (int) event.getX();
        int y = (int) event.getY();

        if ((x < 0) || (y < 0) || (x > ivCitra.getWidth()) || (y > ivCitra.getHeight()))
            return false;

        int projectedX = (int) ((double) x * ((double) cols / (double) ivCitra.getWidth()));
        int projectedY = (int) ((double) y * ((double) rows / (double) ivCitra.getHeight()));

        int pixelValueH = (int) mLab.get(projectedY, projectedX)[0];

        thresholdImage(pixelValueH, 0, 0);
        seekBarH.setProgress(pixelValueH);
        textViewH.setText("" + pixelValueH);
//        mask = mDetector.process(mRgba, projectedX, projectedY); //Aktifkan jika butuh menggunakan FloodFill
//        Rect kotak = new Rect(1, 1, mRgba.width(), mRgba.height());
//        Mat cropped = new Mat(mask, kotak);
//        mRgbaOri.copyTo(hasil, cropped);
//
//        String filenamebaru = folderCitra + "/hasilbaru.png";
//        Imgcodecs.imwrite(filenamebaru, hasil);
//        Imgproc.cvtColor(hasil, RgbAndroid, Imgproc.COLOR_BGRA2RGB);
//        Utils.matToBitmap(RgbAndroid, citraBitmap);
//        ivCitra.setImageBitmap(citraBitmap);
//
//        isSegmented = true;

        return false;
    }

    public void processImage(View view) {
        if (isSegmented) {
            BackgroundTask task = new BackgroundTask(CaptureImageActivity.this);
            task.execute();
        } else
            Toast.makeText(CaptureImageActivity.this,
                    "Please choose the region first by clicking the image or seek bar.",
                    Toast.LENGTH_SHORT).show();
    }

    private void saveData(String kemasakan, float[] dataHasil) {
        String nameText = "Fruit Data.txt";
        File fruitData = new File(folderCitra);
        File file = new File(fruitData, nameText);
        File namef = new File(filename);
        nameFix = namef.getName();
        kemasakan = edittxtBrix.getText().toString();
        if (file.exists()) {
            try {
                FileWriter writer = new FileWriter(file, true);
                writer.append(name + "\t" + dataHasil[0] + "\t" + dataHasil[1] + "\t" +
                        dataHasil[2] + "\t" + dataHasil[3] + "\t" + dataHasil[4] + "\t" +
                        dataHasil[5] + "\t" + dataHasil[6] + "\t" + dataHasil[7] + "\t" +
                        dataHasil[8] + "\t" + dataHasil[9] + "\t" + dataHasil[10] + "\t" +
                        dataHasil[11] + "\t" + kemasakan + "\n");
                writer.flush();
                writer.close();
                Toast.makeText(CaptureImageActivity.this, "Data has been saved!",
                        Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                FileWriter writer = new FileWriter(file, true);
                writer.append("Image\t" + "Area\t" + "Perimeter\t" + "LChannel\t" + "AChannel\t" +
                        "BChannel\t" + "CChannel\t" + "Granularity\t" + "Circularity\t" + "Entropy\t" +
                        "Energy\t" + "Contrast\t" + "Homogenity\t" + "Quality\n");
                writer.append(name + "\t" + dataHasil[0] + "\t" + dataHasil[1] + "\t" +
                        dataHasil[2] + "\t" + dataHasil[3] + "\t" + dataHasil[4] + "\t" +
                        dataHasil[5] + "\t" + dataHasil[6] + "\t" + dataHasil[7] + "\t" +
                        dataHasil[8] + "\t" + dataHasil[9] + "\t" + dataHasil[10] + "\t" +
                        dataHasil[11] + "\t" + kemasakan + "\n");
                writer.flush();
                writer.close();
                Toast.makeText(CaptureImageActivity.this,
                        "Data has been saved with name Fruit Data.txt",
                        Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        public BackgroundTask(CaptureImageActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Processing...");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            final Dialog resultDialog = new Dialog(CaptureImageActivity.this);
            resultDialog.setContentView(R.layout.dialog_result);
            edittxtBrix = (EditText) resultDialog.findViewById(R.id.brix_text);
            TextView txtL = (TextView) resultDialog.findViewById(R.id.L_text);
            TextView txtA = (TextView) resultDialog.findViewById(R.id.A_text);
            TextView txtB = (TextView) resultDialog.findViewById(R.id.B_text);
            TextView txtC = (TextView) resultDialog.findViewById(R.id.C_text);
            TextView txtEntropi = (TextView) resultDialog.findViewById(R.id.entropi_text);
            TextView txtEnergi = (TextView) resultDialog.findViewById(R.id.energi_text);
            TextView txtKontras = (TextView) resultDialog.findViewById(R.id.kontras_text);
            TextView txtHomogen = (TextView) resultDialog.findViewById(R.id.homogen_text);
            ImageView imageView = (ImageView) resultDialog.findViewById(R.id.imageFruit_Result);
            imageView.setVisibility(View.GONE);
            Button buttonSave = (Button) resultDialog.findViewById(R.id.button_save);
            Button buttonGoBack = (Button) resultDialog.findViewById(R.id.button_go_back);
            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        saveData(kemasakan, dataHasil);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(CaptureImageActivity.this, "Save Error",
                                Toast.LENGTH_SHORT).show();
                    }
                    resultDialog.dismiss();
                }
            });
            buttonGoBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resultDialog.dismiss();
                }
            });

            edittxtBrix.setText("" + kemasakan);
            txtL.setText("L : " + dataHasil[2]);
            txtA.setText("A : " + dataHasil[3]);
            txtB.setText("B : " + dataHasil[4]);
            txtC.setText("C : " + dataHasil[5]);
            txtEntropi.setText("Entropy : " + dataHasil[8]);
            txtEnergi.setText("Energy : " + dataHasil[9]);
            txtKontras.setText("Contrast : " + dataHasil[10]);
            txtHomogen.setText("Homogenity : " + dataHasil[11]);

            resultDialog.show();
            resultDialog.setTitle("Results");
            resultDialog.setCancelable(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                FeatureExtraction fe = new FeatureExtraction();
                dataHasil = fe.extraction(jenisBuah, hasil);
                KlasifikasiKemasakan klasifikasiKemasakan = new KlasifikasiKemasakan(getApplicationContext());
                kemasakan = klasifikasiKemasakan.dapatkanKemasakan(dataHasil[5]);
//                kemasakan = klasifikasiWeka(konstantaC);
//                Fann fann = new Fann(modelFruit);
//                float[] qualityArray = fann.run(dataHasil);
//                float maxQuality = qualityArray[0];
//                for (int i = 0; i < qualityArray.length; i++) {
//                    if (qualityArray[i] > maxQuality) {
//                        maxQuality = qualityArray[i];
//                        kemasakan = i + 1;
//                    }
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    private float kalkulasiKonstantaC() {
        float A2 = (float) Math.pow(dataHasil[3], 2);
        float B2 = (float) Math.pow(dataHasil[4], 2);
        float C2 = A2 + B2;
        return (float) Math.pow(C2, 0.5);
    }

    private String klasifikasiWeka(float C) throws IOException {
        InputStream is = null;
        Object ois = null;
        Instances testInstance = null;
        double labelIndex = 0;
        String label = "Red";

        // Read Model MLP from Asset (Worked!)
        try {
            is = getAssets().open("MLP_37.model");
            ois = new ObjectInputStream(is).readObject();
        } catch (IOException e) {
            Toast.makeText(this, "Gagal mbuh", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(this, "Gagal Load Data", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        MultilayerPerceptron mlp = (MultilayerPerceptron) ois;

        // Read data template

        // Start by changing dataset with new input (Worked!)
        File file = new File(folderCitra, dataTemplateFruit);

        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(file, "rw");
            byte b;
            long length = f.length() - 1;
            do {
                length -= 1;
                f.seek(length);
                b = f.readByte();
            } while (b != 10);
            f.setLength(length + 1);
            f.writeChars(C + ",2");
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Now read the modified dataset
        ArffLoader atf = new ArffLoader();
        try {
            atf.setFile(file);
            // Set test instances
            if (atf != null) {
                testInstance = atf.getDataSet();
            }

            if (mlp != null) {
                // Classify using Model
                labelIndex = mlp.classifyInstance(testInstance.firstInstance());
            }
        } catch (IOException e) {
            Log.e("GALAT BACA TEMPLATE", "GALAT 1");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("GALAT BACA DATA", "GALAT 1");
            e.printStackTrace();
        }
        // Return the string of kemasakan
        return testInstance.classAttribute().value((int) labelIndex);
    }
}

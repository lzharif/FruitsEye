package com.luzharif.fruitseye;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.googlecode.fannj.Fann;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by LuZharif on 24/04/2016.
 */
public class CaptureFruitActivity extends AppCompatActivity implements View.OnTouchListener {
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
    private int quality;
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
    private String modelFruit = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/Fruits Eye/tomato_6_fann.net";

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
        System.loadLibrary("ampas");
        System.loadLibrary("opencv_java3");
        System.loadLibrary("gnustl_shared");
    }




    public native int kenaliKualitas(long dataFitur, int jenisBuah);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capturefruit);

        //initialization
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

    private void thresholdImage(int L, int A, int B) {
        Mat mask = new Mat(mRgba.rows(),mRgba.cols(),CvType.CV_8UC1, new Scalar(0));
        int sensitivity  = 15;
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
        switch (jenisBuah) {
            case 0:
                buah = "tomat";
                break;
            case 1:
                buah = "semangka";
                break;
            case 2:
                buah = "burjo";
                break;
            default:
                buah = "tomat";
        }
        DatabaseHandler db = new DatabaseHandler(this);
        db.addShot(new Shots(name, buah, quality, filename));
        CaptureFruitActivity.super.onBackPressed();
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
        if(isSegmented) {
            BackgroundTask task = new BackgroundTask(CaptureFruitActivity.this);
            task.execute();
//            txtLuas.setText("Luas : " + (int) dataHasil.get(0,0)[0]);
//            txtKeliling.setText("Keliling : " + (int) dataHasil.get(0,1)[0]);
//            txtL.setText("L : " + dataHasil.get(0,2)[0]);
//            txtA.setText("A : " + dataHasil.get(0,3)[0]);
//            txtB.setText("B : " + dataHasil.get(0,4)[0]);
//            txtGranul.setText("Granularity : " + dataHasil.get(0,5)[0]);
//            txtCircular.setText("Circularity : " + dataHasil.get(0,6)[0]);
//            txtEntropi.setText("Entropi : " + dataHasil.get(0,7)[0]);
//            txtEnergi.setText("Energi : " + dataHasil.get(0,8)[0]);
//            txtKontras.setText("Kontras : " + dataHasil.get(0,9)[0]);
//            txtHomogen.setText("Homogenitas : " + dataHasil.get(0,10)[0]);
        }
        else
            Toast.makeText(CaptureFruitActivity.this,
                    "Please choose the region first by clicking the image or seek bar.",
                    Toast.LENGTH_SHORT).show();
    }

    private void saveData(int quality, float[] dataHasil) {
        String nameText = "Fruit Data.txt";
        File fruitData = new File(folderCitra);
        File file = new File(fruitData, nameText);
        if(file.exists()) {
            try {
                FileWriter writer = new FileWriter(file, true);
                writer.append(dataHasil[0] + "\t" + dataHasil[1] + "\t" + dataHasil[2] + "\t" +
                        dataHasil[3] + "\t" + dataHasil[4] + "\t" + dataHasil[5] + "\t" +
                        dataHasil[6] + "\t" + dataHasil[7] + "\t" + dataHasil[8] + "\t" +
                        dataHasil[9] + "\t" + dataHasil[10] + "\t" + quality + "\n");
                writer.flush();
                writer.close();
                Toast.makeText(CaptureFruitActivity.this, "Data has been saved!",
                        Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                FileWriter writer = new FileWriter(file, true);
                writer = new FileWriter(file, true);
                writer.append("Area\t" + "Perimeter\t" + "LChannel\t" + "AChannel\t" + "BChannel\t"
                        + "Granularity\t" + "Circularity\t" + "Entropy\t" + "Energy\t"
                        + "Contrast\t" + "Homogenity\t" + "\n");
                writer.append(dataHasil[0] + "\t" + dataHasil[1] + "\t" + dataHasil[2] + "\t" +
                        dataHasil[3] + "\t" + dataHasil[4] + "\t" + dataHasil[5] + "\t" +
                        dataHasil[6] + "\t" + dataHasil[7] + "\t" + dataHasil[8] + "\t" +
                        dataHasil[9] + "\t" + dataHasil[10] + "\t" + quality + "\n");
                writer.flush();
                writer.close();
                Toast.makeText(CaptureFruitActivity.this,
                        "Data has been saved with name Fruit Data.txt",
                        Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        public BackgroundTask(CaptureFruitActivity activity) {
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
//            quality = 1;

            final Dialog resultDialog = new Dialog(CaptureFruitActivity.this);
            resultDialog.setContentView(R.layout.dialog_result);
            TextView txtKualitas = (TextView) resultDialog.findViewById(R.id.quality_text);
            TextView txtLuas = (TextView) resultDialog.findViewById(R.id.luas_text);
            TextView txtKeliling = (TextView) resultDialog.findViewById(R.id.keliling_text);
            TextView txtL = (TextView) resultDialog.findViewById(R.id.L_text);
            TextView txtA = (TextView) resultDialog.findViewById(R.id.A_text);
            TextView txtB = (TextView) resultDialog.findViewById(R.id.B_text);
            TextView txtGranul = (TextView) resultDialog.findViewById(R.id.granularity_text);
            TextView txtCircular = (TextView) resultDialog.findViewById(R.id.circularity_text);
            TextView txtEntropi = (TextView) resultDialog.findViewById(R.id.entropi_text);
            TextView txtEnergi = (TextView) resultDialog.findViewById(R.id.energi_text);
            TextView txtKontras = (TextView) resultDialog.findViewById(R.id.kontras_text);
            TextView txtHomogen = (TextView) resultDialog.findViewById(R.id.homogen_text);
            Button buttonSave = (Button) resultDialog.findViewById(R.id.button_save);
            Button buttonGoBack = (Button) resultDialog.findViewById(R.id.button_go_back);
            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        saveData(quality, dataHasil);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(CaptureFruitActivity.this, "Save Error",
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

//            quality = kenaliKualitas(dataHasil.getNativeObjAddr(), jenisBuah);
            txtKualitas.setText("Quality : " + quality);
            txtLuas.setText("Area : " + (int) dataHasil[0]);
            txtKeliling.setText("Perimeter : " + (int) dataHasil[1]);
            txtL.setText("L : " + dataHasil[2]);
            txtA.setText("A : " + dataHasil[3]);
            txtB.setText("B : " + dataHasil[4]);
            txtGranul.setText("Granularity : " + dataHasil[5]);
            txtCircular.setText("Circularity : " + dataHasil[6]);
            txtEntropi.setText("Entropy : " + dataHasil[7]);
            txtEnergi.setText("Energy : " + dataHasil[8]);
            txtKontras.setText("Contrast : " + dataHasil[9]);
            txtHomogen.setText("Homogenity : " + dataHasil[10]);

            resultDialog.show();
            resultDialog.setTitle("Results");
            resultDialog.setCancelable(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                FeatureExtraction fe = new FeatureExtraction();
                dataHasil = fe.extraction(jenisBuah, hasil);
                //Tentukan Kualitas
                Fann fann = new Fann(modelFruit);
                float[] qualityArray = fann.run(dataHasil);
                float maxQuality = qualityArray[0];
                for (int i = 0; i < qualityArray.length; i++) {
                    if (qualityArray[i] > maxQuality) {
                        maxQuality = qualityArray[i];
                        quality = i + 1;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }
}

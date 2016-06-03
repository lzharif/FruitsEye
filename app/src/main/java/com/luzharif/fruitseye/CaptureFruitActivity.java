package com.luzharif.fruitseye;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LuZharif on 24/04/2016.
 */
public class CaptureFruitActivity extends AppCompatActivity implements View.OnTouchListener {
    private boolean mIsColorSelected = false;
    private Mat mRgba;
    private Mat mRgbaOri;
    private Mat RgbAndroid;
    private Mat mask;
    private Mat hasil;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private FloodFillDetector mDetector;
    private String filename;
    private int quality;
    private int jenisBuah;
    private ImageView ivCitra;
    private TextView txtKualitas;
    private Bitmap citraBitmap;
    private Bitmap orientedBitmap;
    private String folderCitra = Environment.getExternalStorageDirectory().getPath() +
            "/Fruits Eye";

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
        txtKualitas = (TextView) findViewById(R.id.quality_text);
        ivCitra = (ImageView) findViewById(R.id.citra_edit);

        Bundle ambilInfo = getIntent().getBundleExtra("bundle");
        filename = ambilInfo.getString("filename");
        jenisBuah = ambilInfo.getInt("jenis_buah");

//        try {
//            mRgba = Utils.loadResource(this, R.drawable.tomatlegit, Imgcodecs.CV_LOAD_IMAGE_COLOR);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        mRgba = new Mat();
        RgbAndroid = new Mat();
        mRgbaOri = new Mat();
        hasil = Mat.zeros(mRgba.rows(), mRgba.cols(), CvType.CV_8UC3);

        mDetector = new FloodFillDetector();
        if (filename != null) {
            mRgba = Imgcodecs.imread(filename);

            Imgproc.resize(mRgba, mRgba, new Size(480, 640), 0, 0, Imgproc.INTER_CUBIC); //Size itu untuk Portrait
            citraBitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGRA2BGR);
            mRgba.copyTo(mRgbaOri);
            Imgproc.cvtColor(mRgba, RgbAndroid, Imgproc.COLOR_BGRA2RGB);
            Utils.matToBitmap(RgbAndroid, citraBitmap);
            ivCitra.setImageBitmap(citraBitmap);
            ivCitra.setOnTouchListener(this);
        }
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

//        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        mask = mDetector.process(mRgba, projectedX, projectedY);
//        mask = mDetector.process(mRgba, x, y);
//        String filename = folderCitra + "/hasil.jpg";
//        Imgcodecs.imwrite(filename, mask);

        //Simpen disek
        Rect kotak = new Rect(1, 1, mRgba.width(), mRgba.height());
        Mat cropped = new Mat(mask, kotak);
//        List<Mat> citraHasilSplit = new ArrayList<Mat>();
//        Core.split(mRgba, citraHasilSplit);

        mRgbaOri.copyTo(hasil, cropped);

        String filenamebaru = folderCitra + "/hasilbaru.jpg";
        Imgcodecs.imwrite(filenamebaru, hasil);
        Imgproc.cvtColor(hasil, RgbAndroid, Imgproc.COLOR_BGRA2RGB);
        Utils.matToBitmap(RgbAndroid, citraBitmap);
        ivCitra.setImageBitmap(citraBitmap);

        return false;
    }

    public void processImage(View view) {
        FeatureExtraction fe = new FeatureExtraction();
//        float[] dataHasil = fe.extraction(jenisBuah, hasil);
        Mat dataHasil = fe.extraction(jenisBuah, hasil);

//        quality = kenaliKualitas(dataHasil, jenisBuah);
//        txtKualitas.setText("Kualitas :" + quality);
        txtKualitas.setText("Kualitas : " + kenaliKualitas(dataHasil.getNativeObjAddr(), jenisBuah));
    }
}

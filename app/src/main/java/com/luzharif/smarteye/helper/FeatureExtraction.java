package com.luzharif.smarteye.helper;

import android.os.Environment;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LuZharif on 13/05/2016.
 */
public class FeatureExtraction {

    private float energy, entropy, contrast, homogenity;
    private String folderCitra = Environment.getExternalStorageDirectory().getPath() +
            "/Fruits Eye";

    public float[] extraction(int fruit, Mat fruitMat) {
        int area, perimeter, largestContourIndex = 0;
        double largestArea = 0.0;
        float[] result = new float[12];
        float avgFruitL, avgFruitA, avgFruitB, avgFruitC, sdFruit, circularity;
//        float[] result = new float[11];
        Mat fruitHsv = new Mat();
        Mat fruitLab = new Mat();
        Mat fruitV = new Mat();
        Mat fruitGray = new Mat();
        Mat fruitThresh = new Mat();
        Mat contourTemp = new Mat();
        Mat fruitHierarchy = new Mat();
        Mat contourFruitMat = new Mat();
        Mat hullFruitMat = new Mat();
        Mat hullAreaFruitMat = new Mat();
        Mat fruitL = new Mat();
        Mat fruitA = new Mat();
        Mat fruitB = new Mat();
        List<Mat> channelFruitLab = new ArrayList<Mat>();
        List<Mat> channelFruitHsv = new ArrayList<Mat>();
        List<MatOfPoint> contourFruit = new ArrayList<MatOfPoint>();
        List<MatOfPoint> contourFungi = new ArrayList<MatOfPoint>();
        MatOfInt hullFruit = new MatOfInt();

//        Mat result = new Mat(1, 11, CvType.CV_32F);

        Imgproc.resize(fruitMat, fruitMat, new Size(640, 480), 0, 0, Imgproc.INTER_CUBIC);
        Imgproc.cvtColor(fruitMat, fruitHsv, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(fruitMat, fruitLab, Imgproc.COLOR_BGR2Lab);
        Core.split(fruitHsv, channelFruitHsv);
        Core.split(fruitLab, channelFruitLab);
        fruitV = channelFruitHsv.get(2);

        Imgproc.cvtColor(fruitMat, fruitGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(fruitGray, fruitThresh, 128, 255, Imgproc.THRESH_OTSU);

        contourTemp = fruitThresh.clone();
        Imgproc.findContours(contourTemp, contourFruit, fruitHierarchy, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        contourFruitMat = Mat.zeros(fruitThresh.rows(), fruitThresh.cols(), CvType.CV_8UC1);
        hullFruitMat = contourFruitMat.clone();
        hullAreaFruitMat = contourFruitMat.clone();

        for (int i = 0; i < contourFruit.size(); i++) {
            Imgproc.convexHull(contourFruit.get(i), hullFruit, false);
        }

        for (int i = 0; i < contourFruit.size(); i++) {
            double contArea = Imgproc.contourArea(contourFruit.get(i), false);
            if (contArea > largestArea) {
                largestArea = contArea;
                largestContourIndex = i;
            }
//            Point[] titikHullBuah = new Point[200];
//            int j;
//            for (j = 0; j < hullFruit.get(i, 0)[0]; j++) {
//                titikHullBuah[j] = hullFruit.;
//            }
//            List<MatOfPoint> hullFruitSum[1] = {titikHullBuah};
//            Imgproc.fillPoly(contourTemp, );
//            Imgproc.fillPoly(contourTemp, contourFruit, new Scalar(255,255,255));
        }
        MatOfPoint2f contour2f = new MatOfPoint2f(contourFruit.get(largestContourIndex).toArray());

        area = (int) largestArea;
        perimeter = (int) Imgproc.arcLength(contour2f, true);
        circularity = (float) (4 * Math.PI * area / Math.pow(perimeter, 2));

        Core.bitwise_and(fruitThresh, channelFruitLab.get(0), fruitL);
        Core.bitwise_and(fruitThresh, channelFruitLab.get(1), fruitA);
        Core.bitwise_and(fruitThresh, channelFruitLab.get(2), fruitB);

        Core.bitwise_and(fruitThresh, fruitV, fruitV);
        Core.bitwise_and(fruitThresh, fruitGray, fruitGray);
        MeasureTexture(fruitGray);

        MatOfDouble avg = new MatOfDouble();
        MatOfDouble sd = new MatOfDouble();

        Core.meanStdDev(fruitL, avg, sd);
        avgFruitL = (float) avg.get(0, 0)[0];

        Core.meanStdDev(fruitA, avg, sd);
        avgFruitA = (float) avg.get(0, 0)[0];

        Core.meanStdDev(fruitB, avg, sd);
        avgFruitB = (float) avg.get(0, 0)[0];

        avgFruitC = measureConstantC(avgFruitA, avgFruitB);

        Core.meanStdDev(fruitV, avg, sd);
        sdFruit = (float) sd.get(0, 0)[0];


//        area = Core.countNonZero(fruitThresh);
//        float eccentricity = MeasureEccentricity(contourFruit.get(largestContourIndex));


//        float[] data = new float[11];
//        data[0] = area;
//        data[1] = perimeter;
//        data[2] = avgFruitL;    // L
//        data[3] = avgFruitA;    // A
//        data[4] = avgFruitB;    // B
//        data[5] = sdFruit;      // Granularity
//        data[6] = circularity;
//        data[7] = eccentricity;
//        data[8] = entropy;
//        data[9] = energy;
//        data[10] = contrast;
//        data[11] = homogenity;

//        result.put(0, 0, data);

        // No Solidity
        result[0] = area;
        result[1] = perimeter;
        result[2] = avgFruitL;
        result[3] = avgFruitA;
        result[4] = avgFruitB;
        result[5] = avgFruitC;
        result[6] = sdFruit;
        result[7] = circularity;
//        result[7] = eccentricity; // Tidak digunakan karena lebih baik tanpanya
        result[8] = entropy;
        result[9] = energy;
        result[10] = contrast;
        result[11] = homogenity;

//        result.put(0, 0, area);
//        result.put(0, 1, perimeter);
//        result.put(0, 2, avgFruitL);
//        result.put(0, 3, avgFruitA);
//        result.put(0, 4, avgFruitB);
//        result.put(0, 5, sdFruit);
//        result.put(0, 6, circularity);
//        result.put(0, 7, entropy);
//        result.put(0, 8, energy);
//        result.put(0, 9, contrast);
//        result.put(0, 10, homogenity);
        return result;
    }

    private float MeasureEccentricity(MatOfPoint largestContourFruit) {
        float myu20, myu11, myu02, eigenValue1, eigenValue2, result;
        Moments mu = new Moments();
        Mat matrice = new Mat(2, 2, CvType.CV_32F);
        Mat eigenv = new Mat();
        Mat eigenvct = new Mat();

        myu02 = 0;
        myu11 = 0;
        myu20 = 0;

//        for (int i = 0; i < contourFruit.size(); i++) {
//            double area = Imgproc.contourArea(contourFruit.get(i), false);  //  Find the area of contour
//            if (area > largestArea) {
//                largestArea = area;
//                largestContourIndex = i;
//            }
//        }

        mu = Imgproc.moments(largestContourFruit, false);
        myu20 = myu20 + (float) mu.mu20;
        myu11 = myu11 + (float) mu.mu11;
        myu02 = myu02 + (float) mu.mu02;

        matrice.put(0, 0, myu20);
        matrice.put(1, 0, myu11);
        matrice.put(0, 1, myu11);
        matrice.put(1, 1, myu02);

        Core.eigen(matrice, eigenv, eigenvct);
        eigenValue1 = (float) eigenv.get(0, 0)[0];
        eigenValue2 = (float) eigenv.get(1, 0)[0];

        if (eigenValue1 >= eigenValue2)
            result = eigenValue2 / eigenValue1;
        else
            result = eigenValue1 / eigenValue2;

        return result;
    }

    private void MeasureTexture(Mat fruitGray) {
        int row = fruitGray.rows();
        int col = fruitGray.cols();
        Mat gl = new Mat();
        gl = Mat.zeros(256, 256, CvType.CV_32FC1);

        float[] data = new float[1];
        //creating glcm matrix with 256 levels,radius=1 and in the horizontal direction
        for (int i = 0; i < row; i++)
            for (int j = 0; j < col - 1; j++) {
                int getRows = (int) fruitGray.get(i, j)[0];
                int getCols = (int) fruitGray.get(i, j + 1)[0];
                data[0] = (float) fruitGray.get(getRows, getCols)[0] + 1;
                gl.put(getRows, getCols, data);
            }

        // normalizing glcm matrix for parameter determination
        Core.normalize(gl, gl);
//        gl=gl+gl.t();
//        gl=gl/sum(gl)[0];

        double energi, kontras, homogenitas, entropi;
        energi = 0;
        kontras = 0;
        homogenitas = 0;
        entropi = 0;

        for (int i = 0; i < 256; i++)
            for (int j = 0; j < 256; j++) {
                energi = energi + gl.get(i, j)[0] * gl.get(i, j)[0];
                kontras = kontras + (i - j) * (i - j) * gl.get(i, j)[0];
                homogenitas = homogenitas + gl.get(i, j)[0] / (1 + Math.abs(i - j));
                if (gl.get(i, j)[0] != 0)
                    entropi = entropi - gl.get(i, j)[0] * Math.log10(gl.get(i, j)[0]);
            }
        energy = (float) energi;
        contrast = (float) kontras;
        homogenity = (float) homogenitas;
        entropy = (float) entropi;
    }

    private float measureConstantC(float A, float B) {
        float A2 = (float) Math.pow(A, 2);
        float B2 = (float) Math.pow(B, 2);
        float C2 = A2 + B2;
        return (float) Math.pow(C2, 0.5);
    }
}

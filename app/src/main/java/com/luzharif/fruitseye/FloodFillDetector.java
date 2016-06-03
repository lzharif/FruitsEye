package com.luzharif.fruitseye;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FloodFillDetector {
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(100,100,100);
    private Scalar mUpperBound = new Scalar(255,255,255);

    public Mat process(Mat rgbaImage, int xCoor, int yCoor) {
        Mat mask = new Mat();
        Rect rect = new Rect();
        Scalar newVal = new Scalar(255,255,255);
        int connectivity = 4;
        int newMask = 255;
        int flags = connectivity + (newMask << 8) + Imgproc.FLOODFILL_FIXED_RANGE;
//        mask.create(rgbaImage.rows() + 2, rgbaImage.cols() + 2, CvType.CV_8UC1);
        mask = Mat.zeros(rgbaImage.rows() + 2, rgbaImage.cols() + 2, CvType.CV_8UC1);
        Point seed = new Point(xCoor, yCoor);
        Imgproc.threshold(mask, mask, 1, 128, Imgproc.THRESH_BINARY);
        Imgproc.floodFill(rgbaImage, mask, seed, newVal, rect, mLowerBound, mUpperBound, flags);
        return mask;
    }
}

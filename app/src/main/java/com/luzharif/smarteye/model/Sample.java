package com.luzharif.smarteye.model;

import java.util.Arrays;

/**
 * Created by Visual.Sensor on 08/02/2017.
 */

public class Sample {
//    public int nr;
    public int label;
    public double [] features;

    public Sample(int _label, double[] _features) {
//        this.nr = _nr;
        this.label = _label;
        this.features = _features;
    }

    @Override
    public String toString() {
        return  ", cls " + label +
                ", feat: " + Arrays.toString(features);
    }
}

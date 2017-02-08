package com.luzharif.smarteye.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.luzharif.smarteye.model.Sample;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * Created by Visual.Sensor on 08/02/2017.
 */

public class KlasifikasiKemasakan {
    public Context ctx;
    public KlasifikasiKemasakan(Context context) {
        this.ctx = context;
    }

    public String dapatkanKemasakan(final float C) {
        String className = "";
        // Persiapan Model
        // Read Model MLP from Asset (Worked!)
        InputStream is = null;
        Object ois = null;
        try {
            is = ctx.getAssets().open("MLP_37.model");
            ois = new ObjectInputStream(is).readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MultilayerPerceptron mClassifier = (MultilayerPerceptron) ois;


        // Persiapan Instances Offline Manual
        final Attribute attributeC = new Attribute("C");
        final List<String> classes = new ArrayList<String>() {
            {
                add("Light-red"); // cls nr 1
                add("Turning"); // cls nr 2
                add("Red"); // cls nr 3
            }
        };

        // Instances(...) requires ArrayList<> instead of List<>...
        ArrayList<Attribute> attributeList = new ArrayList<Attribute>(2) {
            {
                add(attributeC);
                Attribute attributeClass = new Attribute("@@class@@", classes);
                add(attributeClass);
            }
        };

        // unpredicted data sets (reference to sample structure for new instances)
        Instances dataUnpredicted = new Instances("TestInstances",
                attributeList, 1);
        // last feature is target variable
        dataUnpredicted.setClassIndex(dataUnpredicted.numAttributes() - 1);

        // create new instance: this one should fall into the setosa domain
//        final Sample s = mSamples[mRandom.nextInt(mSamples.length)];
        DenseInstance newInstance = new DenseInstance(dataUnpredicted.numAttributes()) {
            {
                setValue(attributeC, C);
            }
        };
        // reference to dataset
        newInstance.setDataset(dataUnpredicted);

        // predict new sample
        try {
            double result = mClassifier.classifyInstance(newInstance);
            className = classes.get(new Double(result).intValue());
//            String msg = "Nr: " + s.nr + ", predicted: " + className + ", actual: " + classes.get(s.label);
//            Log.d(WEKA_TEST, msg);
//            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return className;
    }

}

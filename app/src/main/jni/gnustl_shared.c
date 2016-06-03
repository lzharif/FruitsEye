#include <jni.h>
#include "opencv2/core/core.hpp"
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/ml/ml.hpp>
#include <stdio.h>

extern "C" {

JNIEXPORT jstring JNICALL Java_com_luzharif_fruitseye_CaptureFruitActivity_kenaliKualitas(JNIEnv *env, jobject obj, jfloatArray data,
                                                                                          jint addrJenisBuah) {

    using namespace std;
    using namespace cv;
    using namespace ml;

    jfloat* _data  = env->GetFloatArrayElements(data, 0);
    int kualitas;
    jInt retKualitas;
    String model = "/storage/emulated/0/Fruits Eye/model_fruit.xml";
    Ptr<ANN_MLP> mlpfunc = Algorithm::load<ANN_MLP>(model);
//    Mat* mRgba = (Mat*) src;
    Mat input(12,1,CV_32F, (unsigned char *)_data);
    Mat response(1,6,CV_8U);
    cv::Point max_loc = {0,0};

    mlpfunc->predict(input, response);

    minMaxLoc(response, NULL, NULL, NULL, &max_loc);
    kualitas = max_loc.x;
    retKualitas = (jInt) kualitas;

    return env->NewStringUTF(env, "Kualitas ", kualitas);
}
}
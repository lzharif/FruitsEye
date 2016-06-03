#include <jni.h>
#include "opencv2/core/core.hpp"
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/ml/ml.hpp>
#include <stdio.h>

extern "C" {

JNIEXPORT jint JNICALL Java_com_luzharif_fruitseye_CaptureFruitActivity_kenaliKualitas(JNIEnv * env, jobject obj, jlong data,
                                                                                          jint addrJenisBuah) {

    using namespace std;
    using namespace cv;
    using namespace ml;

    //jfloat* _data  = env->GetFloatArrayElements(data, 0);
    int kualitas;
    jint retKualitas;
    String model = "/storage/emulated/0/Fruits Eye/model_fruit.xml";
    Ptr<ANN_MLP> mlpfunc = Algorithm::load<ANN_MLP>(model);
    Mat& input = *(Mat*) data;
    //Mat input(11,1,CV_32FC1, (unsigned char *)_data);
    Mat response(1,5,CV_32F);
    cv::Point maxloc;
    maxloc = {0,0};

    mlpfunc->predict(input, response);

    minMaxLoc(response, NULL, NULL, NULL, &maxloc);
    kualitas = maxloc.x + 1;
    retKualitas = (jint) kualitas;

    return retKualitas;

    //return env->NewStringUTF(env, "Kualitas ", kualitas);
    //return env->NewStringUTF((const char* )result.c_str());
}
}
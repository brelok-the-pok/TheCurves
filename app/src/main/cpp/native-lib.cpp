#include <jni.h>

int factorial(int number){
    int result = 1;

    for (int i = 1; i < number + 1; i++) {
        result *= i;
    }
    return result;
}
float pow(float val1, int val2){
    float result = 1;

    for (int i = 0; i < val2; i++) {
        result *= val1;
    }
    return result;
}
extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_thecurves_DrawView_calculateCurvePoint(JNIEnv *env, jobject callingObject, jfloatArray  points, jfloat t, jint rank){
    float *pointsArr = env->GetFloatArrayElements(points, 0);

    float res = 0;

    for (int i = 0; i < rank + 1; ++i) {
        double multiplier = factorial(rank);//Рассчёт вклада в значение текущей точки
        multiplier /= factorial(i);
        multiplier *= pow(t, i);
        multiplier /= factorial(rank - i);
        multiplier *= pow(1 - t, rank - i);
        res += pointsArr[i] * multiplier;
}
return res;

}


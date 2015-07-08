// calculates the values for missing temperatures
// startIdx: the index of the last value before the interval
// endIdx: the index of the first value after the interval


int startIdx = 3188
int endIdx = 3197

double startVal = 9.9
double endVal = 8.9

double step = (startVal - endVal) / (startIdx - endIdx)
println "step = $step\n"


int k = 1;
for(int i = startIdx + 1; i < endIdx; i++) {
    double val = startVal + k * step
    println val
    k++
}

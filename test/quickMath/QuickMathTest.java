package quickMath;

import quickmath.QuickMath;

public class QuickMathTest {

    private interface doSomethingAndIMeasureTime {
        void toDoAndMeasureTime();
    }

    public static float measureTime(doSomethingAndIMeasureTime toDo) {
        long s = System.nanoTime();
        toDo.toDoAndMeasureTime();
        long e = System.nanoTime();
        return (e - s) / 1000000000.0f;
    }

    public static void main(String[] args) {

        final int numIterations = 8;
        final float increment = (float)(360) / numIterations;
        float et1, et2;
        for ( int i = 0; i <= numIterations; i++ ) {
            float degrees = i * increment;
            System.out.println("====== Grados: " + degrees + " ======\n[Original Method]");
            et1 = measureTime(() -> System.out.println(Math.cos(degrees)));
            System.out.printf("Elapsed time: %.6f\n[New Method]\n", et1);

            float rads = degrees * (float) Math.PI / 180.0f;

            et2 = measureTime(() -> System.out.println(QuickMath.cos(rads)));
            System.out.printf("Elapsed time: %.6f\nMejora: %.3f%%\n", et2, (et1 - et2)/et1 * 100);
            System.out.println();
        }
    }

}

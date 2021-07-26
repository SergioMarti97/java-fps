package quickmath;

/**
 * This class contains some
 * mathematical methods (as square root,
 * sine, cosine...) what are approach
 * of the original method but take less time
 * than the original Math library
 */
public class QuickMath {

    /**
     * This method uses simple mathematical
     * operations to approach the cosine result
     * without doing it (because is high resources needed)
     * It takes the input as radians
     *
     * I assume you know how cosine works: in an angle,
     * out the base of the triangle
     *
     * Thanks to @Eriksson, this code is from him
     *
     * @param val the value of the angle
     * @return the base of the triangle (approach)
     */
    public static float cos(float val) {
        //make 3 phase-shifted triangle waves
        float v = Math.abs((val % 2) -1);
        //use cubic beizer curve to approximate the (cos+1)/2 function
        v = v * v * ( 3 - 2 * v );
        return v;
    }

    /**
     * If you think it, the cosine and the sine
     * do the same but only offset
     * So it's possible do the sin using the
     * cos method
     *
     * cos(x) = sin(x + PI/2)
     * (in degrees, 90 degrees)
     *
     * sin: in angle, out the high of the triangle
     *
     * Thank to @Eriksson to explain me this on
     * the OneLoneCoder discord chat
     *
     * @param val the value of the angle
     * @return the high of the triangle (approach)
     */
    public static float sin(float val) {
        return cos(val + ((float)Math.PI / 2.0f));
    }

}

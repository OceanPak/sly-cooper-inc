package solver.ls;
import java.util.concurrent.ThreadLocalRandom;


public class ArrayUtils {
    //Chance should be [0 to 100]
    static public <T> void SlidingWindowShuffle (T[] array, int chance, int windowSize){
        for (int i = 0; i < array.length; i++) {
            if (ThreadLocalRandom.current().nextInt(0, 100) < chance) continue;

            int offset = ThreadLocalRandom.current().nextInt(0, windowSize * 2 + 1) - windowSize;
            int newIndex = i + offset;

            if (newIndex < 0) continue;
            if (newIndex >= array.length) continue;

            T temp = array[i];
            array[i] = array[newIndex];
            array[newIndex] = temp;
        }
    }
}

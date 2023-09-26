package algorithms.alishev.lessons;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

/******************************************************************************

 Online Java Compiler.
 Code, Compile, Run and Debug java program online.
 Write your code in this editor and press "Run" button to execute it.
 *******************************************************************************/

public class Lesson5 {

    // составить из массива цифр самое большое число 3,5,3,9,9, -> 99533
    public static void main(String[] args) {
        int[] enterArray = {1, 2, 3, 6, 9, 9};
        System.out.println("Old array: " + Arrays.toString(enterArray));

        System.out.println("__________________Native array method______________");
        System.out.println("Result array 1: " + nativeArrayMethod(enterArray));
        enterArray = new int[]{1, 2, 3, 6, 9, 9};
        System.out.println("__________________Stream and Collections method______________");
        System.out.println("Result array 2: " + streamAndCollectionsMethod(enterArray));
    }

    private static String nativeArrayMethod(int[] enterArray) {

        StringBuilder resultDigit = new StringBuilder();
        // take every element in the array
        for (int i = 0; i <= enterArray.length - 1; i++) {
            int maxNumber = enterArray[i];
            int indexToClear = 0;
            // comparing with rest of them
            for (int j = 0; j <= enterArray.length - 1; j++) {
                // if we found greater than this one change max value
                if (enterArray[i] < enterArray[j]) {
                    maxNumber = enterArray[j];
                    indexToClear = j;
                }
            }
            resultDigit.append(maxNumber);
            enterArray[indexToClear] = 0;
        }
        return resultDigit.toString();
    }

    private static String streamAndCollectionsMethod(int[] enterArray) {
        return Arrays.stream(enterArray)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
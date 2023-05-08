package solver.ls;

import java.util.Collections;
import java.util.ArrayList;

public class TwoOptSwap {

    public static void checkForSwaps(ClarkeWrightTour t, VRPInstance instance) {        
        boolean foundImprovement;
        do {
            foundImprovement = false;
            for (int i = 0; i < t.customers.size() - 1; i++) {
                for (int j = i + 1; j < t.customers.size() - 1; j++) {
                    double lengthOfUnchangedEdge1 = PointPair.customerDistSq(t.customers.get(i), t.customers.get(i + 1), instance);
                    double lengthOfUnchangedEdge2 = PointPair.customerDistSq(t.customers.get(j), t.customers.get(j + 1), instance);
                    double lengthOfChangedEdge1 = PointPair.customerDistSq(t.customers.get(i + 1), t.customers.get(j + 1), instance);
                    double lengthOfChangedEdge2 = PointPair.customerDistSq(t.customers.get(i), t.customers.get(j), instance);
                    double lengthDelta = - lengthOfUnchangedEdge1 - lengthOfUnchangedEdge2 + lengthOfChangedEdge1 + lengthOfChangedEdge2;

                    if (lengthDelta < 0) {
                        swap(t, i, j);
                        foundImprovement = true;
                    }
                }
            }
        } while (foundImprovement);
    }

    public static void swap(ClarkeWrightTour t, int indexOfBegin, int indexOfEnd) {
        ClarkeWrightTour newTour = new ClarkeWrightTour();

        // adding everything prior to the beginning node (inclusive)
        // take route[0] to route[v1] and add them in order to new_route
        newTour.customers.addAll(t.customers.subList(0, indexOfBegin+1));
        
        // creating a new object to not mutate original tour
        ArrayList<Integer> intersection = new ArrayList<>();

        // take route[v1+1] to route[v2] and add them in reverse order to new_route
        intersection.addAll(t.customers.subList(indexOfBegin+1, indexOfEnd+1));
        Collections.reverse(intersection);
        newTour.customers.addAll(intersection);

        // take route[v2+1] to route[start] and add them in order to new_route
        newTour.customers.addAll(t.customers.subList(indexOfEnd+1, t.customers.size()));
    }
}

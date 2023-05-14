package solver.ls;

import java.util.Collections;
import java.util.ArrayList;

public class ThreeOptSwap {

    public static double reverseSegmentIfBetter(VehicleTour tour, int i, int j, int k, VRPInstance instance) {
        // given tour [...A-B...C-D...E-F...]
        int A = tour.customers.get(i-1), B = tour.customers.get(i), C = tour.customers.get(j-1), D=tour.customers.get(j), E=tour.customers.get(k-1), F=tour.customers.get(k % tour.customers.size());
        
        double d0 = ClarkeWrightPointPair.customerDistSq(A, B, instance) + ClarkeWrightPointPair.customerDistSq(C, D, instance) + ClarkeWrightPointPair.customerDistSq(E, F, instance);
        double d1 = ClarkeWrightPointPair.customerDistSq(A, C, instance) + ClarkeWrightPointPair.customerDistSq(B, D, instance) + ClarkeWrightPointPair.customerDistSq(E, F, instance);
        double d2 = ClarkeWrightPointPair.customerDistSq(A, B, instance) + ClarkeWrightPointPair.customerDistSq(C, E, instance) + ClarkeWrightPointPair.customerDistSq(D, F, instance);
        double d3 = ClarkeWrightPointPair.customerDistSq(A, D, instance) + ClarkeWrightPointPair.customerDistSq(E, B, instance) + ClarkeWrightPointPair.customerDistSq(C, F, instance);
        double d4 = ClarkeWrightPointPair.customerDistSq(F, B, instance) + ClarkeWrightPointPair.customerDistSq(C, D, instance) + ClarkeWrightPointPair.customerDistSq(E, A, instance);

        if (d0 > d1) {
            Collections.reverse(tour.customers.subList(i, j));
            return -d0 + d1;
        } else if (d0 > d2) {
            Collections.reverse(tour.customers.subList(j, k));
            return -d0 + d2;
        } else if (d0 > d4) { 
            Collections.reverse(tour.customers.subList(i, k));
            return -d0 + d4;
        } else if (d0 > d3) {
            ArrayList<Integer> temp = new ArrayList<>();
            temp.addAll(tour.customers.subList(j, k));
            temp.addAll(tour.customers.subList(i, j));
            int index = 0;
            for (int tempIndex = i; tempIndex < k; tempIndex++) {
                tour.customers.set(tempIndex, temp.get(index));
                index++;
            }
            return -d0 + d3;

        }
        return 0;
    }

    public static void threeOptSwap(VehicleTour tour, VRPInstance instance) {
        while (true) {
            double delta = 0;
            for (int i = 1; i < tour.customers.size(); i++) {
                for (int j = i + 2; j < tour.customers.size(); j++) {
                    for (int k = j + 2; k < tour.customers.size() + 1; k++) {
                        delta += reverseSegmentIfBetter(tour, i, j, k, instance);
                    }
                }
            }
            if (delta >= 0) {
                break;
            }
        }
    }

}

package solver.ls;

import java.util.ArrayList;

public class InterTourTwoOptSwap {

    public static void checkForSwaps(ArrayList<ClarkeWrightTour> tours, VRPInstance i) {
        boolean foundImprovement;
        int maxIterations = 10;
        int iterationCount = 0;
        int thresholdDistanceSq = 15 * 15;
        do {
            foundImprovement = false;
            for (int subjectTourIndex = 0; subjectTourIndex < tours.size() - 1; subjectTourIndex++) {
                ClarkeWrightTour subjectTour = tours.get(subjectTourIndex);
                for (int targetTourIndex = subjectTourIndex + 1; targetTourIndex < tours.size()
                        - 1; subjectTourIndex++) {
                    ClarkeWrightTour targetTour = tours.get(targetTourIndex);
                    // TODO: check whether this will cause indexing errors
                    // Ignore the nodes that are next to the depot
                    for (int subjectTourNode = 1; subjectTourNode < subjectTour.customers.size() - 1; subjectTourNode++) {
                        for (int targetTourNode = 1; targetTourNode < targetTour.customers.size() - 1; targetTourNode++) {
                            // check for demand constraints
                            if (subjectTour.totalDemand - i.demandOfCustomer[subjectTourNode] + i.demandOfCustomer[targetTourNode] > i.vehicleCapacity ||
                            targetTour.totalDemand - i.demandOfCustomer[targetTourNode] + i.demandOfCustomer[subjectTourNode] > i.vehicleCapacity){
                                continue;
                            }
                            
                            // check distance 
                            if (PointPair.customerDistSq(
                                    subjectTour.customers.get(subjectTourNode), subjectTour.customers.get(targetTourNode), i) > thresholdDistanceSq) {
                                continue;
                            }

                            // lengthDelta take into account of changed distances from before and after
                            // changing this edge
                            // If we're changing swapping B and E in two tours A - B - C and D - E - F,
                            // We have to check that all the change in distnaces is worth it

                            double lengthOfUnchangedTour1 = 
                                PointPair.customerDistSq(subjectTour.customers.get(subjectTourNode - 1), subjectTour.customers.get(subjectTourNode), i) + 
                                PointPair.customerDistSq(subjectTour.customers.get(subjectTourNode), subjectTour.customers.get(subjectTourNode + 1), i);

                            double lengthOfUnchangedTour2 = 
                                PointPair.customerDistSq(targetTour.customers.get(targetTourNode - 1), targetTour.customers.get(targetTourNode), i) + 
                                PointPair.customerDistSq(targetTour.customers.get(targetTourNode), targetTour.customers.get(targetTourNode + 1), i);

                            double lengthOfChangedTour1 = 
                                PointPair.customerDistSq(subjectTour.customers.get(subjectTourNode - 1), targetTour.customers.get(targetTourNode), i) + 
                                PointPair.customerDistSq(targetTour.customers.get(targetTourNode), subjectTour.customers.get(subjectTourNode + 1), i);

                            double lengthOfChangedTour2 = 
                                PointPair.customerDistSq(targetTour.customers.get(targetTourNode - 1), subjectTour.customers.get(subjectTourNode), i) + 
                                PointPair.customerDistSq(subjectTour.customers.get(subjectTourNode), targetTour.customers.get(targetTourNode + 1), i);

                            double lengthDelta = - lengthOfUnchangedTour1 - lengthOfUnchangedTour2 + lengthOfChangedTour1 + lengthOfChangedTour2;

                            if (lengthDelta < 0) {
                                swap(subjectTour, targetTour, subjectTour.customers.get(subjectTourNode), targetTour.customers.get(targetTourNode));
                                foundImprovement = true;
                            }
                            
                        }
                    }
                }
            }
            iterationCount++;
        } while (foundImprovement && iterationCount > maxIterations);
    }

    public static void swap(ClarkeWrightTour t1, ClarkeWrightTour t2, int t1Node, int t2Node) {
        int indexOfT1Node = t1.customers.indexOf(t1Node);
        int indexOfT2Node = t2.customers.indexOf(t2Node);

        t1.customers.set(indexOfT1Node, t2Node);
        t2.customers.set(indexOfT2Node, t1Node);
    }
}

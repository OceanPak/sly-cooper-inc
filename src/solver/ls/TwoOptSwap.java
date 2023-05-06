package solver.ls;

import java.util.Collections;
import java.util.ArrayList;

public class TwoOptSwap {

    public Tour checkForSwaps(Tour t, VRPInstance instance) {
        double currentDistance = 0;
        for (int i = 0; i < t.customers.size() - 1; i++) {
            currentDistance += PointPair.distSq(
                    instance.xCoordOfCustomer[t.customers.get(i)], instance.yCoordOfCustomer[t.customers.get(i)],
                    instance.xCoordOfCustomer[t.customers.get(i + 1)], instance.yCoordOfCustomer[t.customers.get(i + 1)]);
        }
        System.out.println("previous distance");
        System.out.println(currentDistance);
        
        boolean foundImprovement = true;
        while (foundImprovement) {
            foundImprovement = false;
            for (int i = 0; i < t.customers.size() - 1; i++) {
                for (int j = i + 1; j < t.customers.size() - 1; j++) {
                    double lengthOfUnchangedEdge1 = PointPair.distSq(
                        instance.xCoordOfCustomer[t.customers.get(i)], instance.yCoordOfCustomer[t.customers.get(i)], 
                        instance.xCoordOfCustomer[t.customers.get(i+1)], instance.yCoordOfCustomer[t.customers.get(i+1)]);
                    double lengthOfUnchangedEdge2 = PointPair.distSq(
                        instance.xCoordOfCustomer[t.customers.get(j)], instance.yCoordOfCustomer[t.customers.get(j)], 
                        instance.xCoordOfCustomer[t.customers.get(j+1)], instance.yCoordOfCustomer[t.customers.get(j+1)]);
                    double lengthOfChangedEdge1 = PointPair.distSq(
                        instance.xCoordOfCustomer[t.customers.get(i+1)], instance.yCoordOfCustomer[t.customers.get(i+1)], 
                        instance.xCoordOfCustomer[t.customers.get(j+1)], instance.yCoordOfCustomer[t.customers.get(j+1)]);
                    double lengthOfChangedEdge2 = PointPair.distSq(
                        instance.xCoordOfCustomer[t.customers.get(i)], instance.yCoordOfCustomer[t.customers.get(i)], 
                        instance.xCoordOfCustomer[t.customers.get(j)], instance.yCoordOfCustomer[t.customers.get(j)]);
                    double lengthDelta = - lengthOfUnchangedEdge1 - lengthOfUnchangedEdge2 + lengthOfChangedEdge1 + lengthOfChangedEdge2;

                    if (lengthDelta < 0) {
                        t = swap(t, i, j);
                        currentDistance += lengthDelta;
                        foundImprovement = true;
                    }
                }
            }
        }
        
        double newDistance = 0;
        for (int i = 0; i < t.customers.size() - 1; i++) {
            newDistance += PointPair.distSq(
                    instance.xCoordOfCustomer[t.customers.get(i)], instance.yCoordOfCustomer[t.customers.get(i)],
                    instance.xCoordOfCustomer[t.customers.get(i + 1)], instance.yCoordOfCustomer[t.customers.get(i + 1)]);
        }
        System.out.println("new distance");
        System.out.println(newDistance);
        return t;
    }

    public Tour swap(Tour t, int indexOfBegin, int indexOfEnd) {
        Tour newTour = new Tour();

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
        return newTour;
    }
}

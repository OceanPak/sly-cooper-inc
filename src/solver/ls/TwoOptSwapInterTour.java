package solver.ls;

import java.util.ArrayList;

public class TwoOptSwapInterTour {

  public static void checkForSwaps(ArrayList<VehicleTour> tours, VRPInstance i) {
    boolean foundImprovement;

    do {
      foundImprovement = false;

      for (int subjectTourIndex = 0; subjectTourIndex < tours.size() - 1; subjectTourIndex++) {
        VehicleTour subjectTour = tours.get(subjectTourIndex);
        for (int targetTourIndex = subjectTourIndex + 1; targetTourIndex < tours.size() - 1; targetTourIndex++) {
          VehicleTour targetTour = tours.get(targetTourIndex);

          // Ignore the nodes that are next to the depot
          boolean swapJustHappened = false;
          for (int subjectCustomerIndx = 1; subjectCustomerIndx < subjectTour.customers.size(); subjectCustomerIndx++) {
            if (swapJustHappened) {
              swapJustHappened = false;
              continue;
            }
            for (int targetCustomerIndx = 1; targetCustomerIndx < targetTour.customers.size(); targetCustomerIndx++) {
              if (swapJustHappened) {
                break;
              }

              // check for demand constraints
              if (targetTour.getPartialDemand(0, targetCustomerIndx, i) +
                  subjectTour.getPartialDemand(subjectCustomerIndx, subjectTour.customers.size(), i) > i.vehicleCapacity
                  ||
                  subjectTour.getPartialDemand(0, subjectCustomerIndx, i) +
                      targetTour.getPartialDemand(targetCustomerIndx, targetTour.customers.size(),
                          i) > i.vehicleCapacity) {
                continue;
              }

              //We used to check for point closeness between subjectCustomerIndx and targetCustomerIndx 
              //but it turns our this isn't really needed

              // lengthDelta take into account of changed distances from before and after
              // changing this edge
              // If we're changing swapping B and E in two tours A - B - C and D - E - F,
              // We have to check that all the change in distances is worth it

              double lengthOfUnchangedTour1 = ClarkeWrightPointPair.customerDist(subjectTour.customers.get(subjectCustomerIndx - 1),
                  subjectTour.customers.get(subjectCustomerIndx), i);

              double lengthOfUnchangedTour2 = ClarkeWrightPointPair.customerDist(targetTour.customers.get(targetCustomerIndx - 1),
                  targetTour.customers.get(targetCustomerIndx), i);

              double lengthOfChangedTour1 = ClarkeWrightPointPair.customerDist(subjectTour.customers.get(subjectCustomerIndx - 1),
                  targetTour.customers.get(targetCustomerIndx), i);

              double lengthOfChangedTour2 = ClarkeWrightPointPair.customerDist(targetTour.customers.get(targetCustomerIndx - 1),
                  subjectTour.customers.get(subjectCustomerIndx), i);

              double lengthDelta = -lengthOfUnchangedTour1 - lengthOfUnchangedTour2 + lengthOfChangedTour1
                  + lengthOfChangedTour2;

              if (lengthDelta < -0.0000000001) {
                foundImprovement = true;
                swapJustHappened = true;

                swap(subjectTour, targetTour, subjectTour.customers.get(subjectCustomerIndx),
                    targetTour.customers.get(targetCustomerIndx));
              }
            }
          }
        }
      }
    } while (foundImprovement);

  }

  public static void swap(VehicleTour t1, VehicleTour t2, int t1Node, int t2Node) {
    int indexOfT1Node = t1.customers.indexOf(t1Node);
    int indexOfT2Node = t2.customers.indexOf(t2Node);

    ArrayList<Integer> newT1Customers = new ArrayList<>();
    ArrayList<Integer> newT2Customers = new ArrayList<>();

    newT1Customers.addAll(t1.customers.subList(0, indexOfT1Node));
    newT1Customers.addAll(t2.customers.subList(indexOfT2Node, t2.customers.size()));

    newT2Customers.addAll(t2.customers.subList(0, indexOfT2Node));
    newT2Customers.addAll(t1.customers.subList(indexOfT1Node, t1.customers.size()));

    t1.customers = newT1Customers;
    t2.customers = newT2Customers;
  }
}

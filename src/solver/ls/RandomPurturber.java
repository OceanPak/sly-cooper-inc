package solver.ls;

import java.util.concurrent.ThreadLocalRandom;

//BUGGY >_> Wasn't used
public class RandomPurturber {

    static double tolerance = 10;
    static int maxAttempts = 200;

    static void AttemptRandomCustomerTransfer(VRPInstance instance, VehicleTour[] tours) {
        // Keeps trying until it makes a success or reaches max attempt count
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Pick any two tours
            VehicleTour tourA = tours[ThreadLocalRandom.current().nextInt(0, tours.length)];
            VehicleTour tourB = tours[ThreadLocalRandom.current().nextInt(0, tours.length)];
            if (tourA == tourB)
                return;

            // Pick a random customer in A and also pick the interior closest customer to
            // that in B
            int customerAIndex = ThreadLocalRandom.current().nextInt(0, tourA.customers.size());
            int customerA = tourA.customers.get(customerAIndex);
            int customerBIndex = 0;
            int customerB = 0;

            if (tourB.totalDemand + instance.demandOfCustomer[customerA] > instance.vehicleCapacity){
                continue;
            }

            double currentBestDistace = 100000;
            for (int customerBCandidiateIndx = 1; customerBCandidiateIndx < tourB.customers.size()
                    - 1; customerBCandidiateIndx++) {
                int customerBCandidate = tourB.customers.get(customerBCandidiateIndx);
                double dst = ClarkeWrightPointPair.customerDistSq(customerA, customerBCandidate, instance);
                if (dst < currentBestDistace) {
                    currentBestDistace = dst;
                    customerB = customerBCandidate;
                    customerBIndex = customerBCandidiateIndx;
                }
            }

            double newADistance = 0;
            double newBDistanceToLeft;
            double newBDistanceToRight;

            if (tourA.firstStop == customerA) {
                newADistance = tourA.getTotalDistance(instance)
                        - ClarkeWrightPointPair.customerDepotDist(customerA, instance)
                        + ClarkeWrightPointPair.customerDepotDist(tourA.customers.get(1), instance);
            } else if (tourA.lastStop == customerA) {
                newADistance = tourA.getTotalDistance(instance)
                        - ClarkeWrightPointPair.customerDepotDist(customerA, instance)
                        + ClarkeWrightPointPair.customerDepotDist(tourA.customers.get(tourA.customers.size() - 2),
                                instance);
            } else {
                newADistance = tourA.getTotalDistance(instance)
                        - ClarkeWrightPointPair.customerDist(customerA, tourA.customers.get(customerAIndex + 1),
                                instance)
                        - ClarkeWrightPointPair.customerDist(customerA, tourA.customers.get(customerAIndex - 1),
                                instance)
                        + ClarkeWrightPointPair.customerDist(tourA.customers.get(customerAIndex + 1),
                                tourA.customers.get(customerAIndex - 1), instance);
            }

            newBDistanceToRight = tourB.getTotalDistance(instance)
                    - ClarkeWrightPointPair.customerDist(customerB, tourB.customers.get(customerBIndex + 1), instance)
                    + ClarkeWrightPointPair.customerDist(customerB, customerA, instance)
                    + ClarkeWrightPointPair.customerDist(customerA, tourB.customers.get(customerBIndex + 1), instance);

            newBDistanceToLeft = tourB.getTotalDistance(instance)
                    - ClarkeWrightPointPair.customerDist(tourB.customers.get(customerBIndex - 1), customerB, instance)
                    + ClarkeWrightPointPair.customerDist(tourB.customers.get(customerBIndex + 1), customerA, instance)
                    + ClarkeWrightPointPair.customerDist(customerA, customerB, instance);

            double totalOldDistance = tourA.getTotalDistance(instance) + tourB.getTotalDistance(instance);
            double totalNewDistance = newADistance + Math.min(newBDistanceToLeft, newBDistanceToRight);

            if (totalNewDistance - totalOldDistance < tolerance) {
                // Actually make the swap...
                tourA.removeCustomer(customerA, instance);
                tourB.addCustomerInternal(customerA, customerB, newBDistanceToRight < newBDistanceToLeft, instance);
                System.out.println("Made a successful transfer after " + attempt + " attempts: Dist Diff: " + (totalNewDistance - totalOldDistance));
                return;
            }
        }
    }
}
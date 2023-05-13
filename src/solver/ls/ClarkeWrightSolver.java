package solver.ls;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

//Keep in mind: the indexes of the customers here are off by 1 since the depot is actually
//a customer in the problem specification files (but we removed it from the customer list while parsing)
public class ClarkeWrightSolver {

    VRPInstance instance;

    // Indexed by customer, if two customers have the same route then their values
    // in this dict will be references to the same Tour
    HashMap<Integer, VehicleTour> tours = new HashMap<Integer, VehicleTour>();
    ClarkeWrightPointPair[] savings;

    public ClarkeWrightSolver(VRPInstance instance, boolean shouldRandomize) {
        this.instance = instance;
        // number of distinct pairs

        this.savings = new ClarkeWrightPointPair[((instance.numCustomers) * (instance.numCustomers - 1)) / 2];
        int pairIndex = 0;
        for (int customer1 = 0; customer1 < instance.numCustomers - 1; customer1++) {
            for (int customer2 = customer1 + 1; customer2 < instance.numCustomers; customer2++) {
                savings[pairIndex] = new ClarkeWrightPointPair(customer1, customer2, instance);
                pairIndex += 1;
            }
        }
        Arrays.sort(this.savings, Comparator.reverseOrder());
       if (shouldRandomize) ArrayUtils.SlidingWindowShuffle(this.savings, 30, 30);
        //System.out.println(Arrays.deepToString(savings));
    }

    public HashSet<VehicleTour> solve() {
        for (int i = 0; i < this.savings.length; i++) {
            ClarkeWrightPointPair nextBestSaving = this.savings[i];

            //Necessary since these pairs aren't actually sorted in order of savings
            if (nextBestSaving.savings < 0) continue;

            int customer1 = nextBestSaving.firstCustomer;
            int customer2 = nextBestSaving.secondCustomer;

            boolean customer1HasTour = tours.containsKey(customer1);
            boolean customer2HasTour = tours.containsKey(customer2);

            // Case 1: Neither i nor j have already been assigned to a route, in which case
            // a new route is initiated including both i and j.
            if (!customer1HasTour && !customer2HasTour) {
                VehicleTour newTour = new VehicleTour();
                // check if adding two new customer demands will exceed vehicle capacity, skip
                // if it is the case
                if (instance.demandOfCustomer[customer1]
                        + instance.demandOfCustomer[customer2] > instance.vehicleCapacity) {
                    continue;
                }
                // add customer if it can fit in the vehicle
                newTour.appendCustomer(customer1, instance.demandOfCustomer[customer1]);
                newTour.addCustomerNextToCustomer(customer2, instance.demandOfCustomer[customer2], customer1);
                // set it in tours
                tours.put(customer1, newTour);
                tours.put(customer2, newTour);
            }

            // Case 2: exactly one of the two points (i or j) has already been included in
            // an existing route and that point is not interior to that route
            if (customer1HasTour && !customer2HasTour) {
                VehicleTour tour = tours.get(customer1);
                // if the point in the tour is interior, we skip
                if (!tour.isExteriorStop(customer1)) {
                    continue;
                }
                // if adding the new point causes the demand to exceed, we skip
                if (tour.totalDemand + instance.demandOfCustomer[customer2] > instance.vehicleCapacity) {
                    continue;
                }
                tour.addCustomerNextToCustomer(customer2, instance.demandOfCustomer[customer2], customer1);
                tours.put(customer2, tour);
            }

            if (customer2HasTour && !customer1HasTour) {
                VehicleTour tour = tours.get(customer2);
                // if the point in the tour is interior, we skip
                if (!tour.isExteriorStop(customer2)) {
                    continue;
                }
                // if adding the new point causes the demand to exceed, we skip
                if (tour.totalDemand + instance.demandOfCustomer[customer1] > instance.vehicleCapacity) {
                    continue;
                }
                tour.addCustomerNextToCustomer(customer1, instance.demandOfCustomer[customer1], customer2);
                tours.put(customer1, tour);
            }

            // Case 3: both i and j have already been included in two different existing
            // routes and neither point is interior to its route,
            // in which case the two routes are merged
            if (customer1HasTour && customer2HasTour) {
                VehicleTour t1 = tours.get(customer1);
                VehicleTour t2 = tours.get(customer2);
                // check if the two tours are the same, or if either stop is interior
                if (t1 == t2 || !t1.isExteriorStop(customer1) || !t2.isExteriorStop(customer2)) {
                    continue;
                }
                // check if merging routes will exceed capacity
                if (t1.totalDemand + t2.totalDemand > instance.vehicleCapacity) {
                    continue;
                }
                VehicleTour newTour = VehicleTour.merge(t1, customer1, t2, customer2);
                for (int customer : newTour.customers) {
                    tours.put(customer, newTour);
                }
            }
        }

        // handle individual stops that have no tours (roundtrip to that one point)
        for (int customer = 0; customer < instance.numCustomers; customer++) {
            if (!tours.containsKey(customer)) {
                VehicleTour newTour = new VehicleTour();
                newTour.appendCustomer(customer, instance.demandOfCustomer[customer]);
                tours.put(customer, newTour);
            }
        }

        // Flatten our tours map into a set of tours....
        return new HashSet<VehicleTour>(tours.values());
    }
}
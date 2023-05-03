package solver.ls;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;

public class ClarkeWrightSolver {

    VRPInstance instance;

    // Indexed by customer, if two customers have the same route then their values
    // in this dict will be references to the same Tour
    HashMap<Integer, Tour> tours = new HashMap<Integer, Tour>();
    PointPair[] savings;

    public ClarkeWrightSolver(VRPInstance instance) {
        this.instance = instance;
        // number of distinct pairs
        this.savings = new PointPair[((instance.numCustomers) * (instance.numCustomers - 1)) / 2];
        int pairIndex = 0;
        for (int customer1 = 0; customer1 < instance.numCustomers - 1; customer1++) {
            for (int customer2 = customer1 + 1; customer2 < instance.numCustomers; customer2++) {
                savings[pairIndex] = new PointPair(customer1, customer2,
                        instance.xCoordOfCustomer[customer1], instance.yCoordOfCustomer[customer1],
                        instance.xCoordOfCustomer[customer2], instance.yCoordOfCustomer[customer2]);
                pairIndex += 1;
            }
        }
        Arrays.sort(this.savings);
    }

    public HashSet<Tour> solve() {
        System.out.println("savings list");
        for (int i = 0; i < this.savings.length; i++) {
            PointPair nextBestSaving = this.savings[i];

            int customer1 = nextBestSaving.firstPointName;
            int customer2 = nextBestSaving.secondPointName;

            boolean customer1HasTour = tours.containsKey(customer1);
            boolean customer2HasTour = tours.containsKey(customer2);

            // Case 1: Neither i nor j have already been assigned to a route, in which case
            // a new route is initiated including both i and j.
            if (!customer1HasTour && !customer2HasTour) {
                Tour newTour = new Tour();
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
                Tour tour = tours.get(customer1);
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
                Tour tour = tours.get(customer2);
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
                Tour t1 = tours.get(customer1);
                Tour t2 = tours.get(customer2);
                // check if the two tours are the same, or if either stop is interior
                if (t1 == t2 || !t1.isExteriorStop(customer1) || !t2.isExteriorStop(customer2)) {
                    continue;
                }
                // check if merging routes will exceed capacity
                if (t1.totalDemand + t2.totalDemand > instance.vehicleCapacity) {
                    continue;
                }
                Tour newTour = Tour.merge(t1, customer1, t2, customer2);
                for (int customer : newTour.customers) {
                    tours.put(customer, newTour);
                }
            }
        }

        // handle individual stops that have no tours (roundtrip to that one point)
        for (int customer = 0; customer < instance.numCustomers; customer++) {
            if (!tours.containsKey(customer)) {
                Tour newTour = new Tour();
                newTour.appendCustomer(customer, instance.demandOfCustomer[customer]);
            }
        }

        // Flatten our tours map into a set of tours....
        return new HashSet<Tour>(tours.values());
    }
}

class PointPair implements Comparable<PointPair> {
    public int firstPointName = 0;
    public int secondPointName = 0;
    public double savings = 0;

    public PointPair(int firstName, int secondName, double xfirst, double yfirst, double xsecond, double ysecond) {
        this.firstPointName = firstName;
        this.secondPointName = secondName;
        double distanceFromDepotToFirst = distSq(0, 0, xfirst, yfirst);
        double distanceFromDepotToSecond = distSq(0, 0, xsecond, ysecond);
        double distanceFromFirstToSecond = distSq(xfirst, yfirst, xsecond, ysecond);
        this.savings = distanceFromDepotToFirst + distanceFromDepotToSecond - distanceFromFirstToSecond;
    }

    @Override
    public int compareTo(PointPair arg0) {
        if (this.savings < arg0.savings) {
            return -1;
        }
        if (this.savings > arg0.savings) {
            return 1;
        }
        return 0;
    }

    // TODO: could potentially be more accurate with square root
    static public double distSq(double x1, double y1, double x2, double y2) {
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
    }
}

class Tour {
    public ArrayList<Integer> customers = new ArrayList<>();
    public int totalDemand = 0;
    public int lastStop = 0;
    public int firstStop = 0;

    public void appendCustomer(int customer, int demand) {
        customers.add(customer);
        totalDemand += demand;
        firstStop = customers.get(0); // Necessary in case this is the first customer being added
        lastStop = customers.get(customers.size() - 1);
    }

    // Assumes isExteriorStop(anchorCustomer) is true
    public void addCustomerNextToCustomer(int customerBeingAdded, int demand, int anchorCustomer) {
        if (anchorCustomer == lastStop) {
            customers.add(customerBeingAdded);
            lastStop = customerBeingAdded;
        } else {
            customers.add(0, customerBeingAdded);
            firstStop = customerBeingAdded;
        }
        totalDemand += demand;
    }

    // Assumes both are exterior customers
    static Tour merge(Tour tour1, int tour1AnchorCustomer, Tour tour2, int tour2AnchorCustomer) {
        Tour newTour;

        if (tour1AnchorCustomer == tour1.firstStop && tour2AnchorCustomer == tour2.firstStop) {
            newTour = tour1;
            Collections.reverse(tour1.customers);
            tour1.customers.addAll(tour2.customers);
        } else if (tour1AnchorCustomer == tour1.lastStop && tour2AnchorCustomer == tour2.lastStop) {
            newTour = tour1;
            Collections.reverse(tour2.customers);
            tour1.customers.addAll(tour2.customers);
        } else if (tour1AnchorCustomer == tour1.firstStop && tour2AnchorCustomer == tour2.lastStop) {
            newTour = tour2;
            tour2.customers.addAll(tour1.customers);
        } else {
            // tour1AnchorCustomer == tour1.lastStop && tour2AnchorCustomer ==
            // tour2.firstStop
            newTour = tour1;
            tour1.customers.addAll(tour2.customers);
        }

        newTour.firstStop = newTour.customers.get(0);
        newTour.lastStop = newTour.customers.get(newTour.customers.size() - 1);
        newTour.totalDemand = tour1.totalDemand + tour2.totalDemand;
        return newTour;
    }

    public boolean isExteriorStop(int customer) {
        return customer == lastStop || customer == firstStop;
    }

    public float getTotalDistance(VRPInstance instance) {
        float distance = 0;

        for (int i = 1; i < customers.size(); i++) {
            distance += Math.sqrt(PointPair.distSq(
                    instance.xCoordOfCustomer[customers.get(i)], instance.yCoordOfCustomer[customers.get(i)],
                    instance.xCoordOfCustomer[customers.get(i - 1)], instance.yCoordOfCustomer[customers.get(i - 1)]));
        }

        distance += Math.sqrt(PointPair.distSq(
                instance.xCoordOfCustomer[lastStop], instance.yCoordOfCustomer[lastStop],
                0, 0));

        distance += Math.sqrt(PointPair.distSq(
                instance.xCoordOfCustomer[firstStop], instance.yCoordOfCustomer[firstStop],
                0, 0));

        return distance;
    }
}
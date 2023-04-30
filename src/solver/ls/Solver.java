package solver.ls;

import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;

public class Solver {

    VRPInstance instance;

    // Indexed by customer, if two customers have the same route then their values
    // in this dict will be references to the same Tour
    HashMap<Integer, Tour> tours = new HashMap<Integer, Tour>();
    boolean[] isCustomerInteriorInTour;
    PointPair[] savings;

    public Solver(VRPInstance instance) {
        this.instance = instance;
        this.savings = new PointPair[((instance.numCustomers) * (instance.numCustomers - 1)) / 2]; // number of distinct pairs
        this.isCustomerInteriorInTour = new boolean[instance.numCustomers];
        Arrays.fill(isCustomerInteriorInTour, true);
        int pairIndex = 0;
        for (int customer1 = 0; customer1 < instance.numCustomers - 1; customer1++) {
            for (int customer2 = customer1 + 1; customer2 < instance.numCustomers; customer2++) {
                savings[pairIndex] = new PointPair(customer1, customer2,
                        instance.xCoordOfCustomer[customer1], instance.yCoordOfCustomer[customer1],
                        instance.xCoordOfCustomer[customer2], instance.yCoordOfCustomer[customer2]);
                pairIndex += 1;
            }
            pairIndex += 1;
        }
        Arrays.sort(this.savings);
    }

    public void solve() {
        for (int i = 0; i < this.savings.length; i++) {
            PointPair nextBestSaving = this.savings[i];

            int customer1 = nextBestSaving.firstPointName;
            int customer2 = nextBestSaving.secondPointName;

            // Either, neither i nor j have already been assigned to a route, in which case a new route is initiated including both i and j.
            if (!tours.containsKey(customer1) && !tours.containsKey(customer2)) {
                Tour newTour = new Tour();
                newTour.addCustomer(customer1, instance.demandOfCustomer[customer1], true);
                newTour.addCustomer(customer2, instance.demandOfCustomer[customer2], true);
            }
        }
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
    
    public void addCustomer(int customer, int demand, boolean prepend) {
        if (prepend) {
            customers.add(0, customer);
        } else {
            customers.add(customer);
        }
        totalDemand += demand;
    }
}
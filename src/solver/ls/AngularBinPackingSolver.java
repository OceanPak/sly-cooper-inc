package solver.ls;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AngularBinPackingSolver {

    VRPInstance instance;
    Bin[] bins;
    Integer[] sortedCustomers;

    double[] multipliersForAngularDistance = { 10, 7, 3, 2, 1.5, 1.3, 1, 0.9 };

    public AngularBinPackingSolver(VRPInstance instance, boolean shouldRandomize) {
        this.instance = instance;
        bins = Stream.generate(() -> new Bin(instance)).limit(instance.numVehicles).toArray(Bin[]::new);
        sortedCustomers = IntStream.range(0, instance.numCustomers).boxed().toArray(Integer[]::new);
        Arrays.sort(sortedCustomers, Comparator.comparingInt(a -> instance.demandOfCustomer[(int) a]).reversed());
        if (shouldRandomize) ArrayUtils.SlidingWindowShuffle(sortedCustomers, 30, 7);
    }

    // Assumes you'll do 2-opt and whatever later on the solutions
    HashSet<VehicleTour> solve() {
        for (int customer : sortedCustomers) {
            boolean foundBin = false;
            // We use multiple multipliers such that if one is too limiting we can die it
            // down a bit...
            for (double angularMultiplier : multipliersForAngularDistance) {
                for (Bin bin : bins) {
                    if (bin.canFit(customer, angularMultiplier)) {
                        bin.addCustomer(customer);
                        foundBin = true;
                        System.out.println(angularMultiplier);
                        break;
                    }
                }
                if (foundBin)
                    break;
            }
            // TODO: improve
            if (!foundBin)
                System.out.printf("Error! Didn't find a bin for %d \n", customer + 1);
        }

        return new HashSet<VehicleTour>(Arrays.stream(bins).map(bin -> bin.tour).collect(Collectors.toSet()));
    }
}

class Bin {
    public VehicleTour tour;
    public int spaceLeft;
    public VRPInstance instance;

    public Bin(VRPInstance instance) {
        this.instance = instance;
        spaceLeft = instance.vehicleCapacity;
        tour = new VehicleTour();
    }

    public void addCustomer(int customer) {
        tour.appendCustomer(customer, instance.demandOfCustomer[customer]);
        spaceLeft -= instance.demandOfCustomer[customer];
    }

    public boolean canFit(int customer, double angularDistanceMultiplier) {
        // [-1, 1]
        double averageDotProduct = getAverageNormalizedDotProduct(customer);
        // [1, 0], smaller distance better
        double distance = 1 - (averageDotProduct + 1) / 2;

        System.out.printf("%.4f dist, so %d -> %.2f \n", distance, instance.demandOfCustomer[customer], instance.demandOfCustomer[customer] * (1 + (angularDistanceMultiplier * distance)));
        return instance.demandOfCustomer[customer] * (1 + (angularDistanceMultiplier * distance)) <= spaceLeft;
    }

    public double getAverageNormalizedDotProduct(int customer) {
        // High dot products are better...
        if (tour.customers.size() == 0)
            return 0;
        double average = 0;
        for (int existingCustomer : tour.customers) {
            average += getNormalizedDotProduct(existingCustomer, customer);
        }
        average /= tour.customers.size();
        return average;

    }

    // Is it ok to average over cos of an angle, or should we average over the angle
    // itself?
    private double getNormalizedDotProduct(int c1, int c2) {
        double dotProduct = (instance.depotXCoordinate - instance.xCoordOfCustomer[c1])
                * (instance.depotXCoordinate - instance.xCoordOfCustomer[c2])
                + (instance.depotYCoordinate - instance.yCoordOfCustomer[c1])
                        * (instance.depotYCoordinate - instance.yCoordOfCustomer[c2]);

        dotProduct /= ClarkeWrightPointPair.customerDepotDist(c1, instance)
                * ClarkeWrightPointPair.customerDepotDist(c2, instance);

        return dotProduct;
    }

}

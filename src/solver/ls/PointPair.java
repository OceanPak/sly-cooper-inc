package solver.ls;

class PointPair implements Comparable<PointPair> {
    public int firstCustomer;
    public int secondCustomer;
    public double savings;
    public int combinedDemand;
    public int thresholdDemand;

    public PointPair(int customer1, int customer2, VRPInstance i) {
        this.firstCustomer = customer1;
        this.secondCustomer = customer2;
        this.combinedDemand = (i.demandOfCustomer[customer1] + i.demandOfCustomer[customer2]);
        this.thresholdDemand = Math.round(i.vehicleCapacity * 0.5f);
        double distanceFromDepotToFirst = customerDepotDistSq(customer1, i);
        double distanceFromDepotToSecond = customerDepotDistSq(customer2, i);
        double distanceFromFirstToSecond = customerDistSq(customer1, customer2, i);
        this.savings = distanceFromDepotToFirst + distanceFromDepotToSecond - distanceFromFirstToSecond;
    }

    @Override
    public int compareTo(PointPair arg0) {
        if (this.combinedDemand >= thresholdDemand && arg0.combinedDemand >= arg0.thresholdDemand) {
            if (this.combinedDemand < arg0.combinedDemand) {
                return -1;
            }
            if (this.combinedDemand > arg0.combinedDemand) {
                return 1;
            }
            return 0;
        } else if (this.combinedDemand < thresholdDemand && arg0.combinedDemand < arg0.thresholdDemand) {
            if (this.savings < arg0.savings) {
                return -1;
            }
            if (this.savings > arg0.savings) {
                return 1;
            }
            return 0;
        } else if (this.combinedDemand > thresholdDemand && arg0.combinedDemand < arg0.thresholdDemand) {
            return 1;
        } else {
            return -1;
        }
    }

    static public double customerDistSq(int c1, int c2, VRPInstance i) {
        return Math.pow(i.xCoordOfCustomer[c1] - i.xCoordOfCustomer[c2], 2)
                + Math.pow(i.yCoordOfCustomer[c1] - i.yCoordOfCustomer[c2], 2);
    }

    static public double customerDepotDistSq(int c1, VRPInstance i) {
        return Math.pow(i.depotXCoordinate - i.xCoordOfCustomer[c1], 2)
                + Math.pow(i.depotYCoordinate - i.yCoordOfCustomer[c1], 2);
    }

    static public double customerDist(int c1, int c2, VRPInstance i) {
        return Math.sqrt(PointPair.customerDistSq(c1, c2, i));

    }

    static public double customerDepotDist(int c1, VRPInstance i) {
        return Math.sqrt(PointPair.customerDepotDistSq(c1, i));
    }

    @Override
    public String toString() {
        return String.format("%d-%d (%d, %.1f)", firstCustomer + 1, secondCustomer + 1, combinedDemand, savings);
    }
}
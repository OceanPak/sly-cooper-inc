package solver.ls;

class PointPair implements Comparable<PointPair> {
    public int firstPointName = 0;
    public int secondPointName = 0;
    public double savings = 0;
    public int combinedDemand;
    public int thresholdDemand;

    public PointPair(int firstName, int secondName, VRPInstance i) {
        this.firstPointName = firstName;
        this.secondPointName = secondName;
        this.combinedDemand = (i.demandOfCustomer[firstName] + i.demandOfCustomer[secondName]);
        this.thresholdDemand = Math.round(i.vehicleCapacity * 0.7f);
        double distanceFromDepotToFirst = customerDepoDistSq(firstName, i);
        double distanceFromDepotToSecond = customerDepoDistSq(secondName, i);
        double distanceFromFirstToSecond = customerDistSq(firstName, secondName, i);
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

    // TODO: could potentially be more accurate with square root
    static public double customerDistSq(int c1, int c2, VRPInstance i) {
        return Math.pow(i.xCoordOfCustomer[c1] - i.xCoordOfCustomer[c2], 2) + Math.pow(i.yCoordOfCustomer[c1] - i.yCoordOfCustomer[c2], 2);
    }

    static public double customerDepoDistSq(int c1, VRPInstance i) {
        return Math.pow(i.depotXCoordinate - i.xCoordOfCustomer[c1], 2) + Math.pow(i.depotYCoordinate - i.yCoordOfCustomer[c1], 2);
    }


    @Override
    public String toString() {
        return firstPointName + " " + secondPointName;
    }
}
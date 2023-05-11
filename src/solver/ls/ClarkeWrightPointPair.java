package solver.ls;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;

class ClarkeWrightPointPair implements Comparable<ClarkeWrightPointPair> {
    public int firstCustomer;
    public int secondCustomer;
    public double savings;
    public int combinedDemand;
    public int thresholdDemand;
    HashMap<String, Integer> debugTable = new HashMap<String, Integer>();

    public ClarkeWrightPointPair(int customer1, int customer2, VRPInstance i) {
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
    public int compareTo(ClarkeWrightPointPair arg0) {
        // System.out.printf("current pair %s; arg0 pair %s\n", this.toString(), arg0.toString());
        // System.out.printf("this.combined demand %d, threshold demand %d, arg0 combined demand %d, arg0 threshold demand %d\n", 
        //     this.combinedDemand, thresholdDemand, arg0.combinedDemand, arg0.thresholdDemand);

        // 126-323 > 126-359
        if (this.firstCustomer == 126 - 1 && this.secondCustomer == 323 - 1 && arg0.firstCustomer == 126 - 1 && arg0.secondCustomer == 359 - 1) {
            ArrayList<String> greaterThan = new ArrayList<>();
            for (Entry<String, Integer> entry : debugTable.entrySet()) {
                String[] split = entry.getKey().split(" -> ");
                if (split[0].equals(this.toString()) && entry.getValue() == -1) {
                    System.out.printf("case 1: this is greater %s than %s \n", split[1], split[0]);
                    greaterThan.add(split[1]);
                } else if (split[1].equals(this.toString()) && entry.getValue() == 1) {
                    System.out.printf("case 2: this is greater %s than %s \n", split[0], split[1]);
                }
            }
            for (Entry<String, Integer> entry : debugTable.entrySet()) {
                String[] split = entry.getKey().split(" -> ");
                if (split[0].equals(arg0.toString()) && split[1].equals(greaterThan.get(0)) && entry.getValue() == 1) {
                    System.out.println("case 1: it's smaller than 126-359!");
                } else if (split[0].equals(greaterThan.get(0)) && split[1].equals(arg0.toString()) && entry.getValue() == -1) {
                    System.out.println("case 2: it's smaller than 126-359!");
                }
            }
        }

        if (this.firstCustomer == 126 - 1 && this.secondCustomer == 324 - 1) {
            System.out.printf("suspicious edge %s \n", this.toString());
        }

        if (this.combinedDemand >= thresholdDemand && arg0.combinedDemand >= arg0.thresholdDemand) {
                if (this.combinedDemand < arg0.combinedDemand) {
                    debugTable.put(this.toString() + " -> " + arg0.toString(), -1);
                    return -1;
                }
                if (this.combinedDemand > arg0.combinedDemand) {
                    debugTable.put(this.toString() + " -> " + arg0.toString(), 1);
                    return 1;
                }
                debugTable.put(this.toString() + " -> " + arg0.toString(), 0);
                return 0;
            } else if (this.combinedDemand < thresholdDemand && arg0.combinedDemand < arg0.thresholdDemand) {
                if (this.savings < arg0.savings) {
                    debugTable.put(this.toString() + " -> " + arg0.toString(), -1);
                    return -1;
                }
                if (this.savings > arg0.savings) {
                    debugTable.put(this.toString() + " -> " + arg0.toString(), 1);
                    return 1;
                }
                debugTable.put(this.toString() + " -> " + arg0.toString(), 0);
                return 0;
            } else if (this.combinedDemand > thresholdDemand && arg0.combinedDemand < arg0.thresholdDemand) {
                debugTable.put(this.toString() + " -> " + arg0.toString(), 1);
                return 1;
            } else {
                debugTable.put(this.toString() + " -> " + arg0.toString(), -1);
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
        return Math.sqrt(ClarkeWrightPointPair.customerDistSq(c1, c2, i));

    }

    static public double customerDepotDist(int c1, VRPInstance i) {
        return Math.sqrt(ClarkeWrightPointPair.customerDepotDistSq(c1, i));
    }

    @Override
    public String toString() {
        return String.format("%d-%d (%d, %.1f)", firstCustomer + 1, secondCustomer + 1, combinedDemand, savings);
    }
}
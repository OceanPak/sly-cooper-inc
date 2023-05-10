package solver.ls;

import java.util.ArrayList;
import java.util.Collections;

class ClarkeWrightTour {
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
    static ClarkeWrightTour merge(ClarkeWrightTour tour1, int tour1AnchorCustomer, ClarkeWrightTour tour2, int tour2AnchorCustomer) {
        ClarkeWrightTour newTour;

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
            distance += PointPair.customerDist(customers.get(i), customers.get(i-1), instance);
        }

        distance += PointPair.customerDepotDist(lastStop, instance);
        distance += PointPair.customerDepotDist(firstStop, instance);
    
        return distance;
    }

    public int getPartialDemand(int startIndexIncl, int endIndexExcl, VRPInstance i){
        return customers.subList(startIndexIncl, endIndexExcl).stream().reduce
        (0, (partialSum, customer) -> partialSum + i.demandOfCustomer[customer]);
    }
}
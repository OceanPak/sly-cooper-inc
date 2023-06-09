package solver.ls;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Scanner;

public class VRPInstance {
  // VRP Input Parameters
  public int numCustomers;        		// the number of customers	   
  public int numVehicles;           	// the number of vehicles
  public int vehicleCapacity;			// the capacity of the vehicles

  public double depotXCoordinate;
  public double depotYCoordinate;

  public int[] demandOfCustomer;		// the demand of each customer
  public double[] xCoordOfCustomer;	// the x coordinate of each customer
  public double[] yCoordOfCustomer;	// the y coordinate of each customer
  
  public VRPInstance(String fileName) {
    Scanner read = null;
    
    try {
      read = new Scanner(new File(fileName));
    } catch (FileNotFoundException e) {
      System.out.println("Error: in VRPInstance() " + fileName + "\n" + e.getMessage());
      System.exit(-1);
    }

    numCustomers = read.nextInt() - 1;  //First customer is actually the depot
    numVehicles = read.nextInt();
    vehicleCapacity = read.nextInt();
    
    System.out.println("Number of customers: " + numCustomers);
    System.out.println("Number of vehicles: " + numVehicles);
    System.out.println("Vehicle capacity: " + vehicleCapacity);

    demandOfCustomer = new int[numCustomers]; 
	  xCoordOfCustomer = new double[numCustomers];
	  yCoordOfCustomer = new double[numCustomers];

    // Handle Depot 'demand' and location
    read.nextInt();
    depotXCoordinate = read.nextDouble();
    depotYCoordinate = read.nextDouble();

    for (int i = 0; i < numCustomers; i++) {
		  demandOfCustomer[i] = read.nextInt();
		  xCoordOfCustomer[i] = read.nextDouble();
		  yCoordOfCustomer[i] = read.nextDouble();
    }
  }
}

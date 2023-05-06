package solver.ls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.out.println("Usage: java Main <file>");
			return;
		}

		String input = args[0];
		Path path = Paths.get(input);
		String filename = path.getFileName().toString();
		System.out.println("Instance: " + input);

		Timer watch = new Timer();
		watch.start();
		VRPInstance instance = new VRPInstance(input);

		// ***** Clarke Wright Solver *****
		ClarkeWrightSolver s = new ClarkeWrightSolver(instance);
		HashSet<Tour> tours = s.solve();
		ArrayList<Tour> modifiedTours = new ArrayList<>();

		if (tours.size() > instance.numVehicles) {
			System.out.printf("Problem! For instance " + input + ", Clarke Wright gave us %d vehicles, but %d is the max\n",
					tours.size(), instance.numVehicles);
			return;
		}

		// Perform 2 Opt Swap
		Iterator<Tour> iter = tours.iterator();
		TwoOptSwap twoopt = new TwoOptSwap();
        while (iter.hasNext()) {
			Tour t = iter.next();
			t = twoopt.checkForSwaps(t, instance);
			modifiedTours.add(t);
        }

		// Java streams was being annoying, did the naive solution
		int totalDistance = 0;
		for (Tour t : modifiedTours)
			totalDistance += t.getTotalDistance(instance);

		File yourFile = new File("output.txt");
		yourFile.createNewFile(); // if file already exists will do nothing
		FileWriter writer = new FileWriter(yourFile);

		writer.write(totalDistance + " 0\n");
		for (Tour t : modifiedTours) {
			String pathString = t.customers.stream().reduce(
					"",
					(partialPath, customer) -> partialPath + " " + customer.toString(),
					String::concat);
			writer.write("0" + pathString + " 0\n");
		}

		writer.close();

		// ***** ***** ***** ***** *****
		watch.stop();

		System.out.println("{\"Instance\": \"" + filename +
				"\", \"Time\": " + String.format("%.2f", watch.getTime()) +
				", \"Result\": " + totalDistance +
				", \"Solution\": \"--\"}");
	}
}
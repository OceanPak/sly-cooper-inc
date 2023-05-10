package solver.ls;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

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

		// *************** Clarke Wright Solver ********************
		ClarkeWrightSolver s = new ClarkeWrightSolver(instance);
		HashSet<ClarkeWrightTour> tours = s.solve();

		if (tours.size() > instance.numVehicles) {
			System.out.printf(
					"Problem! For instance " + input + ", Clarke Wright gave us %d vehicles, but %d is the max\n",
					tours.size(), instance.numVehicles);
		}

		// Perform 2 Opt Swap
		Iterator<ClarkeWrightTour> iter = tours.iterator();
		while (iter.hasNext()) {
			ClarkeWrightTour t = iter.next();
			TwoOptSwap.checkForSwaps(t, instance);
		}

		// Perform inter-tour 2 opt swap (then 2 Opt Swap to clean up any changes)
		InterTourTwoOptSwap.checkForSwaps(new ArrayList<>(tours), instance);

		iter = tours.iterator();
		while (iter.hasNext()) {
			ClarkeWrightTour t = iter.next();
			TwoOptSwap.checkForSwaps(t, instance);
		}

		// Java streams was being annoying, did the naive solution
		int totalDistance = 0;
		for (ClarkeWrightTour t : tours)
			totalDistance += t.getTotalDistance(instance);

		Files.createDirectories(Paths.get("./solver_outputs/"));
		File yourFile = new File("./solver_outputs/" + filename + "output.txt");
		System.out.println(yourFile.getAbsolutePath());
		yourFile.createNewFile(); // if file already exists will do nothing
		FileWriter writer = new FileWriter(yourFile);

		writer.write(totalDistance + " 0\n");
		for (ClarkeWrightTour t : tours) {
			String pathString = t.customers.stream().reduce(
					"",
					// Adding +1 because customer indexes are off by 1; check
					// ClarkeWrightSolver.java for more info
					(partialPath, customer) -> partialPath + " " + (customer + 1),
					String::concat);
			writer.write("0" + pathString + " 0\n");
		}

		writer.close();

		// ***** ***** ***** ***** ***** ***** *****
		watch.stop();

		System.out.println("{\"Instance\": \"" + filename +
				"\", \"Time\": " + String.format("%.2f", watch.getTime()) +
				", \"Result\": " + totalDistance +
				", \"Solution\": \"--\"}");
	}
}
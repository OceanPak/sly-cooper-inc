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

	static int randomizedAttempts = 50;

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

		// Just trying multiple times since solvers make use of randomization
		HashSet<VehicleTour> bestTours = new HashSet<>();
		double bestDistance = Double.MAX_VALUE;

		for (int attempt = 0; attempt < randomizedAttempts; attempt++) {
			try {

				ClarkeWrightSolver s = new ClarkeWrightSolver(instance, attempt != 0);
				// AngularBinPackingSolver s = new AngularBinPackingSolver(instance, attempt != 0);

				HashSet<VehicleTour> tours = s.solve();

				if (tours.size() > instance.numVehicles) {
					System.out.printf(
							"Attempt aborted - For instance " + input
									+ ", solver gave us %d vehicles, but %d is the max.\n",
							tours.size(), instance.numVehicles);
					continue;
				}

				// Perform 2 Opt Swap
				Iterator<VehicleTour> iter = tours.iterator();
				while (iter.hasNext()) {
					VehicleTour t = iter.next();
					TwoOptSwap.checkForSwaps(t, instance);
				}

				// Perform inter-tour 2 opt swap (then 2 Opt Swap to clean up any changes)
				TwoOptSwapInterTour.checkForSwaps(new ArrayList<>(tours), instance);

				iter = tours.iterator();
				while (iter.hasNext()) {
					VehicleTour t = iter.next();
					TwoOptSwap.checkForSwaps(t, instance);
				}

				Double totalDistance = tours.stream().mapToDouble(t -> t.getTotalDistance(instance)).sum();

				if (totalDistance < bestDistance) {
					bestDistance = totalDistance;
					bestTours = tours;
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		if (bestDistance == Double.MAX_VALUE) {
			System.out.printf(
					"Couldn't find a good solution after %d attempts.\n",
					randomizedAttempts);
			System.exit(1);
		}

		Files.createDirectories(Paths.get("./solver_outputs/"));
		File yourFile = new File("./solver_outputs/" + filename + "output.txt");
		System.out.println(yourFile.getAbsolutePath());
		yourFile.createNewFile(); // if file already exists will do nothing
		FileWriter writer = new FileWriter(yourFile);

		// TODO: for trucks that end up not getting tours, we should still make empty
		// tours for htem.
		writer.write(bestDistance + " 0\n");
		for (VehicleTour t : bestTours) {
			String pathString = t.customers.stream().reduce(
					"",
					// Adding +1 because customer indexes are off by 1; check
					// ClarkeWrightSolver.java for more info
					(partialPath, customer) -> partialPath + " " + (customer + 1),
					String::concat);
			writer.write("0" + pathString + " 0\n");
		}

		writer.close();

		watch.stop();

		System.out.println("{\"Instance\": \"" + filename +
				"\", \"Time\": " + String.format("%.2f", watch.getTime()) +
				", \"Result\": " + bestDistance +
				", \"Solution\": \"--\"}");
	}
}
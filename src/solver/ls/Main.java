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
import java.util.function.BiFunction;

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
		SolutionHolder solution = solveWithSolver(instance, input, 5000, (a, i) -> {
			ClarkeWrightSolver s = new ClarkeWrightSolver(i, a != 0);
			return s.solve();
		});

		if (solution.distance == Double.MAX_VALUE) {
			System.out.printf(
					"Couldn't find a good solution after %d attempts. Resorting to bin packing solution :(\n",
					5000);

			solution = solveWithSolver(instance, input, 100, (a, i) -> {
				AngularBinPackingSolver s = new AngularBinPackingSolver(i, a != 0);
				return s.solve();
			});
		}

		if (solution.distance == Double.MAX_VALUE) {
			System.out.printf("Still couldn't find solution. Giving up. \n");
			System.exit(1);
		}

		Files.createDirectories(Paths.get("./solver_outputs/"));
		File yourFile = new File("./solver_outputs/" + filename + "output.txt");
		System.out.println(yourFile.getAbsolutePath());
		yourFile.createNewFile(); // if file already exists will do nothing
		FileWriter writer = new FileWriter(yourFile);

		// TODO: for trucks that end up not getting tours, we should still make empty
		// tours for them.
		writer.write(solution.distance + " 0\n");
		String resultsString = "";
		for (VehicleTour t : solution.tours) {
			String pathString = t.toString();
			writer.write("0" + pathString + " 0\n");
			resultsString += "0" + pathString + " 0 ";
		}

		writer.close();
		watch.stop();
		System.out.println("{\"Instance\": \"" + filename +
				"\", \"Time\": " + String.format("%.2f", watch.getTime()) +
				", \"Result\": " + String.format("%.2f", solution.distance) +
				", \"Solution\": \"" + "0 " + resultsString.trim() + "\"}");
	}

	static SolutionHolder solveWithSolver(
			VRPInstance instance, String input, int maxAttempts,
			BiFunction<Integer, VRPInstance, HashSet<VehicleTour>> solver) {

		HashSet<VehicleTour> bestTours = new HashSet<>();
		double bestDistance = Double.MAX_VALUE;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			try {

				HashSet<VehicleTour> tours = solver.apply(attempt, instance);

				if (tours.size() > instance.numVehicles) {
					System.out.printf(
							"Attempt " + attempt + " aborted - For instance " + input
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
			} catch (Error e) {
				System.out.println(e.getMessage() + " Skipping attempt " + attempt);
			} catch (Exception e) {
				System.out.println(e.getMessage() + " Skipping attempt " + attempt);
			}
		}
		return new SolutionHolder(bestTours, bestDistance);
	}
}

class SolutionHolder {
	HashSet<VehicleTour> tours;
	double distance;

	public SolutionHolder(HashSet<VehicleTour> tours, double distance) {
		this.tours = tours;
		this.distance = distance;
	}
}
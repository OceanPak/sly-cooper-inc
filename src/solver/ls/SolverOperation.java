package solver.ls;

import java.util.HashSet;

interface SolverOperation {

    public HashSet<VehicleTour> operation(int attempt, VRPInstance instance);
}
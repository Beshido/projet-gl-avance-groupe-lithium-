package hubertmap.model.transport;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents a network of stations and edges between them, forming a transport network.
 */
public class Network {
    Graph<Station, EdgeTransport> graph;
    HashMap<String, Station> stations;
    List<EdgeTransport> shortestPath;

    DijkstraShortestPath<Station, EdgeTransport> distancePaths;

    public Network(Collection<EdgeTransport> edges) {
        graph = new SparseGraph<>();
        stations = new HashMap<>();
        shortestPath = new ArrayList<>();

        if (edges != null) {
            for (EdgeTransport e : edges) {
                addEdge(e);
            }
        }

        distancePaths =
                new DijkstraShortestPath<Station, EdgeTransport>(
                        graph,
                        (EdgeTransport e) -> {
                            return ((EdgeTransport) e).getDistance();
                        });
    }

    public Network() {
        this(null);
    }

    public void addEdge(EdgeTransport edge, Station station1, Station station2) {
        graph.addEdge(edge, station1, station2);

        stations.putIfAbsent(station1.getName().toLowerCase(), station1);
        stations.putIfAbsent(station2.getName().toLowerCase(), station2);
    }

    public void addEdge(EdgeTransport edge) {
        addEdge(edge, edge.getStartingStation(), edge.getEndingStation());
    }

    public Graph<Station, EdgeTransport> getGraph() {
        return graph;
    }

    /**
     * Calculates the shortest path from station1 to station2 using the distance between two
     * stations as weight
     *
     * @param station1 starting station
     * @param station2 destination station
     * @return a list edges to visit in the correct order
     */
    public List<EdgeTransport> shortestPath(Station station1, Station station2) {
        return distancePaths.getPath(station1, station2);
    }

    /**
     * Calculates the shortest path from station1 to station2 using the distance between two
     * stations as weight
     *
     * @param station1 name of the starting station
     * @param station2 name of the destination station
     * @return a list edges to visit in the correct order
     */
    public List<EdgeTransport> shortestPath(String station1, String station2) {
        if (stations.get(station1) != null && stations.get(station2) != null)
            return shortestPath(stations.get(station1), stations.get(station2));
        else return null;
    }
}

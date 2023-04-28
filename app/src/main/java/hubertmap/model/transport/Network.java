package hubertmap.model.transport;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import hubertmap.model.DurationJourney;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a network of stations and edges between them, forming a transport network.
 */
public class Network {
    Graph<Station, EdgeTransport> graph;
    HashMap<String, Station> stations;
    List<EdgeTransport> shortestPath;

    DijkstraShortestPath<Station, EdgeTransport> distancePaths;
    private Map<Line, ArrayList<DurationJourney>> datatLine;

    /**
     * Constructs a new Network object with the given collection of edges.
     *
     * @param edges the collection of edges to add to the network
     */
    public Network(Collection<EdgeTransport> edges) {
        graph = new SparseGraph<>();
        stations = new HashMap<>();
        shortestPath = new ArrayList<>();

        if (edges != null) {
            for (EdgeTransport e : edges) {
                addEdge(e);
            }
        }

        distancePaths = new DijkstraShortestPath<>(graph, EdgeTransport::estimateWeight);
    }
    /** Constructs a new Network object with no edges. */
    public Network() {
        this(null);
    }
    /**
     * Adds an edge to the network, connecting the given stations.
     *
     * @param edge the edge to add
     * @param station1 the first station to connect
     * @param station2 the second station to connect
     */
    public void addEdge(EdgeTransport edge, Station station1, Station station2) {
        graph.addEdge(edge, station1, station2);

        stations.putIfAbsent(station1.getName().toLowerCase(), station1);
        stations.putIfAbsent(station2.getName().toLowerCase(), station2);
    }

    /**
     * Adds an edge to the network, connecting the stations at the start and end of the edge.
     *
     * @param edge the edge to add
     */
    public void addEdge(EdgeTransport edge) {
        addEdge(edge, edge.getStartingStation(), edge.getEndingStation());
    }

    /**
     * Returns the graph representing the network.
     *
     * @return the graph representing the network
     */
    public Graph<Station, EdgeTransport> getGraph() {
        return graph;
    }

    /**
     * Calculates the shortest path from station1 to station2 using the distance between two
     * stations as weight
     *
     * <p>for each edge returned, swaps starting and ending stations if needed, corresponding with
     * the direction taken
     *
     * @param station1 starting station
     * @param station2 destination station
     * @return a list edges to visit in the correct order
     */
    public List<EdgeTransport> shortestPath(Station station1, Station station2) {
        List<EdgeTransport> list = distancePaths.getPath(station1, station2);
        Station s = station1;
        for (EdgeTransport e : list) {
            if (!e.getStartingStation().equals(s)) {
                e.swapStations();
            }
            s = e.getEndingStation();
        }
        return list;
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

    /**
     * Set the list of lines in the network with their duration.
     *
     * @param dataLine the list of lines with their duration
     */
    public void setDataLine(Map<Line, ArrayList<DurationJourney>> dataLine) {
        this.datatLine = dataLine;
    }

    /**
     * Returns the list of lines in the network.
     *
     * @return the list of lines in the network
     */
    public Set<Line> getLines() {
        return datatLine.keySet();
    }

    /**
     * Uses the input path to create an abstracted vestion of it, were adjascent path on the same
     * line are merged into a single path
     *
     * @param path list of EdgeTransport
     * @return a new list of EdgeTransport
     */
    public List<EdgeTransport> simplifiedPath(List<EdgeTransport> path) {
        List<EdgeTransport> simplePath = new ArrayList<>();
        if (path.isEmpty()) {
            return simplePath;
        }

        EdgeTransport prevEdge = path.get(0);
        Station prevStation = prevEdge.getStartingStation();

        float distance = prevEdge.getDistance();
        DurationJourney duration = prevEdge.getDurationJourney().copy();

        for (int i = 1; i < path.size() - 1; i++) {
            EdgeTransport e = path.get(i);
            if (!e.getLineName().equals(prevEdge.getLineName())) {
                simplePath.add(
                        new EdgeTransport(
                                prevStation,
                                e.getStartingStation(),
                                duration,
                                distance,
                                prevEdge.getLineName()));
                prevStation = e.getStartingStation();
                prevEdge = e;
                distance = e.getDistance();
                duration = e.getDurationJourney().copy();
            } else {
                distance += e.getDistance();
                duration.add(e.getDurationJourney());
            }
        }

        EdgeTransport lastEdge = path.get(path.size() - 1);

        if (lastEdge.getLineName().equals(prevEdge.getLineName())) {
            distance += lastEdge.getDistance();
            duration.add(lastEdge.getDurationJourney());
            simplePath.add(
                    new EdgeTransport(
                            prevStation,
                            lastEdge.getEndingStation(),
                            duration,
                            distance,
                            lastEdge.getLineName()));
        } else {
            simplePath.add(
                    new EdgeTransport(
                            prevStation,
                            lastEdge.getStartingStation(),
                            duration,
                            distance,
                            prevEdge.getLineName()));
            simplePath.add(lastEdge.copy());
        }

        return simplePath;
    }
}

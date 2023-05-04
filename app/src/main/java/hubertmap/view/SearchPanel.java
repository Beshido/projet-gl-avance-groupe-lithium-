package hubertmap.view;

import hubertmap.controller.Controller;
import hubertmap.model.transport.VertexTransport;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * The SearchPanel class is responsible for the search interface that allows the user to search for
 * the shortest path between two stations. It provides two text areas for the user to enter the
 * names of the departure and arrival stations or their coordinates.
 */
public class SearchPanel extends JPanel {

    /** TextArea with autocompletion to search start station */
    TextAreaDemo textAreaStationStart;

    /** TextArea with autocompletion to search end station */
    TextAreaDemo textAreaStationEnd;

    /** HashMap to store station names with accents */
    private HashMap<String, String> actualStationsNames;

    /** A List object to hold names of station for search. */
    private List<String> stationsNames;

    SearchPanel(GraphData graphView) {

        setData(graphView.getVertices());

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        textAreaStationStart = new TextAreaDemo(stationsNames);
        textAreaStationStart.setText("Departure : station or coordinates");
        textAreaStationEnd = new TextAreaDemo(stationsNames);
        textAreaStationEnd.setText("Arrival : station or coordinates");

        JPanel startPanel = new JPanel();
        startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.X_AXIS));
        startPanel.add(textAreaStationStart);

        JPanel endPanel = new JPanel();
        endPanel.setLayout(new BoxLayout(endPanel, BoxLayout.X_AXIS));
        endPanel.add(textAreaStationEnd);

        JButton search = new JButton("Search");
        search.addActionListener(search());

        this.add(startPanel);
        this.add(endPanel);
        this.add(search);
    }

    /**
     * Convert Collection of station to ArrayList and initialise stationsNames list.
     *
     * @param stations Collection of station used for search
     */
    private void setData(Collection<VertexTransport> stations) {
        stationsNames = new ArrayList<>();
        actualStationsNames = new HashMap<>();
        for (VertexTransport station : stations) {
            String n1 = station.getName().toLowerCase();
            String n2 = stripAccents(n1);
            stationsNames.add(n1);
            if (!(n1.equals(n2))) {
                stationsNames.add(n2);
                actualStationsNames.put(n2, n1);
            }
        }
        Collections.sort(stationsNames);
    }

    /**
     * @param s a character string
     * @return s with accents replaced with ascii characters
     */
    private String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    /**
     * Returns an ActionListener that executes a search for the shortest path between two stations.
     *
     * @return ActionListener that executes a search for the shortest path
     */
    private ActionListener search() {
        ActionListener al =
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        Controller.deleteUserPoints();

                        String stringStart = textAreaStationStart.getValue();
                        String stringEnd = textAreaStationEnd.getValue();

                        String station1Name;
                        String station2Name;

                        Point2D.Float coords1 = parseCoordinates(stringStart);
                        if (coords1 != null)
                            station1Name = Controller.createPoint(coords1.getX(), coords1.getY());
                        else {
                            String s1 = actualStationsNames.get(stringStart);
                            if (s1 != null) {
                                station1Name = s1;
                            } else station1Name = stringStart;
                        }

                        Point2D.Float coords2 = parseCoordinates(stringEnd);
                        if (coords2 != null)
                            station2Name = Controller.createPoint(coords2.getX(), coords2.getY());
                        else {
                            String s2 = actualStationsNames.get(stringEnd);
                            if (s2 != null) {
                                station2Name = s2;
                            } else station2Name = stringEnd;
                        }

                        Controller.setShortestPath(station1Name, station2Name);
                    }
                };
        return al;
    }

    /**
     * Parses a string containing latitude and longitude coordinates in the format
     * "latitude,longitude" and returns a Point2D.Float object representing the coordinates.
     *
     * @param str to parse
     * @return Point2D.Float with latitude and longitude
     */
    public Point2D.Float parseCoordinates(String str) {
        String[] parts = str.split(",");
        if (parts.length != 2) {
            return null;
        }
        try {
            float latitude = Float.parseFloat(parts[0].trim());
            float longitude = Float.parseFloat(parts[1].trim());
            return new Point2D.Float(latitude, longitude);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Sets the name of the departure station to be displayed in the text area.
     *
     * @param name the name of the departure station to be displayed.
     */
    public void setDeparture(String name) {
        textAreaStationStart.setText(name);
        String end = stripAccents(ArrivalGetText().toLowerCase());

        if (end.equals(stripAccents(name.toLowerCase()))) {
            setArrival("arrival : station or coordinates");
        }
    }

    public String DepartureGetText() {
        return textAreaStationStart.getValue();
    }

    public String ArrivalGetText() {
        return textAreaStationEnd.getValue();
    }

    /**
     * Sets the name of the arrival station to be displayed in the text area.
     *
     * @param name the name of the arrival station to be displayed.
     */
    public void setArrival(String name) {
        textAreaStationEnd.setText(name);

        String start = stripAccents(DepartureGetText().toLowerCase());

        if (start.equals(stripAccents(name.toLowerCase()))) {
            setDeparture("departure : station or coordinates");
        }
    }
}

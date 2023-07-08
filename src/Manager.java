import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Manager {
    private int numNodes;
    private int maxDeg;
    private List<Node> nodes;

    public Manager() {
        nodes = new ArrayList<>();
    }

    public void readInput(String path) {
        try {
            Scanner scanner = new Scanner(new File(path));
            numNodes = scanner.nextInt();
            maxDeg = scanner.nextInt();

            scanner.nextLine(); // Consume the newline character after maxDeg

            for (int i = 0; i < numNodes; i++) {
                int nodeId = i;
                int[][] neighborsInfo = parseNeighborsInfo(scanner);
                Node node = new Node(nodeId, numNodes, maxDeg, neighborsInfo);
                nodes.add(node);
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error: Input file not found.");
            e.printStackTrace();
        }
    }

    private int[][] parseNeighborsInfo(Scanner scanner) {
        String neighborsString = scanner.nextLine();
        neighborsString = neighborsString.substring(1); // Remove the leading space
        String[] neighborsArray = neighborsString.split("\\], \\[");

        int numNeighbors = neighborsArray.length;
        int[][] neighbors = new int[numNeighbors][3];

        for (int i = 0; i < numNeighbors; i++) {
            String neighborInfo = neighborsArray[i].replaceAll("[\\[\\]]", ""); // Remove square brackets
            String[] neighborParts = neighborInfo.split(",");

            int neighborId = Integer.parseInt(neighborParts[0].trim());
            int writingPort = Integer.parseInt(neighborParts[1].trim());
            int readingPort = Integer.parseInt(neighborParts[2].trim());

            neighbors[i][0] = neighborId;
            neighbors[i][1] = writingPort;
            neighbors[i][2] = readingPort;
        }

        return neighbors;
    }

    public String start() {
        Thread[] threads = new Thread[numNodes];
        for (int i = 0; i < numNodes; i++) {
            final int nodeId = i;
            Node node = nodes.get(nodeId);
            threads[nodeId] = new Thread(() -> node.run());
            threads[nodeId].start();
        }

        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Error: Thread interrupted.");
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        for (Node node : nodes) {
            sb.append(node.toString()).append("\n");
        }
        return sb.toString().trim();
    }

    public String terminate() {
        StringBuilder sb = new StringBuilder();
        for (Node node : nodes) {
            node.terminate();
            sb.append(node.toString()).append("\n");
        }
        return sb.toString().trim();
    }
}
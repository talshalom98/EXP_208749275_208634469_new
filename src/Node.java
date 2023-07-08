import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class Node implements Runnable {
    private int id;
    private int color;
    private int numNodes;
    private int maxDeg;
    private List<int[]> neighbors;
    private volatile boolean isTerminated;

    public Node(int id, int numNodes, int maxDeg, int[][] neighbors) {
        this.id = id;
        this.color = id;
        this.numNodes = numNodes;
        this.maxDeg = maxDeg;
        this.neighbors = List.of(neighbors);
        this.isTerminated = false;
    }

    @Override
    public void run() {
        boolean isColoringValid = false;
        int iterations = 0;

        while (!isColoringValid && !isTerminated) {
            iterations++;

            for (int[] neighbor : neighbors) {
                int neighborId = neighbor[0];
                int writingPort = neighbor[1];
                int readingPort = neighbor[2];

                sendMessage(neighborId, writingPort, id, color);

                int receivedId = receiveMessage(readingPort);
                int receivedColor = receiveMessage(readingPort);

                if (receivedId == neighborId && receivedColor == color) {
                    color = (color + 1) % (maxDeg + 1);
                }
            }

            isColoringValid = checkColoringValidity();

            if (iterations >= numNodes) {
                break;
            }
        }
    }

    private void sendMessage(int neighborId, int writingPort, int id, int color) {
        try (Socket socket = new Socket("localhost", writingPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeInt(id);
            out.writeInt(color);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int receiveMessage(int readingPort) {
        try (Socket socket = new Socket("localhost", readingPort);
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            return in.readInt();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private boolean checkColoringValidity() {
        for (int[] neighbor : neighbors) {
            int neighborId = neighbor[0];
            int readingPort = neighbor[2];
            int neighborColor = receiveMessage(readingPort);

            if (neighborColor == color) {
                return false;
            }
        }

        return true;
    }

    public void terminate() {
        isTerminated = true;
    }

    @Override
    public String toString() {
        return id + "," + color;
    }
}

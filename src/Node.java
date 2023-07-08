import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", writingPort), 5000); // Timeout of 5 seconds
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeInt(id);
            out.writeInt(color);
            out.flush();
        } catch (ConnectException e) {
            System.err.println("Connection refused. Make sure the server or node is running on the specified port.");
        } catch (SocketTimeoutException e) {
            System.err.println("Connection timeout. Check if there are any network issues or firewall restrictions.");
        } catch (IOException e) {
            System.err.println("Error occurred while sending message:");
            e.printStackTrace();
        }
    }

    private int receiveMessage(int readingPort) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", readingPort), 5000); // Timeout of 5 seconds
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            return in.readInt();
        } catch (ConnectException e) {
            System.err.println("Connection refused. Make sure the server or node is running on the specified port.");
        } catch (SocketTimeoutException e) {
            System.err.println("Connection timeout. Check if there are any network issues or firewall restrictions.");
        } catch (IOException e) {
            System.err.println("Error occurred while receiving message:");
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

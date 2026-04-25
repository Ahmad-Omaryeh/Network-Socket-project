import java.net.*;
import java.util.Scanner;

public class Client {
    private static final int SERVER_PORT = 5012;
    private static final String SERVER_IP = "localhost";

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
        Scanner scanner = new Scanner(System.in);

        // join to the game
        System.out.print("Enter your username: ");
        String username = scanner.nextLine().trim();
        String joinMessage = "JOIN " + username;
        sendMessage(socket, serverAddress, joinMessage);

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        boolean running = true;

        while (running) {
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Server: " + message);

            if (message.startsWith("ROUND:")) {
                System.out.print("Enter your number (1-100): ");
                String number = scanner.nextLine().trim();
                sendMessage(socket, serverAddress, number);
            } else if (message.contains("You lost")) {
                System.out.println("You have been eliminated from the game.");
                running = false;
            } else if (message.contains("winner") || message.contains("Game Over")) {
                System.out.println("Game ended.");
                running = false;
            }
          
        }

        scanner.close();
        socket.close();
    }

    private static void sendMessage(DatagramSocket socket, InetAddress address, String message) throws Exception {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, address, SERVER_PORT);
        socket.send(packet);
    }
}

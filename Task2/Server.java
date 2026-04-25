import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 5012;
    private static final int BUFFER_SIZE = 1024;
    private static final int MIN_PLAYERS = 2;

    private static Map<String, InetSocketAddress> clients = new HashMap<>();
    private static Set<Integer> usedNumbers = new HashSet<>();

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(PORT);
        byte[] buffer = new byte[BUFFER_SIZE];
        System.out.println("Server is running on port " + PORT);

        // wait for players (at least two players)
        while (true) {
            if (clients.size() >= MIN_PLAYERS) {
                broadcast(socket, "Game starting with " + clients.size() + " players.");
                break;
            }

            DatagramPacket joinPacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(joinPacket);

            String message = new String(joinPacket.getData(), 0, joinPacket.getLength()).trim();
            if (message.startsWith("JOIN")) {
                String username = message.substring(5).trim();
                InetSocketAddress clientAddr = new InetSocketAddress(joinPacket.getAddress(), joinPacket.getPort());

                if (!clients.containsKey(username)) {
                    clients.put(username, clientAddr);
                    System.out.println("Player joined: " + username);
                    sendMessage(socket, clientAddr, "Welcome " + username + "! Waiting for more players...");
                }
            }
        }

        // start the rounds
        while (clients.size() > 1 && usedNumbers.size() < 100) {
            System.out.println("Starting new round...");
            Map<String, Integer> roundNumbers = new HashMap<>();
            Set<String> eliminatedThisRound = new HashSet<>();
            Map<String, String> eliminationReasons = new HashMap<>();
            Set<String> respondedUsers = new HashSet<>();

            // send request for current players 
            for (String username : clients.keySet()) {
                sendMessage(socket, clients.get(username), "ROUND: Send a unique number between 1 and 100");
            }

            socket.setSoTimeout(60000); // waiting time = 60 sec (if timeout the server kick the player)

            // receving the requests
            while (respondedUsers.size() < clients.size()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    System.out.println("Time is up! Not all players responded.");
                    break;
                }

                String received = new String(packet.getData(), 0, packet.getLength()).trim();
                InetSocketAddress senderAddr = new InetSocketAddress(packet.getAddress(), packet.getPort());

                if (received.startsWith("JOIN")) {
                    String newUsername = received.substring(5).trim();
                    if (!clients.containsKey(newUsername)) {
                        clients.put(newUsername, senderAddr);
                        System.out.println("New player joined during game: " + newUsername);
                        sendMessage(socket, senderAddr, "Welcome " + newUsername + "! You joined mid-game.");
                        sendMessage(socket, senderAddr, "ROUND: Send a unique number between 1 and 100");
                    }
                    continue;
                }

                String username = getUsernameByAddress(senderAddr);
                if (username == null || eliminatedThisRound.contains(username) || respondedUsers.contains(username)) {
                    continue;
                }

                try {
                    int number = Integer.parseInt(received);
                    if (number < 1 || number > 100) {
                        eliminationReasons.put(username, "Invalid number");
                        eliminatedThisRound.add(username);
                    } else if (usedNumbers.contains(number) || roundNumbers.containsValue(number)) {
                        eliminationReasons.put(username, "Number already used");
                        eliminatedThisRound.add(username);
                    } else {
                        roundNumbers.put(username, number);
                        sendMessage(socket, senderAddr, "Number accepted.");
                        System.out.println(username + " submitted: " + number);
                    }
                } catch (NumberFormatException e) {
                    eliminationReasons.put(username, "Invalid input");
                    eliminatedThisRound.add(username);
                }

                respondedUsers.add(username);
            }

            // eliminate players who dont send a number (timeout)
            for (String name : clients.keySet()) {
                if (!respondedUsers.contains(name) && !eliminatedThisRound.contains(name)) {
                    eliminatedThisRound.add(name);
                    eliminationReasons.put(name, "No response (timeout)");
                }
            }

            usedNumbers.addAll(roundNumbers.values());

            // round sammary
            StringBuilder stats = new StringBuilder();
            stats.append("Round Summary:\n");
            stats.append("Remaining players:\n");
            for (String name : clients.keySet()) {
                if (!eliminatedThisRound.contains(name)) {
                    stats.append(" - ").append(name).append("\n");
                }
            }

            if (!eliminatedThisRound.isEmpty()) {
                stats.append("Eliminated players:\n");
                for (String name : eliminatedThisRound) {
                    stats.append(" - ").append(name)
                         .append(": ").append(eliminationReasons.get(name)).append("\n");
                }
            } else {
                stats.append("No players eliminated this round.\n");
            }

            broadcast(socket, stats.toString());

            // eliminate the lost players
            for (String name : eliminatedThisRound) {
                InetSocketAddress addr = clients.get(name);
                sendMessage(socket, addr, "You lost. Reason: " + eliminationReasons.get(name));
                clients.remove(name);
            }
        }

        // output a messege to the winner 
        if (clients.size() == 1) {
            String winner = clients.keySet().iterator().next();
            sendMessage(socket, clients.get(winner), "You are the winner!");
            System.out.println("Winner: " + winner);
        } else {
            broadcast(socket, "Game Over! Multiple winners:");
            for (String username : clients.keySet()) {
                System.out.println("Winner: " + username);
            }
        }

        socket.close();
    }

    private static void sendMessage(DatagramSocket socket, InetSocketAddress address, String message) throws Exception {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, address.getAddress(), address.getPort());
        socket.send(packet);
    }

    private static void broadcast(DatagramSocket socket, String message) throws Exception {
        for (InetSocketAddress addr : clients.values()) {
            sendMessage(socket, addr, message);
        }
    }

    private static String getUsernameByAddress(InetSocketAddress addr) {
        for (Map.Entry<String, InetSocketAddress> entry : clients.entrySet()) {
            if (entry.getValue().equals(addr)) return entry.getKey();
        }
        return null;
    }
}

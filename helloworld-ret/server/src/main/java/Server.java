import java.io.*;
import java.net.InetAddress;
import java.util.List;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        List<String> extraArgs = new ArrayList<>();

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.server", extraArgs)) {
            if (!extraArgs.isEmpty()) {
                System.err.println("too many arguments");
                for (String v : extraArgs) {
                    System.out.println(v);
                }
            }
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Printer");
            com.zeroc.Ice.Object object = new PrinterI();
            adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
            adapter.activate();
            communicator.waitForShutdown();
        }
    }

    public static void f(String m) {
        String str = null, output = "";

        try {
            Process p = Runtime.getRuntime().exec(m);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((str = br.readLine()) != null)
                output += str + System.getProperty("line.separator");
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String handleClientMessage(String message, String username, String hostname) {
        String response = "";
        if (message.matches("\\d+")) {
            int n = Integer.parseInt(message);
            response = username + ":" + hostname + ": Fibonacci series: " + fibonacci(n) + ", Prime factors: " + primeFactors(n);
        } else if (message.startsWith("listifs")) {
            response = username + ":" + hostname + ": " + listInterfaces();
        } else if (message.startsWith("listports")) {
            String[] parts = message.split(" ");
            if (parts.length > 1) {
                response = username + ":" + hostname + ": " + listPorts(parts[1]);
            } else {
                response = "Invalid command format for listports.";
            }
        } else if (message.startsWith("!")) {
            String command = message.substring(1);
            response = username + ":" + hostname + ": " + executeCommand(command);
        } else {
            response = "Unknown command.";
        }
        return response;
    }

    private static String fibonacci(int n) {
        List<Integer> series = new ArrayList<>();
        int a = 0, b = 1;
        for (int i = 0; i < n; i++) {
            series.add(a);
            int sum = a + b;
            a = b;
            b = sum;
        }
        return series.toString();
    }

    private static String primeFactors(int n) {
        List<Integer> factors = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            while (n % i == 0) {
                factors.add(i);
                n /= i;
            }
        }
        return factors.toString();
    }

    private static String listInterfaces() {
        StringBuilder result = new StringBuilder();
        try {
            InetAddress[] addresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            for (InetAddress address : addresses) {
                result.append(address.toString()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private static String listPorts(String ip) {
        // Implement logic to list open ports and services for the given IP address
        return "Listing ports for IP: " + ip;
    }

    private static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line).append("\n");
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}

import Demo.Response;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Current;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrinterI implements Demo.Printer {
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private Communicator communicator;

    private long startTime;
    private long latencyProcess;
    private int requestsReceived;
    private int requestsAnswered;

    private String msg;

    public PrinterI(Communicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public Response printString(String message, Current current) {
        requestsReceived += 1;
        this.msg = message;

        System.out.println("Mensaje recibido: " + message);
        String[] parts = message.split(":", 3);
        String command = parts.length > 2 ? parts[2] : "";

        startTime = System.currentTimeMillis(); // Inicia la medición de tiempo

        String response;
        if (command.matches("\\d+")) {
            int n = Integer.parseInt(command);
            response = "Fibonacci series: " + fibonacci(n) + ", Prime factors: " + primeFactors(n);
        } else if (command.startsWith("listifs")) {
            response = listInterfaces();
        } else if (command.startsWith("listports")) {
            String ipAddress = command.split(" ")[1];
            response = "Listing ports in progress for IP: " + ipAddress;
            listPorts(ipAddress);
        } else if (command.startsWith("!")) {
            response = executeCommand(command.substring(1));
        } else if (command.equalsIgnoreCase("exit")) {
            communicator.shutdown();
            response = "Server shutting down...";
        } else {
            response = "Unknown command.";
        }

        updatePerformanceMetrics();
        return new Response(System.currentTimeMillis(), response + getPerformance());
    }

    private void updatePerformanceMetrics() {
        latencyProcess = System.currentTimeMillis() - startTime;
        requestsAnswered += 1;
    }

    private String fibonacci(int n) {
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

    private String primeFactors(int n) {
        List<Integer> factors = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            while (n % i == 0) {
                factors.add(i);
                n /= i;
            }
        }
        return factors.toString();
    }

    private String listInterfaces() {
        StringBuilder result = new StringBuilder();
        try {
            InetAddress[] addresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            for (InetAddress address : addresses) {
                result.append(address.toString()).append("\n");
            }
        } catch (Exception e) {
            result.append("Error al listar interfaces: ").append(e.getMessage()).append("\n");
        }
        return result.toString();
    }

    private void listPorts(String ipAddress) {
        executorService.submit(() -> {
            StringBuilder result = new StringBuilder();
            try {
                for (int port = 9000; port <= 10000; port++) {
                    try (Socket socket = new Socket(ipAddress, port)) {
                        result.append("Port ").append(port).append(" is open on ").append(ipAddress).append("\n");
                    } catch (Exception e) {
                        // Port is closed or filtered, ignore
                    }
                }
                System.out.println("Listing ports complete for IP: " + ipAddress + "\n" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private String executeCommand(String command) {
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
            output.append("Error al ejecutar el comando: ").append(e.getMessage()).append("\n");
        }
        return output.toString();
    }

    private String getPerformance() {
        return getRequestsReceived() + getRequestsAnswered() + getLatencyProcess() + getThroughput();
    }

    private String getRequestsReceived() {
        return "\nRequests received (server): " + requestsReceived;
    }

    private String getRequestsAnswered() {
        return "\nUnprocessed rate: " + (requestsReceived - requestsAnswered);
    }

    private String getLatencyProcess() {
        return "\nLatency (process): " + latencyProcess + "ms";
    }

    private String getThroughput() {
        long start = System.currentTimeMillis();
        int executedCommands = 0;

        while (true) {
            String r = simulateCommandProcessing(msg);
            if (System.currentTimeMillis() - start >= 1000) {
                break;
            }
            executedCommands++;
        }

        return "\nThroughput (of this command): " + executedCommands;
    }

    private String simulateCommandProcessing(String msg) {
        String[] msgParts = msg.split(" ");

        if (msgParts.length == 1) {
            return "Ups! Type a valid message";
        }

        String realMsg = msgParts[1];

        try {
            Long num = Long.parseLong(realMsg);

            if (num <= 0) {
                return "Ups! Type a valid number";
            }

            return primeFactors(num.intValue());
        } catch (NumberFormatException e) {
            return "Ups! Type a valid message";
        }
    }
}
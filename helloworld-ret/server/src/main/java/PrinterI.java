import Demo.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class PrinterI implements Demo.Printer {
    @Override
    public Response printString(String message, com.zeroc.Ice.Current current) {
        System.out.println("Mensaje recibido: " + message);

        // Separar el prefijo (username y hostname) del mensaje real
        String[] parts = message.split(":", 3);
        String command = parts.length > 2 ? parts[2] : "";

        String response;
        if (command.matches("\\d+")) {
            int n = Integer.parseInt(command);
            response = "Fibonacci series: " + fibonacci(n) + ", Prime factors: " + primeFactors(n);
        } else if (command.startsWith("listifs")) {
            response = listInterfaces();
        } else if (command.startsWith("listports")) {
            if (command.split(" ").length > 1) {
                String ip = command.split(" ")[1];
                response = listPorts(ip);
            } else {
                response = "Invalid command format for listports.";
            }
        } else if (command.startsWith("!")) {
            response = executeCommand(command.substring(1));
        } else {
            response = "Unknown command.";
        }

        return new Response(System.currentTimeMillis(), response);
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
            e.printStackTrace();
        }
        return result.toString();
    }

    private String listPorts(String ip) {
        //se puede mejorar 
        return "Listing ports for IP: " + ip;
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
            e.printStackTrace();
        }
        return output.toString();
    }
}

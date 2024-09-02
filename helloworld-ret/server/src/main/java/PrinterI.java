import Demo.Response;
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

    @Override
    public Response printString(String message, com.zeroc.Ice.Current current) {
        System.out.println("Mensaje recibido: " + message);
        String[] parts = message.split(":", 3);
        String command = parts.length > 2 ? parts[2] : "";

        long startTime = System.nanoTime(); // Inicia la medición de tiempo

        if (command.matches("\\d+")) {
            int n = Integer.parseInt(command);
            String response = "Fibonacci series: " + fibonacci(n) + ", Prime factors: " + primeFactors(n);
            long endTime = System.nanoTime(); // Termina la medición de tiempo
            System.out.println("Tiempo para calcular Fibonacci y factores primos: " + (endTime - startTime) + " nanosegundos");
            return new Response(System.currentTimeMillis(), response);
        } else if (command.startsWith("listifs")) {
            String response = listInterfaces();
            long endTime = System.nanoTime(); // Termina la medición de tiempo
            System.out.println("Tiempo para listar interfaces: " + (endTime - startTime) + " nanosegundos");
            return new Response(System.currentTimeMillis(), response);
        } else if (command.startsWith("listports")) {
            String ipAddress = command.split(" ")[1];
            listPorts(ipAddress);
            long endTime = System.nanoTime(); // Termina la medición de tiempo
            System.out.println("Tiempo para iniciar listado de puertos: " + (endTime - startTime) + " nanosegundos");
            return new Response(System.currentTimeMillis(), "Listing ports in progress for IP: " + ipAddress);
        } else if (command.startsWith("!")) {
            String response = executeCommand(command.substring(1));
            long endTime = System.nanoTime(); // Termina la medición de tiempo
            System.out.println("Tiempo para ejecutar el comando: " + (endTime - startTime) + " nanosegundos");
            return new Response(System.currentTimeMillis(), response);
        } else {
            long endTime = System.nanoTime(); // Termina la medición de tiempo
            System.out.println("Tiempo para procesar comando desconocido: " + (endTime - startTime) + " nanosegundos");
            return new Response(System.currentTimeMillis(), "Unknown command.");
        }
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

    private void listPorts(String ipAddress) {
        System.out.println("Entro");
        executorService.submit(() -> {
            long startTime = System.nanoTime(); // Inicia la medición de tiempo
            StringBuilder result = new StringBuilder();
            try {
                for (int port = 9000; port <= 10000; port++) {
                    try (Socket socket = new Socket(ipAddress, port)) {
                        result.append("Port ").append(port).append(" is open on ").append(ipAddress).append("\n");
                    } catch (Exception e) {
                        // Port is closed or filtered, ignore
                    }
                }
                long endTime = System.nanoTime(); // Termina la medición de tiempo
                System.out.println("Tiempo para listar puertos: " + (endTime - startTime) + " nanosegundos");
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
            e.printStackTrace();
        }
        return output.toString();
    }
}

import Demo.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.client", extraArgs)) {
            Demo.PrinterPrx service = Demo.PrinterPrx.checkedCast(communicator.propertyToProxy("Printer.Proxy"));

            if (service == null) {
                throw new Error("Invalid proxy");
            }

            // Obtener el nombre de usuario y el hostname
            String username = System.getProperty("user.name");
            String hostname = java.net.InetAddress.getLocalHost().getHostName();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Ingrese un mensaje: ");
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                // Anteponer el username y hostname al mensaje
                String prefixedMessage = username + ":" + hostname + ":" + message;
                Response response = service.printString(prefixedMessage);

                System.out.println("Respuesta del servidor: " + response.value + ", " + response.responseTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

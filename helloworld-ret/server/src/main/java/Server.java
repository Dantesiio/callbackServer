import java.util.ArrayList;
import java.util.List;

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

            // Creación del adaptador para manejar las solicitudes
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Printer");

            // Creación del objeto PrinterI con el Communicator
            com.zeroc.Ice.Object object = new PrinterI(communicator);

            // Añadir el objeto al adaptador con un nombre de identidad
            adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));

            // Activación del adaptador para aceptar solicitudes
            adapter.activate();

            // Esperar a que el servidor se apague
            communicator.waitForShutdown();
        }
    }
}
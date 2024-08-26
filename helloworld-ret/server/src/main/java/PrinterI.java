import Demo.Response;

public class PrinterI implements Demo.Printer
{
    public Response printString(String s, com.zeroc.Ice.Current current)
    {
        System.out.println(s);
        return new Response(0, "Server response: " + s);
    }
}
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class CalculatorImpl extends UnicastRemoteObject implements Calculator {
    
    // Constructor with exception handling
    protected CalculatorImpl() throws RemoteException {
        super();
    }
    
    @Override
    public double add(double firstValue, double secondValue) throws RemoteException {
        return firstValue + secondValue;
    }
    
    @Override
    public double subtract(double firstValue, double secondValue) throws RemoteException {
        return firstValue - secondValue;
    }
    
    @Override
    public double multiply(double firstValue, double secondValue) throws RemoteException {
        return firstValue * secondValue;
    }
    
    @Override
    public double divide(double numerator, double denominator) throws RemoteException {
        if (Math.abs(denominator) < 0.0000001) {
            throw new ArithmeticException("Division by zero is undefined");
        }
        return numerator / denominator;
    }
    
    public static void main(String[] args) {
        try {
            // Create RMI registry on default port
            LocateRegistry.createRegistry(1099);
            
            // Create and bind calculator service
            CalculatorImpl calculatorService = new CalculatorImpl();
            Naming.rebind("rmi://localhost:1099/CALCULATOR", calculatorService);
            
            System.out.println("Remote Calculator Service is operational and ready for connections...");
            
        } catch (Exception serverError) {
            System.err.println("Server initialization failed:");
            serverError.printStackTrace();
        }
    }
}
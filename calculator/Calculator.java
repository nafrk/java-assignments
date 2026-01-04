import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Calculator extends Remote {
    
    // Arithmetic operation methods
    double add(double firstOperand, double secondOperand) throws RemoteException;
    
    double subtract(double firstOperand, double secondOperand) throws RemoteException;
    
    double multiply(double firstOperand, double secondOperand) throws RemoteException;
    
    double divide(double dividend, double divisor) throws RemoteException;
}
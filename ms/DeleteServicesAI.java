import java.rmi.*;

public interface DeleteServicesAI extends java.rmi.Remote
{
    /*******************************************************
    * Deletes the order corresponding to the order id
    * Returns a success message or error string
    *******************************************************/
    String deleteOrder(String id) throws RemoteException;
}   
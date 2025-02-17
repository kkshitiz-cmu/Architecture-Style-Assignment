import java.rmi.*;

public interface DeleteServicesAI extends java.rmi.Remote
{
    String deleteOrder(String id) throws RemoteException;
}
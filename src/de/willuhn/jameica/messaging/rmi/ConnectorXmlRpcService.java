/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/ConnectorXmlRpcService.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/05/29 16:24:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.rmi;

import java.rmi.RemoteException;
import java.util.HashMap;

import de.willuhn.datasource.Service;

/**
 * XML-RPC-tauglicher Connector.
 */
public interface ConnectorXmlRpcService extends Service
{
  /**
   * Loescht die Nachricht mit der angegebenen UUID.
   * @param uuid ID der Nachricht.
   * @return true, wenn die Nachricht gefunden und geloescht wurde,
   * false, wenn die Nachricht nicht existierte.
   * @throws RemoteException
   */
  public boolean delete(String uuid) throws RemoteException;

  /**
   * Liefert die naechste UUID aus dem Channel.
   * @param channel Name des Channel.
   * @return UUID oder NULL, wenn keine Nachricht im Channel vorliegt.
   * @throws RemoteException
   */
  public String next(String channel) throws RemoteException;

  /**
   * Liefert die Liste der UUIDs aus dem Channel.
   * @param channel Name des Channel.
   * @return Liste der UUIDs oder NULL, wenn keine Nachrichten im Channel vorliegen.
   * @throws RemoteException
   */
  public String[] list(String channel) throws RemoteException;

  /**
   * Uebergibt eine Nachricht an die Queue.
   * @param channel Name des Channels. 
   * Punkte koennen als Trennzeichen fuer Sub-Channels verwendet werden (wie bei Java-Packages).
   * @param data die Nutzdaten.
   * @param properties optionale Map mit beliebigen Attributen, die mitgespeichert werden sollen.
   * @return eine UUID, anhand derer die Message auch identifiziert werden kann.
   * @throws RemoteException
   */
  public String put(String channel, byte[] data, HashMap<String,String> properties) throws RemoteException;
  
  /**
   * Liefert die Nachricht mit der angegebenen UUID.
   * @param uuid ID der Nachricht.
   * @return die Nachricht oder <code>null</code> wenn die Nachricht nicht gefunden wurde.
   * @throws RemoteException
   */
  public byte[] get(String uuid) throws RemoteException;

  /**
   * Liefert die Properties zur angegebenen UUID.
   * @param uuid ID der Nachricht.
   * @return die Properties oder <code>null</code> wenn die Nachricht nicht gefunden wurde.
   * @throws RemoteException
   */
  public HashMap<String,String> getProperties(String uuid) throws RemoteException;
}


/*********************************************************************
 * $Log: ConnectorXmlRpcService.java,v $
 * Revision 1.3  2009/05/29 16:24:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2008/10/08 23:18:38  willuhn
 * @B bugfixing
 * @N SoapTest
 *
 * Revision 1.1  2008/10/07 23:03:34  willuhn
 * @C "queue" und "archive" entfernt. Zugriff jetzt direkt ueber Connectoren
 *
 **********************************************************************/
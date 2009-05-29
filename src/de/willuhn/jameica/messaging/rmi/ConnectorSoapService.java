/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/ConnectorSoapService.java,v $
 * $Revision: 1.2 $
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

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import de.willuhn.datasource.Service;

/**
 * SOAP-tauglicher Connector.
 */
@WebService(name="Message")
public interface ConnectorSoapService extends Service
{
  /**
   * Loescht die Nachricht mit der angegebenen UUID.
   * @param uuid ID der Nachricht.
   * @return true, wenn die Nachricht gefunden und geloescht wurde,
   * false, wenn die Nachricht nicht existierte.
   * @throws RemoteException
   */
  public @WebResult(name="success") boolean delete(@WebParam(name="uuid") String uuid) throws RemoteException;

  /**
   * Liefert die naechste UUID aus dem Channel.
   * @param channel Name des Channel.
   * @return UUID oder NULL, wenn keine Nachricht im Channel vorliegt.
   * @throws RemoteException
   */
  public @WebResult(name="uuid") String next(@WebParam(name="channel") String channel) throws RemoteException;
  
  /**
   * Uebergibt eine Nachricht an die Queue.
   * @param channel Name des Channels. 
   * Punkte koennen als Trennzeichen fuer Sub-Channels verwendet werden (wie bei Java-Packages).
   * @param data die Nutzdaten.
   * @param properties optionale Map mit beliebigen Attributen, die mitgespeichert werden sollen.
   * @return eine UUID, anhand derer die Message auch identifiziert werden kann.
   * @throws RemoteException
   */
  public @WebResult(name="uuid") String put(@WebParam(name="channel") String channel, @WebParam(name="data") byte[] data, @WebParam(name="properties") HashMap<String,String> properties) throws RemoteException;
  
  /**
   * Liefert die Nachricht mit der angegebenen UUID.
   * @param uuid ID der Nachricht.
   * @return die Nachricht oder <code>null</code> wenn die Nachricht nicht gefunden wurde.
   * @throws RemoteException
   */
  public @WebResult(name="data") byte[] get(@WebParam(name="uuid") String uuid) throws RemoteException;

  /**
   * Liefert die Properties zur angegebenen UUID.
   * @param uuid ID der Nachricht.
   * @return die Properties oder <code>null</code> wenn die Nachricht nicht gefunden wurde.
   * @throws RemoteException
   */
  public @WebResult(name="properties") HashMap<String,String> getProperties(@WebParam(name="uuid") String uuid) throws RemoteException;
}


/*********************************************************************
 * $Log: ConnectorSoapService.java,v $
 * Revision 1.2  2009/05/29 16:24:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2008/10/08 23:18:38  willuhn
 * @B bugfixing
 * @N SoapTest
 *
 * Revision 1.1  2008/10/08 17:55:11  willuhn
 * @N SOAP-Connector (in progress)
 *
 **********************************************************************/
/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/services/Attic/ConnectorSoapService.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/10/08 17:55:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.services;

import java.rmi.RemoteException;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import de.willuhn.datasource.Service;

/**
 * SOAP-tauglicher Connector.
 */
@WebService
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
  public @WebResult(name="uuid") String put(@WebParam(name="channel") String channel, @WebParam(name="data") byte[] data, @WebParam(name="properties") Map properties) throws RemoteException;
  
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
  public @WebResult(name="properties") Map getProperties(@WebParam(name="uuid") String uuid) throws RemoteException;
}


/*********************************************************************
 * $Log: ConnectorSoapService.java,v $
 * Revision 1.1  2008/10/08 17:55:11  willuhn
 * @N SOAP-Connector (in progress)
 *
 **********************************************************************/
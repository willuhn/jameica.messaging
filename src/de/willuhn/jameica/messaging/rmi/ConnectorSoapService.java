/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
   * Liefert die Liste der UUIDs aus dem Channel.
   * @param channel Name des Channel.
   * @return Liste der UUIDs oder NULL, wenn keine Nachrichten im Channel vorliegen.
   * @throws RemoteException
   */
  public @WebResult(name="list") String[] list(@WebParam(name="channel") String channel) throws RemoteException;
  
  /**
   * Uebergibt eine Nachricht an die Queue.
   * @param channel Name des Channels. 
   * Punkte koennen als Trennzeichen fuer Sub-Channels verwendet werden (wie bei Java-Packages).
   * @param data die Nutzdaten.
   * @param properties optionale Map mit beliebigen Attributen, die mitgespeichert werden sollen.
   * @return eine UUID, anhand derer die Message auch identifiziert werden kann.
   * @throws RemoteException
   */
  public @WebResult(name="uuid") String put(@WebParam(name="channel") String channel,
                                            @WebParam(name="data") byte[] data, 
                                            @WebParam(name="properties") HashMap<String,String> properties)
    throws RemoteException;
  
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

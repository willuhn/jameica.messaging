/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/Attic/MessageService.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/01/16 16:44:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.rmi;

import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import de.willuhn.datasource.Service;

/**
 * Basis-Service fuer den asynchronen Nachrichtenaustausch.
 * Jameica-Instanz A kann Nachrichten hier abliefern, Instanz B kann sie
 * irgendwann abrufen.
 */
public interface MessageService extends Service
{
  /**
   * Uebergibt eine Nachricht an die Queue.
   * @param channel Name des Channels. 
   * Punkte koennen als Trennzeichen fuer Sub-Channels verwendet werden (wie bei Java-Packages).
   * @param data die Nutzdaten.
   * @return eine UUID, anhand derer die Message auch identifiziert werden kann.
   * @throws RemoteException
   */
  public String put(String channel, byte[] data) throws RemoteException;
  
  /**
   * Uebergibt eine Nachricht an die Queue.
   * @param channel Name des Channels.
   * Punkte koennen als Trennzeichen fuer Sub-Channels verwendet
   * werden (wie bei Java-Packages).
   * @param is die Nutzdaten.
   * @return eine UUID, anhand derer die Message auch identifiziert werden kann.
   * @throws RemoteException
   */
  public String put(String channel, InputStream is) throws RemoteException;

  /**
   * Liefert die Nachricht mit der angegebenen UUID ab.
   * @param uuid ID der Nachricht.
   * @return die Nachricht oder <code>null</code> wenn die Nachricht nicht gefunden wurde.
   * @throws RemoteException
   */
  public byte[] get(String uuid) throws RemoteException;

  /**
   * Liefert die Nachricht mit der angegebenen UUID ab.
   * @param uuid ID der Nachricht.
   * @param os Der Stream, in den die Nachricht geschrieben wird.
   * @throws RemoteException
   */
  public void get(String uuid, OutputStream os) throws RemoteException;
}


/*********************************************************************
 * $Log: MessageService.java,v $
 * Revision 1.1  2008/01/16 16:44:47  willuhn
 * @N Verwendung von UUIDs fuer die Vergabe der Dateinamen
 * @N Doppel-Funktion des Systems als Archiv und Queue
 *
 **********************************************************************/
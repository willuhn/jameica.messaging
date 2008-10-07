/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/Attic/MessageService.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/10/07 00:11:09 $
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
import java.util.Map;

import de.willuhn.datasource.Service;

/**
 * Basis-Interface fuer einen Message-Service.
 */
public interface MessageService extends Service
{
  /**
   * Uebergibt eine Nachricht an die Queue.
   * @param channel Name des Channels. 
   * Punkte koennen als Trennzeichen fuer Sub-Channels verwendet werden (wie bei Java-Packages).
   * @param data die Nutzdaten.
   * @param properties optionale Map mit beliebigen Attributen, die mitgespeichert werden sollen.
   * @return eine UUID, anhand derer die Message auch identifiziert werden kann.
   * @throws RemoteException
   */
  public String put(String channel, byte[] data, Map properties) throws RemoteException;
  
  /**
   * Liefert die Nachricht mit der angegebenen UUID.
   * @param uuid ID der Nachricht.
   * @return die Nachricht oder <code>null</code> wenn die Nachricht nicht gefunden wurde.
   * @throws RemoteException
   */
  public byte[] get(String uuid) throws RemoteException;
}


/*********************************************************************
 * $Log: MessageService.java,v $
 * Revision 1.3  2008/10/07 00:11:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2008/10/06 23:41:55  willuhn
 * @N Support fuer Properties in Messages
 *
 * Revision 1.1  2008/01/16 16:44:47  willuhn
 * @N Verwendung von UUIDs fuer die Vergabe der Dateinamen
 * @N Doppel-Funktion des Systems als Archiv und Queue
 *
 **********************************************************************/
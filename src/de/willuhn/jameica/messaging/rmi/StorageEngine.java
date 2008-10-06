/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/Attic/StorageEngine.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/10/06 23:30:45 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.rmi;

import java.io.IOException;

import de.willuhn.jameica.messaging.server.Message;

/**
 * Interface fuer den Storage-Service.
 * Damit kann die Queue verschiedene Backends zum Speichern der Messages nutzen.
 */
public interface StorageEngine
{
  /**
   * Ruft die Nachricht ab.
   * @param message die Nachricht.
   * @throws IOException
   */
  public void get(Message message) throws IOException;

  /**
   * Liefert die UUID der naechsten Nachricht in diesem Channel oder null.
   * @param channel Name des Channels. 
   * @return eine UUID fuer diese Nachricht.
   * @throws IOException
   */
  public String next(String channel) throws IOException;
  
  /**
   * Loescht die Nachricht mit der angegebenen UUID.
   * @param message die Message.
   * @throws IOException
   */
  public void delete(Message message) throws IOException;

  /**
   * Uebergibt eine Nachricht an den Speicher.
   * @param channel Name des Channels. 
   * Punkte koennen als Trennzeichen fuer Sub-Channels verwendet
   * werden (wie bei Java-Packages).
   * @param message die Nachricht.
   * @throws IOException
   */
  public void put(String channel, Message message) throws IOException;
}


/*********************************************************************
 * $Log: StorageEngine.java,v $
 * Revision 1.3  2008/10/06 23:30:45  willuhn
 * @N Support fuer Properties in Messages
 *
 * Revision 1.2  2008/01/16 16:44:47  willuhn
 * @N Verwendung von UUIDs fuer die Vergabe der Dateinamen
 * @N Doppel-Funktion des Systems als Archiv und Queue
 *
 * Revision 1.1  2007/12/14 11:28:08  willuhn
 * @N Storage-Engine
 *
 **********************************************************************/
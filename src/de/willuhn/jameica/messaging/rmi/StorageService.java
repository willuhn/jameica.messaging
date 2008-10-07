/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/StorageService.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/10/07 23:03:34 $
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

import de.willuhn.datasource.Service;
import de.willuhn.jameica.messaging.MessageData;

/**
 * Interface fuer den Storage-Service.
 * Damit kann die Queue verschiedene Backends zum Speichern der Messages nutzen.
 */
public interface StorageService extends Service
{
  /**
   * Ruft die Nachricht ab.
   * @param message die Nachricht.
   * @throws IOException
   */
  public void get(MessageData message) throws IOException;

  /**
   * Ruft nur die Properties der Nachricht ab.
   * @param message die Nachricht.
   * @throws IOException
   */
  public void getProperties(MessageData message) throws IOException;

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
  public void delete(MessageData message) throws IOException;

  /**
   * Uebergibt eine Nachricht an den Speicher.
   * @param channel Name des Channels. 
   * Punkte koennen als Trennzeichen fuer Sub-Channels verwendet
   * werden (wie bei Java-Packages).
   * @param message die Nachricht.
   * @throws IOException
   */
  public void put(String channel, MessageData message) throws IOException;
}


/*********************************************************************
 * $Log: StorageService.java,v $
 * Revision 1.1  2008/10/07 23:03:34  willuhn
 * @C "queue" und "archive" entfernt. Zugriff jetzt direkt ueber Connectoren
 *
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
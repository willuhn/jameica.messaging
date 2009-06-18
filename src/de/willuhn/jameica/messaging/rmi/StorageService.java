/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/StorageService.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/06/18 09:50:53 $
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
   * Speichert neue Properties zu einer Nachricht.
   * @param message die Nachricht.
   * @throws IOException
   */
  public void setProperties(MessageData message) throws IOException;

  /**
   * Liefert die UUID der naechsten Nachricht in diesem Channel oder null.
   * @param channel Name des Channels. 
   * @return eine UUID fuer diese Nachricht.
   * @throws IOException
   */
  public String next(String channel) throws IOException;
  
  /**
   * Liefert eine Liste der UUIDs in diesem Channel.
   * @param channel Name des Channels. 
   * @return Liste der UUIDs.
   * @throws IOException
   */
  public String[] list(String channel) throws IOException;
  
  /**
   * Liefert eine Liste der Sub-Channels in diesem Channel.
   * @param channel Name des Channels. 
   * @return Liste der Sub-Channels.
   * @throws IOException
   */
  public String[] listChannels(String channel) throws IOException;

  /**
   * Legt explizit einen neuen Channel an.
   * Der Aufruf der Funktion ist normalerweise nicht noetig,
   * das Channels implizit existieren, wenn sie verwendet werden.
   * @param channel Name des Channel.
   * @throws IOException
   */
  public void create(String channel) throws IOException;

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
 * Revision 1.4  2009/06/18 09:50:53  willuhn
 * @N zwei neue Kommandos (getmeta und putmeta) zum Lesen und Schreiben der Properties
 *
 * Revision 1.3  2009/06/03 14:35:14  willuhn
 * @N WebDAV-Connector (in progress)
 *
 * Revision 1.2  2009/06/02 23:24:52  willuhn
 * @N Funktion, die eine Liste aller UUIDs in einem Channel liefert
 *
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
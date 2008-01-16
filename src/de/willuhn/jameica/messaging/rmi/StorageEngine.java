/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/Attic/StorageEngine.java,v $
 * $Revision: 1.2 $
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface fuer den Storage-Service.
 * Damit kann die Queue verschiedene Backends zum Speichern der Messages nutzen.
 */
public interface StorageEngine
{
  /**
   * Ruft die Nachricht ab.
   * @param uuid ID der Nachricht.
   * @param os OutputStream, in den die Message geschrieben wird.
   * Der Stream wird <b>NICHT</b> von der Storage-Engine geschlossen,
   * das muss also vom Aufrufer getan werden.
   * @throws IOException
   */
  public void get(String uuid, OutputStream os) throws IOException;

  /**
   * Liefert die UUID der naechsten Nachricht in diesem Channel oder null.
   * @param channel Name des Channels. 
   * @return eine UUID fuer diese Nachricht.
   * @throws IOException
   */
  public String next(String channel) throws IOException;
  
  /**
   * Loescht die Nachricht mit der angegebenen UUID.
   * @param uuid UUID.
   * @throws IOException
   */
  public void delete(String uuid) throws IOException;

  /**
   * Uebergibt eine Nachricht an den Speicher.
   * @param channel Name des Channels. 
   * Punkte koennen als Trennzeichen fuer Sub-Channels verwendet
   * werden (wie bei Java-Packages).
   * @param is InputStream, von dem die Nachricht gelesen wird.
   * Der Stream wird <b>NICHT</b> von der Storage-Engine geschlossen,
   * das muss also vom Aufrufer getan werden.
   * @return eine UUID fuer diese Nachricht.
   * @throws IOException
   */
  public String put(String channel, InputStream is) throws IOException;
}


/*********************************************************************
 * $Log: StorageEngine.java,v $
 * Revision 1.2  2008/01/16 16:44:47  willuhn
 * @N Verwendung von UUIDs fuer die Vergabe der Dateinamen
 * @N Doppel-Funktion des Systems als Archiv und Queue
 *
 * Revision 1.1  2007/12/14 11:28:08  willuhn
 * @N Storage-Engine
 *
 **********************************************************************/
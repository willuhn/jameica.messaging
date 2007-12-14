/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/Attic/StorageEngine.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/12/14 11:28:08 $
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
   * Ruft die naechste vorliegende Nachricht ab.
   * @param channel Channel.
   * @param os OutputStream, in den die Message geschrieben wird.
   * Der Stream wird <b>NICHT</b> von der Storage-Engine geschlossen,
   * das muss also vom Aufrufer getan werden.
   * @throws IOException
   */
  public void get(String channel, OutputStream os) throws IOException;

  /**
   * Uebergibt eine Nachricht an die Queue.
   * @param channel Name des Channels. Der Name sollte nur
   * aus Buchstaben, Zahlen und Punkten bestehen.
   * Punkte koennen als Trennzeichen fuer Sub-Channels verwendet
   * werden (wie bei Java-Packages).
   * @param is InputStream, von dem die Nachricht gelesen wird.
   * Der Stream wird <b>NICHT</b> von der Storage-Engine geschlossen,
   * das muss also vom Aufrufer getan werden.
   * @throws IOException
   */
  public void put(String channel, InputStream is) throws IOException;
}


/*********************************************************************
 * $Log: StorageEngine.java,v $
 * Revision 1.1  2007/12/14 11:28:08  willuhn
 * @N Storage-Engine
 *
 **********************************************************************/
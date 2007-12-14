/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/Attic/QueueService.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/12/14 12:04:08 $
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
 * Ueber diesen Service koennen Nachrichten asynchron ausgetauscht werden.
 * Jameica-Instanz A kann Nachrichten hier abliefern, Instanz B kann sie
 * irgendwann abrufen.
 */
public interface QueueService extends Service
{
  /**
   * Uebergibt eine Nachricht an die Queue.
   * @param channel Name des Channels. Der Name sollte nur
   * aus Buchstaben, Zahlen und Punkten bestehen.
   * Punkte koennen als Trennzeichen fuer Sub-Channels verwendet
   * werden (wie bei Java-Packages).
   * @param data die Nutzdaten.
   * @return true, wenn die Nachricht gespeichert werden konnte.
   * Ist ein Zugestaendnis an XML-RPC, da dort ein Rueckgabe-Wert Pflicht ist.
   * @throws RemoteException
   */
  public boolean put(String channel, byte[] data) throws RemoteException;
  
  /**
   * Uebergibt eine Nachricht an die Queue.
   * @param channel Name des Channels. Der Name sollte nur
   * aus Buchstaben, Zahlen und Punkten bestehen.
   * Punkte koennen als Trennzeichen fuer Sub-Channels verwendet
   * werden (wie bei Java-Packages).
   * @param is die Nutzdaten.
   * @throws RemoteException
   */
  public void put(String channel, InputStream is) throws RemoteException;

  /**
   * Ruft die naechste vorliegende Nachricht ab.
   * @param channel Channel.
   * @return die naechste Nachricht oder <code>null</code> wenn keine weiteren Nachrichten vorliegen.
   * @throws RemoteException
   */
  public byte[] get(String channel) throws RemoteException;

  /**
   * Ruft die naechste vorliegende Nachricht ab.
   * @param channel Channel.
   * @param os Der Stream, in den die Nachricht geschrieben wird.
   * @throws RemoteException
   */
  public void get(String channel, OutputStream os) throws RemoteException;

}


/*********************************************************************
 * $Log: QueueService.java,v $
 * Revision 1.3  2007/12/14 12:04:08  willuhn
 * @C TCP-Listener verwendet jetzt Stream-API
 *
 * Revision 1.2  2007/12/14 09:56:59  willuhn
 * @N Channel-Angabe mit Punkt-Notation
 *
 * Revision 1.1  2007/12/13 23:31:38  willuhn
 * @N initial import
 *
 * Revision 1.1  2007/12/13 16:11:51  willuhn
 * @N Generischer Message-Queue-Service
 *
 **********************************************************************/
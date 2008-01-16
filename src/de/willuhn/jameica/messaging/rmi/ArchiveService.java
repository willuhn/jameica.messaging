/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/Attic/ArchiveService.java,v $
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

import java.rmi.RemoteException;


/**
 * Im Gegensatz zum QueueService werden die Nachrichten hier nicht
 * automatisch geloescht sondern erst explizit durch Aufruf von
 * "delete".
 */
public interface ArchiveService extends MessageService
{
  /**
   * Loescht die Nachricht mit der angegebenen UUID.
   * @param uuid ID der Nachricht.
   * @return true, wenn die Nachricht gefunden und geloescht wurde,
   * false, wenn die Nachricht nicht existierte.
   * @throws RemoteException
   */
  public boolean delete(String uuid) throws RemoteException;
}


/*********************************************************************
 * $Log: ArchiveService.java,v $
 * Revision 1.1  2008/01/16 16:44:47  willuhn
 * @N Verwendung von UUIDs fuer die Vergabe der Dateinamen
 * @N Doppel-Funktion des Systems als Archiv und Queue
 *
 **********************************************************************/
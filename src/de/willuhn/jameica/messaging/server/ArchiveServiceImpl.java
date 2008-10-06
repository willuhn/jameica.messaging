/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/Attic/ArchiveServiceImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/10/06 23:30:45 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.server;

import java.io.IOException;
import java.rmi.RemoteException;

import de.willuhn.jameica.messaging.rmi.ArchiveService;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Acrhiv-Services.
 */
public class ArchiveServiceImpl extends AbstractMessageServiceImpl implements
    ArchiveService
{

  /**
   * ct
   * @throws RemoteException
   */
  public ArchiveServiceImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.ArchiveService#delete(java.lang.String)
   */
  public boolean delete(String uuid) throws RemoteException
  {
    try
    {
      Message msg = new Message();
      msg.setUuid(uuid);
      this.storage.delete(msg);
      return true;
    }
    catch (IOException e)
    {
      // Loggen wir nur - das ist nicht weiter tragisch
      Logger.error("unable to delete message [UUID: " + uuid + "]",e);
    }
    return false;
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "archive";
  }

}


/*********************************************************************
 * $Log: ArchiveServiceImpl.java,v $
 * Revision 1.2  2008/10/06 23:30:45  willuhn
 * @N Support fuer Properties in Messages
 *
 * Revision 1.1  2008/01/16 16:44:47  willuhn
 * @N Verwendung von UUIDs fuer die Vergabe der Dateinamen
 * @N Doppel-Funktion des Systems als Archiv und Queue
 *
 **********************************************************************/
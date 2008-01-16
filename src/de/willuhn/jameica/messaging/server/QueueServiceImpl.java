/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/Attic/QueueServiceImpl.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/01/16 16:44:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;

import de.willuhn.jameica.messaging.rmi.QueueService;
import de.willuhn.logging.Logger;

/**
 * Implementierung einer Queue.
 */
public class QueueServiceImpl extends AbstractMessageServiceImpl implements
    QueueService
{
  /**
   * ct
   * @throws RemoteException
   */
  public QueueServiceImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.messaging.server.AbstractMessageServiceImpl#get(java.lang.String)
   */
  public byte[] get(String channel) throws RemoteException
  {
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      this.get(channel,bos);
      return bos.toByteArray();
    }
    catch (IOException e)
    {
      throw new RemoteException("unable to next message",e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.server.AbstractMessageServiceImpl#get(java.lang.String, java.io.OutputStream)
   */
  public void get(String channel, OutputStream os) throws RemoteException
  {
    String uuid = null;
    try
    {
      uuid = this.storage.next(channel);
      if (uuid == null)
        return; // Keine naechste Nachricht da
      
      super.get(uuid,os);
    }
    catch (IOException e)
    {
      throw new RemoteException("unable to queue message",e);
    }
    finally
    {
      if (uuid != null)
      {
        try
        {
          // Datei automatisch wegloeschen
          this.storage.delete(uuid);
        }
        catch (IOException e)
        {
          Logger.error("unable to delete message [UUID: " + uuid + "]",e);
        }
      }
    }
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "queue";
  }
}


/*********************************************************************
 * $Log: QueueServiceImpl.java,v $
 * Revision 1.5  2008/01/16 16:44:47  willuhn
 * @N Verwendung von UUIDs fuer die Vergabe der Dateinamen
 * @N Doppel-Funktion des Systems als Archiv und Queue
 *
 **********************************************************************/
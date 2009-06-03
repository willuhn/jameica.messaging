/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/ConnectorFsServiceImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/03 16:26:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.server;

import java.rmi.RemoteException;

import de.willuhn.io.fs.FileSystemFactory;
import de.willuhn.jameica.messaging.rmi.ConnectorFsService;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Connectors fuer die FS-API.
 */
public class ConnectorFsServiceImpl implements ConnectorFsService
{
  private boolean started = false;
  
  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "connector.fs";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return !this.isStarted();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return this.started;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (this.isStarted())
    {
      Logger.warn("service allready started, skipping request");
      return;
    }
    FileSystemFactory.register(ArchiveFileSystem.PROTOCOL,ArchiveFileSystem.class);
    this.started = true;
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if (!this.isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }
    FileSystemFactory.register(ArchiveFileSystem.PROTOCOL,null);
    this.started = false;
  }

}


/**********************************************************************
 * $Log: ConnectorFsServiceImpl.java,v $
 * Revision 1.1  2009/06/03 16:26:58  willuhn
 * @N Anbindung an FS-API
 *
 **********************************************************************/

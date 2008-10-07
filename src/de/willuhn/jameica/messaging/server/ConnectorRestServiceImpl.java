/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/ConnectorRestServiceImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/10/07 23:45:41 $
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

import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.rest.Commands;
import de.willuhn.jameica.messaging.rmi.ConnectorRestService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Implementierung des Connectors fuer Zugriff via REST-URLs.
 */
public class ConnectorRestServiceImpl implements ConnectorRestService
{
  private boolean started = false;

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "connector.rest";
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

    // TODO: Nicht sichergestellt, dass das passiert, wenn die REST-Registry
    // online ist. Sollte via SystemMessage.SYSTEM_STARTED verzoegert werden
    QueryMessage q = new QueryMessage();
    q.setName(Commands.PATTERN_GET);
    q.setData(Commands.class.getName() + ".get");
    Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.register").sendMessage(q);

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
    QueryMessage q = new QueryMessage();
    q.setName(Commands.PATTERN_GET);
    Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.unregister").sendMessage(q);
    this.started = false;
  }

}


/**********************************************************************
 * $Log: ConnectorRestServiceImpl.java,v $
 * Revision 1.1  2008/10/07 23:45:41  willuhn
 * @N Connector fuer Zugriff via HTTP-REST - noch in Arbeit
 *
 **********************************************************************/

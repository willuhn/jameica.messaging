/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/ConnectorRestServiceImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/10/08 16:01:40 $
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

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
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
  /**
   * Pattern fuer das GET-Kommando.
   */
  private final static String PATTERN_GET = "/messaging/get/(.*)";
  
  /**
   * Pattern fuer das PUT-Kommando.
   */
  private final static String PATTERN_PUT = "/messaging/put/(.*)";


  private RestConsumer consumer = null;

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
    return this.consumer != null;
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

    this.consumer = new RestConsumer();
    Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.ready").registerMessageConsumer(this.consumer);
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

    try
    {
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.unregister").sendMessage(new QueryMessage(PATTERN_GET,null));
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.unregister").sendMessage(new QueryMessage(PATTERN_PUT,null));
    }
    finally
    {
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.ready").unRegisterMessageConsumer(this.consumer);
      this.consumer = null;
    }
  }
  
  /**
   * Hilfsklasse zum Registrieren der REST-Kommandos.
   */
  private class RestConsumer implements MessageConsumer
  {

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{Message.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.register").sendMessage(new QueryMessage(PATTERN_GET,Commands.class.getName() + ".get"));
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.register").sendMessage(new QueryMessage(PATTERN_PUT,Commands.class.getName() + ".put"));
    }
    
  }

}


/**********************************************************************
 * $Log: ConnectorRestServiceImpl.java,v $
 * Revision 1.2  2008/10/08 16:01:40  willuhn
 * @N REST-Services via Injection (mittels Annotation) mit Context-Daten befuellen
 *
 * Revision 1.1  2008/10/07 23:45:41  willuhn
 * @N Connector fuer Zugriff via HTTP-REST - noch in Arbeit
 *
 **********************************************************************/

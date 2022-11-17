/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
  private Commands bean           = null;
  private RestConsumer consumer   = new RestConsumer();

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
    return this.bean != null;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (this.isStarted())
    {
      Logger.warn("service already started, skipping request");
      return;
    }

    // Wir checken, ob das Webadmin-Plugin verfuegbar ist
    if (Application.getPluginLoader().getPlugin("de.willuhn.jameica.webadmin.Plugin") == null)
    {
      Logger.info("plugin jameica.webadmin not installed, skipping REST service");
      return;
    }
    this.bean = new Commands();
    
    // Wir registrieren uns explizit - fuer den Fall, dass der REST-Service schon laeuft
    Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.register").sendMessage(new QueryMessage(bean));
    
    // Wir registrieren und implizit - und lassen uns benachrichtigen, wenn der REST-Service startet.
    Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.start").registerMessageConsumer(this.consumer);
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
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.unregister").sendMessage(new QueryMessage(this.bean));
    }
    finally
    {
      this.bean = null;

      // Wir wollen kuenftig auch nicht mehr benachrichtigt, wenn der REST-Service startet.
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.start").unRegisterMessageConsumer(this.consumer);
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
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.register").sendMessage(new QueryMessage(bean));
    }
    
  }

}

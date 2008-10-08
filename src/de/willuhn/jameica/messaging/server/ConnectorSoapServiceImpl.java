/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/ConnectorSoapServiceImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/10/08 23:18:39 $
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
import java.util.HashMap;

import javax.jws.WebMethod;
import javax.jws.WebService;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.messaging.rmi.ConnectorSoapService;
import de.willuhn.jameica.messaging.rmi.ConnectorXmlRpcService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Implementierung des SOAP-Connectors.
 */
@WebService(endpointInterface="de.willuhn.jameica.messaging.rmi.ConnectorSoapService")
public class ConnectorSoapServiceImpl implements ConnectorSoapService
{
  private boolean started = false;

  /**
   * ct.
   */
  public ConnectorSoapServiceImpl()
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.ConnectorSoapService#delete(java.lang.String)
   */
  public boolean delete(String uuid) throws RemoteException
  {
    try
    {
      ConnectorXmlRpcService service = (ConnectorXmlRpcService) Application.getServiceFactory().lookup(Plugin.class,"connector.xmlrpc");
      return service.delete(uuid);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to delete uuid " + uuid,e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.ConnectorSoapService#get(java.lang.String)
   */
  public byte[] get(String uuid) throws RemoteException
  {
    try
    {
      ConnectorXmlRpcService service = (ConnectorXmlRpcService) Application.getServiceFactory().lookup(Plugin.class,"connector.xmlrpc");
      return service.get(uuid);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to get uuid " + uuid,e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.ConnectorSoapService#getProperties(java.lang.String)
   */
  public HashMap getProperties(String uuid) throws RemoteException
  {
    try
    {
      ConnectorXmlRpcService service = (ConnectorXmlRpcService) Application.getServiceFactory().lookup(Plugin.class,"connector.xmlrpc");
      return service.getProperties(uuid);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to get properties for uuid " + uuid,e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.ConnectorSoapService#next(java.lang.String)
   */
  public String next(String channel) throws RemoteException
  {
    try
    {
      ConnectorXmlRpcService service = (ConnectorXmlRpcService) Application.getServiceFactory().lookup(Plugin.class,"connector.xmlrpc");
      return service.next(channel);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to get next uuid for channel " + channel,e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.ConnectorSoapService#put(java.lang.String, byte[], java.util.HashMap)
   */
  public String put(String channel, byte[] data, HashMap properties)
      throws RemoteException
  {
    try
    {
      ConnectorXmlRpcService service = (ConnectorXmlRpcService) Application.getServiceFactory().lookup(Plugin.class,"connector.xmlrpc");
      return service.put(channel,data,properties);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to put message into channel " + channel,e);
    }
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  @WebMethod(exclude=true)
  public String getName() throws RemoteException
  {
    return "connector.soap";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  @WebMethod(exclude=true)
  public boolean isStartable() throws RemoteException
  {
    return !this.isStarted();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  @WebMethod(exclude=true)
  public boolean isStarted() throws RemoteException
  {
    return this.started;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  @WebMethod(exclude=true)
  public void start() throws RemoteException
  {
    if(this.isStarted())
    {
      Logger.warn("service allready started, skipping request");
      return;
    }
    // wir lassen uns benachrichtigen, wenn wir den SOAP-Service deployen koennen.
    Application.getMessagingFactory().registerMessageConsumer(new SoapConsumer());
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  @WebMethod(exclude=true)
  public void stop(boolean arg0) throws RemoteException
  {
    if(!this.isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }
    this.started = false;
  }

  /**
   * Hilfsklasse zum Registrieren des SOAP-Services.
   */
  private class SoapConsumer implements MessageConsumer
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
      return new Class[]{SystemMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      SystemMessage msg = (SystemMessage) message;
      if (msg.getStatusCode() == SystemMessage.SYSTEM_STARTED)
        Application.getMessagingFactory().getMessagingQueue("jameica.soap.publish").sendMessage(new QueryMessage("/message",ConnectorSoapServiceImpl.this));
    }
    
  }

}


/**********************************************************************
 * $Log: ConnectorSoapServiceImpl.java,v $
 * Revision 1.1  2008/10/08 23:18:39  willuhn
 * @B bugfixing
 * @N SoapTest
 *
 **********************************************************************/

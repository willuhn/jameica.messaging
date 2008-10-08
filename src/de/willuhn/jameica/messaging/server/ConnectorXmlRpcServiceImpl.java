/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/ConnectorXmlRpcServiceImpl.java,v $
 * $Revision: 1.3 $
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.jameica.messaging.MessageData;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.ConnectorXmlRpcService;
import de.willuhn.jameica.messaging.rmi.StorageService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Implementierung des XML-RPC-Connectors.
 */
public class ConnectorXmlRpcServiceImpl implements ConnectorXmlRpcService
{
  private boolean started = false;

  /**
   * ct.
   */
  public ConnectorXmlRpcServiceImpl()
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.ConnectorXmlRpcService#delete(java.lang.String)
   */
  public boolean delete(String uuid) throws RemoteException
  {
    if (!this.isStarted())
      throw new RemoteException("service not started");

    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData message = new MessageData();
      message.setUuid(uuid);
      service.delete(message);
      return true;
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
   * @see de.willuhn.jameica.messaging.rmi.ConnectorXmlRpcService#get(java.lang.String)
   */
  public byte[] get(String uuid) throws RemoteException
  {
    if (!this.isStarted())
      throw new RemoteException("service not started");

    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData message = new MessageData();
      message.setUuid(uuid);

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      message.setOutputStream(bos);
      service.get(message);
      
      return bos.toByteArray();
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to fetch uuid " + uuid,e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.ConnectorXmlRpcService#getProperties(java.lang.String)
   */
  public HashMap getProperties(String uuid) throws RemoteException
  {
    if (!this.isStarted())
      throw new RemoteException("service not started");

    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData message = new MessageData();
      message.setUuid(uuid);
      service.getProperties(message);
      Map map = message.getProperties();
      if (map == null)
        return null;
      
      return new HashMap(map);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to fetch uuid " + uuid,e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.ConnectorXmlRpcService#next(java.lang.String)
   */
  public String next(String channel) throws RemoteException
  {
    if (!this.isStarted())
      throw new RemoteException("service not started");

    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      return service.next(channel);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to fetch next uuid from channel " + channel,e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.ConnectorXmlRpcService#put(java.lang.String, byte[], java.util.HashMap)
   */
  public String put(String channel, byte[] data, HashMap properties)
      throws RemoteException
  {
    if (!this.isStarted())
      throw new RemoteException("service not started");

    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData message = new MessageData();
      message.setProperties(properties);

      ByteArrayInputStream bis = new ByteArrayInputStream(data);
      message.setInputStream(bis);
      service.put(channel,message);
      return message.getUuid();
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to store message in channel " + channel,e);
    }
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "connector.xmlrpc";
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
    if(this.isStarted())
    {
      Logger.warn("service allready started, skipping request");
      return;
    }
    this.started = true;
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if(!this.isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }
    this.started = false;
  }

}


/**********************************************************************
 * $Log: ConnectorXmlRpcServiceImpl.java,v $
 * Revision 1.3  2008/10/08 23:18:39  willuhn
 * @B bugfixing
 * @N SoapTest
 *
 * Revision 1.2  2008/10/08 17:55:11  willuhn
 * @N SOAP-Connector (in progress)
 *
 * Revision 1.1  2008/10/07 23:03:34  willuhn
 * @C "queue" und "archive" entfernt. Zugriff jetzt direkt ueber Connectoren
 *
 **********************************************************************/

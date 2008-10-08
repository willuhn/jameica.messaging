/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/ConnectorXmlRpcServiceImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/10/08 17:55:11 $
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
import java.util.Map;

import javax.jws.WebService;

import de.willuhn.jameica.messaging.LookupService;
import de.willuhn.jameica.messaging.MessageData;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.ConnectorXmlRpcService;
import de.willuhn.jameica.messaging.rmi.StorageService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Implementierung des XML-RPC- und SOAP-Connectors.
 */
@WebService(endpointInterface="de.willuhn.jameica.messaging.rmi.ConnectorXmlRpcService")
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
  public Map getProperties(String uuid) throws RemoteException
  {
    if (!this.isStarted())
      throw new RemoteException("service not started");

    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData message = new MessageData();
      message.setUuid(uuid);
      service.getProperties(message);
      return message.getProperties();
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
   * @see de.willuhn.jameica.messaging.rmi.ConnectorXmlRpcService#put(java.lang.String, byte[], java.util.Map)
   */
  public String put(String channel, byte[] data, Map properties)
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
    try
    {
      LookupService.unRegister("tcp:" + Plugin.class.getName() + "." + getName());
    }
    catch (Exception e)
    {
      Logger.error("unable to unregister multicast lookup",e);
    }
    finally
    {
      this.started = false;
    }
  }

}


/**********************************************************************
 * $Log: ConnectorXmlRpcServiceImpl.java,v $
 * Revision 1.2  2008/10/08 17:55:11  willuhn
 * @N SOAP-Connector (in progress)
 *
 * Revision 1.1  2008/10/07 23:03:34  willuhn
 * @C "queue" und "archive" entfernt. Zugriff jetzt direkt ueber Connectoren
 *
 **********************************************************************/

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;

import de.willuhn.datasource.Service;
import de.willuhn.jameica.messaging.MessageData;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.StorageService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Abstrakte Basis-Implementierung fuer Web-basierte Connectors.
 */
public abstract class AbstractConnectorWebServiceImpl implements Service
{
  private boolean started = false;

  /**
   * Loescht die Nachricht mit der angegebenen UUID.
   * @param uuid UUID.
   * @return true, wenn die Nachricht geloescht wurde.
   * @throws RemoteException
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
   * Liefert die angegebene Nachricht.
   * @param uuid UUID der Nachricht.
   * @return die Nachricht.
   * @throws RemoteException
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
   * Liefert die Properties der Nachricht.
   * @param uuid UUID der Nachricht.
   * @return die Properties.
   * @throws RemoteException
   */
  public HashMap<String,String> getProperties(String uuid) throws RemoteException
  {
    if (!this.isStarted())
      throw new RemoteException("service not started");

    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData message = new MessageData();
      message.setUuid(uuid);
      service.getProperties(message);
      Map m = message.getProperties();
      return m == null ? null : new HashMap<String,String>(m);
      
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
   * Liefert die UUID der naechsten Nachricht in diesem Channel.
   * @param channel Name des Channels.
   * @return UUID der naechsten Nachricht.
   * @throws RemoteException
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
   * Liefert die Liste der UUIDs in diesem Channel.
   * @param channel Name des Channels.
   * @return Liste der UUIDs.
   * @throws RemoteException
   */
  public String[] list(String channel) throws RemoteException
  {
    if (!this.isStarted())
      throw new RemoteException("service not started");

    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      return service.list(channel);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to fetch uuid list from channel " + channel,e);
    }
  }

  /**
   * Speichert eine Nachricht.
   * @param channel Channel.
   * @param data Nutzdaten.
   * @param properties Meta-Daten.
   * @return Erzeugte UUID.
   * @throws RemoteException
   */
  public String put(String channel, byte[] data, HashMap<String,String> properties)
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
      Logger.warn("service already started, skipping request");
      return;
    }
    this.started = true;
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

}

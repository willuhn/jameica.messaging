/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/Attic/QueueServiceImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/12/14 11:28:08 $
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
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.QueueService;
import de.willuhn.jameica.messaging.rmi.StorageEngine;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;

/**
 * Implementierung des Queue-Services.
 */
public class QueueServiceImpl extends UnicastRemoteObject implements
    QueueService
{

  private StorageEngine storage = null;

  /**
   * ct
   * @throws RemoteException
   */
  public QueueServiceImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "Queue-Service";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return !isStarted();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return this.storage != null;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (isStarted())
    {
      Logger.warn("service allready started, skipping request");
      return;
    }
    PluginResources res    = Application.getPluginLoader().getPlugin(Plugin.class).getResources();
    Settings settings      = res.getSettings();
    MultipleClassLoader cl = res.getClassLoader();
    String engine = settings.getString("queue.storage.engine",StorageEngineFileImpl.class.getName());
    Logger.info("using storage engine: " + engine);
    try
    {
      this.storage = (StorageEngine) cl.load(engine).newInstance(); 
    }
    catch (Throwable t)
    {
      Logger.error("unable to load storage engine",t);
    }
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean restartable) throws RemoteException
  {
    if (!isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }
    this.storage = null;
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.QueueService#get(java.lang.String)
   */
  public synchronized byte[] get(String channel) throws RemoteException
  {
    String logPrefix = "[channel: " + channel + "] ";

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try
    {
      this.storage.get(channel,bos);
      byte[] data = bos.toByteArray();
      Logger.info(logPrefix + " message sent (" + data.length + " bytes)");
      return data;
    }
    catch (IOException e)
    {
      throw new RemoteException("unable to fetch message",e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.QueueService#put(java.lang.String, byte[])
   */
  public synchronized boolean put(String channel, byte[] data) throws RemoteException
  {
    String logPrefix = "[channel: " + channel + "] ";
    if (data == null || data.length == 0)
    {
      Logger.info(logPrefix + " got empty message, ignoring");
      return false;
    }
    
    try
    {
      this.storage.put(channel,new ByteArrayInputStream(data));
      Logger.info(logPrefix + " message received (" + data.length + " bytes)");
      return true;
    }
    catch (IOException e)
    {
      throw new RemoteException("unable to queue message",e);
    }
  }
}


/*********************************************************************
 * $Log: QueueServiceImpl.java,v $
 * Revision 1.3  2007/12/14 11:28:08  willuhn
 * @N Storage-Engine
 *
 * Revision 1.2  2007/12/14 09:56:59  willuhn
 * @N Channel-Angabe mit Punkt-Notation
 *
 * Revision 1.1  2007/12/13 23:31:38  willuhn
 * @N initial import
 *
 * Revision 1.1  2007/12/13 16:11:51  willuhn
 * @N Generischer Message-Queue-Service
 *
 **********************************************************************/
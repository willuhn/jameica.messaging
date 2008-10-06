/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/Attic/AbstractMessageServiceImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/10/06 23:30:45 $
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
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.MessageService;
import de.willuhn.jameica.messaging.rmi.StorageEngine;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;

/**
 * Abstrakte Basis-Implementierung eines Message-Services.
 */
public abstract class AbstractMessageServiceImpl extends UnicastRemoteObject implements MessageService
{

  protected StorageEngine storage = null;

  /**
   * ct
   * @throws RemoteException
   */
  public AbstractMessageServiceImpl() throws RemoteException
  {
    super();
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
    String engine = settings.getString("storage.engine." + getName(),StorageEngineFileImpl.class.getName());
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
   * @see de.willuhn.jameica.messaging.rmi.MessageService#get(java.lang.String)
   */
  public synchronized byte[] get(String uuid) throws RemoteException
  {
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      get(uuid,bos);
      return bos.toByteArray();
    }
    catch (IOException e)
    {
      throw new RemoteException("unable to fetch message",e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.MessageService#put(java.lang.String, byte[])
   */
  public synchronized String put(String channel, byte[] data) throws RemoteException
  {
    try
    {
      return put(channel,new ByteArrayInputStream(data));
    }
    catch (IOException e)
    {
      throw new RemoteException("unable to queue message",e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.MessageService#get(java.lang.String, java.io.OutputStream)
   */
  public void get(String uuid, OutputStream os) throws RemoteException
  {
    try
    {
      Message msg = new Message();
      msg.setUuid(uuid);
      msg.setOutputStream(os);
      this.storage.get(msg);
    }
    catch (IOException e)
    {
      throw new RemoteException("unable to queue message",e);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.MessageService#put(java.lang.String, java.io.InputStream)
   */
  public String put(String channel, InputStream is) throws RemoteException
  {
    try
    {
      Message msg = new Message();
      msg.setInputStream(is);
      this.storage.put(channel,msg);
      return msg.getUuid();
    }
    catch (IOException e)
    {
      throw new RemoteException("unable to queue message",e);
    }
  }
}


/*********************************************************************
 * $Log: AbstractMessageServiceImpl.java,v $
 * Revision 1.2  2008/10/06 23:30:45  willuhn
 * @N Support fuer Properties in Messages
 *
 * Revision 1.1  2008/01/16 16:44:47  willuhn
 * @N Verwendung von UUIDs fuer die Vergabe der Dateinamen
 * @N Doppel-Funktion des Systems als Archiv und Queue
 *
 **********************************************************************/
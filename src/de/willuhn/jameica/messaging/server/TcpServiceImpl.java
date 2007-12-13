/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/Attic/TcpServiceImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/12/13 23:31:38 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.TcpService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;


/**
 * Implementierung des TCP-Services.
 */
public class TcpServiceImpl extends UnicastRemoteObject implements TcpService
{
  private Worker worker = null;

  /**
   * @throws RemoteException
   */
  public TcpServiceImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "TCP-Service";
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
    return this.worker != null && this.worker.running;
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
    try
    {
      this.worker = new Worker();
      this.worker.start();
    }
    catch (Exception e)
    {
      Logger.error("unable to start tcp listener",e);
      this.worker = null;
    }
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
      this.worker.shutdown();
    }
    catch (Exception e)
    {
      Logger.error("error while closing server socket",e);
    }
    finally
    {
      this.worker = null;
      Logger.info("tcp listener stopped");
    }
  }

  
  /**
   * 
   */
  private class Worker extends Thread
  {
    private ServerSocket server = null;
    private boolean running     = false;
    
    /**
     * ct
     * @throws Exception
     */
    private Worker() throws Exception
    {
      Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();

      InetAddress address = null;
      String host = settings.getString("listener.tcp.address",null);
      if (host != null)
        address = InetAddress.getByName(host);
      
      int port = settings.getInt("listener.tcp.port",9000);
      Logger.info("binding to " + (address != null ? address.getHostAddress() : "*") + ":" + port);
      this.server = new ServerSocket(port,-1,address);
      Logger.info("tcp listener started");
      this.running = true;
    }
    
    /**
     * Beendet wen Worker.
     */
    private void shutdown()
    {
      this.running = false;
    }
    
    /**
     * @see java.lang.Thread#run()
     */
    public void run()
    {
      try
      {
        while (this.running)
        {
          try
          {
            // TODO Koennte man mal noch Multithreaded machen
            handleRequest(this.server.accept());
          }
          catch (IOException e)
          {
            // Kann z.Bsp. passieren, wenn der Client den Socket unerwartet schliesst
            Logger.write(Level.DEBUG,"error while processing request",e);
          }
        }
      }
      catch (Exception e)
      {
        if (!(e instanceof InterruptedException))
        {
          // Huh? Das kommt unerwartet
          Logger.error("tcp listener stopped abnormally",e);
        }
        Logger.info("stopping tcp listener");
        try {
          this.server.close();
        } catch (Exception e2) {} // ignore
        finally {
          this.server = null;
        }
      }
    }

    /**
     * Bearbeitet den Request.
     * @param socket
     * @throws IOException
     */
    private void handleRequest(Socket socket) throws IOException
    {
      socket.setSoTimeout(30 * 1000); // Client blockt maximal 30 Sekunden
      InputStream is = null;
      try
      {
        is = socket.getInputStream();
      }
      finally
      {
        if (is != null)
        {
          try {
            is.close();
          } catch (Exception e) {} // ignore
        }
      }
    }
  }
}


/**********************************************************************
 * $Log: TcpServiceImpl.java,v $
 * Revision 1.1  2007/12/13 23:31:38  willuhn
 * @N initial import
 *
 **********************************************************************/

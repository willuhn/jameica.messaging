/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/Attic/TcpServiceImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/12/14 00:13:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.QueueService;
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
    private ServerSocket server  = null;
    private QueueService service = null;
    private boolean running      = false;
    
    /**
     * ct
     * @throws Exception
     */
    private Worker() throws Exception
    {
      Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();

      this.service = (QueueService) Application.getServiceFactory().lookup(Plugin.class,"queue");
      InetAddress address = null;
      String host = settings.getString("listener.tcp.address",null);
      if (host != null)
        address = InetAddress.getByName(host);
      
      int port = settings.getInt("listener.tcp.port",9000);
      Logger.info("binding to " + (address != null ? address.getHostAddress() : "*") + ":" + port);
      this.server = new ServerSocket(port,-1,address);
      Logger.info("tcp connector to queue service started");
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
          Socket socket = this.server.accept();
          try
          {
            // TODO Koennte man mal noch Multithreaded machen
            handleRequest(socket);
          }
          catch (Exception e)
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
      OutputStream os = null;
      try
      {
        InputStream is = new BufferedInputStream(socket.getInputStream());
        os = new BufferedOutputStream(socket.getOutputStream());
        
        ////////////////////////////////////////////////////////////////////////
        // Kommando auswerten. Erste Zeile - maximal 255 Zeichen
        int count = 0;
        int max = 255;
        byte[] buf = new byte[max];
        int read = -1;
        do
        {
          read = is.read();
          if (read == -1 || read == '\n')
            break; // Ende
          buf[count++] = (byte) read;
        }
        while (count<max);
        ////////////////////////////////////////////////////////////////////////
        String command = new String(buf).trim();
        if (command.length() == 0 || (!command.startsWith("get ") && !command.startsWith("put ")))
        {
          Logger.warn("invalid command given: " + command);
          return;
        }

        boolean get = command.startsWith("get ");
        command = command.substring(4); // get/put abschneiden
        int sep = command.indexOf(":");
        if (sep == -1)
        {
          Logger.warn("invalid command given: " + command);
          return;
        }
        
        String channel   = command.substring(0,sep);
        String recipient = command.substring(sep+1);

        if (get)
        {
          os.write(service.get(channel,recipient));
        }
        else
        {
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          buf = new byte[4096];
          read = -1;
          do
          {
            read = is.read(buf);
            if (read != -1)
              bos.write(buf,0,read);
          }
          while (read != -1);
          service.put(channel,recipient,bos.toByteArray());
        }
        
        os.flush();
      }
      finally
      {
        if (os != null)
        {
          try {
            os.close();
          } catch (Exception e) {} // ignore
        }
      }
    }
  }
}


/**********************************************************************
 * $Log: TcpServiceImpl.java,v $
 * Revision 1.3  2007/12/14 00:13:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2007/12/14 00:02:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2007/12/13 23:31:38  willuhn
 * @N initial import
 *
 **********************************************************************/

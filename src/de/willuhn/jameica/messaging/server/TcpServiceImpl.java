/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/Attic/TcpServiceImpl.java,v $
 * $Revision: 1.8 $
 * $Date: 2008/01/16 23:31:43 $
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

import de.willuhn.jameica.messaging.LookupService;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.ArchiveService;
import de.willuhn.jameica.messaging.rmi.MessageService;
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
  private Hashtable commands = new Hashtable();
  

  /**
   * @throws RemoteException
   */
  public TcpServiceImpl() throws RemoteException
  {
    super();
    this.commands.put("get", new get());
    this.commands.put("put",new put());
    this.commands.put("delete",new delete());
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "connector.tcp";
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
    private boolean running      = false;
    
    /**
     * ct
     * @throws Exception
     */
    private Worker() throws Exception
    {
      super("tcp.connector for jameica.messaging");
      Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();

      InetAddress address = null;

      // Host
      String host = settings.getString("listener.tcp.address",null);
      if (host != null) address = InetAddress.getByName(host);

      // Port
      int port = settings.getInt("listener.tcp.port",9000);

      Logger.info("binding to " + (address != null ? address.getHostAddress() : "*") + ":" + port);
      this.server = new ServerSocket(port,-1,address);
      Logger.info("raw tcp connector started");
      this.running = true;
      
      String url  = (address != null ? address.getHostName() : Application.getCallback().getHostname()) + ":" + port;
      LookupService.register("tcp:" + Plugin.class.getName() + "." + TcpServiceImpl.this.getName(),url);
    }
    
    /**
     * Beendet wen Worker.
     */
    private void shutdown()
    {
      this.running = false;
      try
      {
        LookupService.unRegister("tcp:" + Plugin.class.getName() + "." + TcpServiceImpl.this.getName());
      }
      catch (Exception e)
      {
        Logger.error("unable to unregister multicast lookup",e);
      }
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
        if (command.length() == 0 || !command.matches("[a-zA-Z]{1,30}:[a-zA-Z]{3,10} .*"))
        {
          Logger.warn("invalid command: " + command);
          return;
        }
        
        String service = command.substring(0,command.indexOf(':'));
        String cmd     = command.substring(command.indexOf(':')+1,command.indexOf(' '));
        String name    = command.substring(command.indexOf(' ')+1);

        Command c = (Command) commands.get(cmd);
        if (c == null)
        {
          Logger.warn("invalid command: " + command);
          return;
        }

        MessageService ms = (MessageService) Application.getServiceFactory().lookup(Plugin.class,service);
        c.exec(ms,name,is,os);
        os.flush();
      }
      catch (IOException e)
      {
        throw e;
      }
      catch (Exception e2)
      {
        Logger.error("error while processing request",e2);
        throw new IOException("service not found");
      }
      finally
      {
        if (os != null)
        {
          try {
            os.close();
          } 
          catch (Exception e) {
            Logger.error("error while closing outputstream",e);
            
          } // ignore
        }
      }
    }
  }
  
  /**
   * Interface fuer die einzelnen Kommandos.
   */
  public static interface Command
  {
    /**
     * @param service
     * @param name
     * @param is
     * @param os
     * @throws IOException
     */
    public void exec(MessageService service, String name, InputStream is, OutputStream os) throws IOException;
  }
  
  private class get implements Command
  {
    /**
     * @see de.willuhn.jameica.messaging.server.TcpServiceImpl.Command#exec(de.willuhn.jameica.messaging.rmi.MessageService, java.lang.String, java.io.InputStream, java.io.OutputStream)
     */
    public void exec(MessageService service, String name, InputStream is, OutputStream os) throws IOException
    {
      service.get(name,os);
    }
  }

  private class put implements Command
  {
    /**
     * @see de.willuhn.jameica.messaging.server.TcpServiceImpl.Command#exec(de.willuhn.jameica.messaging.rmi.MessageService, java.lang.String, java.io.InputStream, java.io.OutputStream)
     */
    public void exec(MessageService service, String name, InputStream is, OutputStream os) throws IOException
    {
      String uuid = service.put(name,is);
      os.write(uuid.getBytes());
      os.write("\r\n".getBytes());
    }
  }

  private class delete implements Command
  {
    /**
     * @see de.willuhn.jameica.messaging.server.TcpServiceImpl.Command#exec(de.willuhn.jameica.messaging.rmi.MessageService, java.lang.String, java.io.InputStream, java.io.OutputStream)
     */
    public void exec(MessageService service, String name, InputStream is, OutputStream os) throws IOException
    {
      try
      {
        ((ArchiveService)service).delete(name);
      }
      catch (ClassCastException e)
      {
        Logger.error("command \"delete\" is only allowed for archive services");
      }
    }
  }

}


/**********************************************************************
 * $Log: TcpServiceImpl.java,v $
 * Revision 1.8  2008/01/16 23:31:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2008/01/16 17:36:30  willuhn
 * @N Multicast-Lookup
 *
 * Revision 1.6  2008/01/16 16:44:47  willuhn
 * @N Verwendung von UUIDs fuer die Vergabe der Dateinamen
 * @N Doppel-Funktion des Systems als Archiv und Queue
 *
 * Revision 1.5  2007/12/14 12:04:08  willuhn
 * @C TCP-Listener verwendet jetzt Stream-API
 *
 * Revision 1.4  2007/12/14 09:56:59  willuhn
 * @N Channel-Angabe mit Punkt-Notation
 *
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

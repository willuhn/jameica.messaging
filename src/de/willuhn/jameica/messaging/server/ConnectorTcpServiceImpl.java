/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/ConnectorTcpServiceImpl.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/06/18 09:50:53 $
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
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import de.willuhn.jameica.messaging.LookupService;
import de.willuhn.jameica.messaging.MessageData;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.ConnectorTcpService;
import de.willuhn.jameica.messaging.rmi.StorageService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;


/**
 * Implementierung des TCP-Connectors.
 */
public class ConnectorTcpServiceImpl implements ConnectorTcpService
{
  private Worker worker = null;
  private Hashtable commands = new Hashtable();
  

  /**
   * ct
   */
  public ConnectorTcpServiceImpl()
  {
    this.commands.put("get",     new Get());
    this.commands.put("put",     new Put());
    this.commands.put("next",    new Next());
    this.commands.put("delete",  new Delete());
    this.commands.put("putmeta", new PutMeta());
    this.commands.put("getmeta", new GetMeta());
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
      LookupService.register("tcp:" + Plugin.class.getName() + "." + ConnectorTcpServiceImpl.this.getName(),url);
    }
    
    /**
     * Beendet wen Worker.
     */
    private void shutdown()
    {
      this.running = false;
      try
      {
        LookupService.unRegister("tcp:" + Plugin.class.getName() + "." + ConnectorTcpServiceImpl.this.getName());
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
        if (command.length() == 0 || !command.matches("[a-zA-Z]{1,30} .*"))
        {
          Logger.warn("invalid command: " + command);
          return;
        }
        
        String cmd  = command.substring(0,command.indexOf(' ')).toLowerCase();
        String data = command.substring(command.indexOf(' ')+1);

        Command c = (Command) commands.get(cmd);
        if (c == null)
        {
          Logger.warn("unknown command: " + cmd);
          return;
        }

        c.exec(data,is,os);
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
     * @param data
     * @param is
     * @param os
     * @throws Exception
     */
    public void exec(String data, InputStream is, OutputStream os) throws Exception;
  }
  
  private class Get implements Command
  {
    /**
     * @see de.willuhn.jameica.messaging.server.ConnectorTcpServiceImpl.Command#exec(java.lang.String, java.io.InputStream, java.io.OutputStream)
     */
    public void exec(String data, InputStream is, OutputStream os) throws Exception
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData message = new MessageData();
      message.setUuid(data);
      message.setOutputStream(os);
      service.get(message);
    }
  }

  private class Put implements Command
  {
    /**
     * @see de.willuhn.jameica.messaging.server.ConnectorTcpServiceImpl.Command#exec(java.lang.String, java.io.InputStream, java.io.OutputStream)
     */
    public void exec(String data, InputStream is, OutputStream os) throws Exception
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData message = new MessageData();
      message.setInputStream(is);
      service.put(data,message);

      // Erzeugte UUID zurueckliefern
      os.write(message.getUuid().getBytes());
      os.write("\r\n".getBytes());
    }
  }

  private class PutMeta implements Command
  {
    /**
     * @see de.willuhn.jameica.messaging.server.ConnectorTcpServiceImpl.Command#exec(java.lang.String, java.io.InputStream, java.io.OutputStream)
     */
    public void exec(String data, InputStream is, OutputStream os) throws Exception
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");

      MessageData message = new MessageData();
      message.setUuid(data);

      // Meta-Daten
      Properties props = new Properties();
      props.load(is);
      Map<String,String> m = new HashMap<String,String>();
      Enumeration e = props.keys();
      while (e.hasMoreElements())
      {
        String key = (String) e.nextElement();
        m.put(key,props.getProperty(key));
      }
      
      message.setProperties(m);
      service.setProperties(message);
    }
  }

  private class GetMeta implements Command
  {
    /**
     * @see de.willuhn.jameica.messaging.server.ConnectorTcpServiceImpl.Command#exec(java.lang.String, java.io.InputStream, java.io.OutputStream)
     */
    public void exec(String data, InputStream is, OutputStream os) throws Exception
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");

      MessageData message = new MessageData();
      message.setUuid(data);
      service.getProperties(message);
      
      Properties props = new Properties();
      props.putAll(message.getProperties());
      props.list(new PrintStream(os));
      props.store(os,null);
    }
  }

  private class Delete implements Command
  {
    /**
     * @see de.willuhn.jameica.messaging.server.ConnectorTcpServiceImpl.Command#exec(java.lang.String, java.io.InputStream, java.io.OutputStream)
     */
    public void exec(String data, InputStream is, OutputStream os) throws Exception
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData message = new MessageData();
      message.setUuid(data);
      service.delete(message);
    }
  }

  private class Next implements Command
  {
    /**
     * @see de.willuhn.jameica.messaging.server.ConnectorTcpServiceImpl.Command#exec(java.lang.String, java.io.InputStream, java.io.OutputStream)
     */
    public void exec(String data, InputStream is, OutputStream os) throws Exception
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      
      // 1. UUID ermitteln 
      String uuid = service.next(data);
      if (uuid != null)
      {
        // 2. Datei abrufen
        Get get = (Get) commands.get("get");
        get.exec(uuid,is,os);
        
        // 3. Datei loeschen
        Delete delete = (Delete) commands.get("delete");
        delete.exec(uuid,is,os);
      }
    }
  }

}


/**********************************************************************
 * $Log: ConnectorTcpServiceImpl.java,v $
 * Revision 1.5  2009/06/18 09:50:53  willuhn
 * @N zwei neue Kommandos (getmeta und putmeta) zum Lesen und Schreiben der Properties
 *
 * Revision 1.4  2008/12/09 17:10:55  willuhn
 * @B falscher lookup name fuer tcp connector
 *
 * Revision 1.3  2008/10/08 17:55:11  willuhn
 * @N SOAP-Connector (in progress)
 *
 * Revision 1.2  2008/10/07 23:45:41  willuhn
 * @N Connector fuer Zugriff via HTTP-REST - noch in Arbeit
 *
 * Revision 1.1  2008/10/07 23:03:34  willuhn
 * @C "queue" und "archive" entfernt. Zugriff jetzt direkt ueber Connectoren
 *
 * Revision 1.10  2008/10/07 00:11:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2008/10/06 23:41:55  willuhn
 * @N Support fuer Properties in Messages
 *
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

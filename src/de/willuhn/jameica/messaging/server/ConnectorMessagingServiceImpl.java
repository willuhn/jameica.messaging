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
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Map;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.MessageData;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.rmi.ConnectorMessagingService;
import de.willuhn.jameica.messaging.rmi.StorageService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Connectors via Jameica-Messaging.
 */
public class ConnectorMessagingServiceImpl implements ConnectorMessagingService
{
  private boolean started = false;
  
  private Put put   = null;
  private Get get   = null;
  private Del del   = null;
  private List list = null;
  private GetMeta getmeta = null;
  private PutMeta putmeta = null;

  private Next next = null;

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "connector.msg";
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
    if (this.isStarted())
    {
      Logger.warn("service already started, skipping request");
      return;
    }

    this.put = new Put();
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.put").registerMessageConsumer(this.put);

    this.get = new Get();
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.get").registerMessageConsumer(this.get);

    this.del = new Del();
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.del").registerMessageConsumer(this.del);
    
    this.list = new List(); 
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.list").registerMessageConsumer(this.list);

    this.putmeta = new PutMeta();
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.putmeta").registerMessageConsumer(this.putmeta);

    this.getmeta = new GetMeta();
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.getmeta").registerMessageConsumer(this.getmeta);

    this.next = new Next();
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.next").registerMessageConsumer(this.next);

    this.started = true;
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

    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.get").unRegisterMessageConsumer(this.get);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.put").unRegisterMessageConsumer(this.put);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.del").unRegisterMessageConsumer(this.del);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.list").unRegisterMessageConsumer(this.list);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.putmeta").unRegisterMessageConsumer(this.putmeta);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.getmeta").unRegisterMessageConsumer(this.getmeta);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.next").unRegisterMessageConsumer(this.next);
    this.started = false;
  }
  
  /**
   * Implementierung des Consumers fuer PUT.
   */
  private class Put implements MessageConsumer
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
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      QueryMessage qm = (QueryMessage) message;
      Object data = qm.getData();
      if (data == null)
      {
        Logger.error("message " + qm.getName() + " contains no data");
        return;
      }
      
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");

      MessageData msg = new MessageData();
      
      if (data instanceof byte[])
      {
        msg.setInputStream(new ByteArrayInputStream((byte[]) data));
      }
      else if (data instanceof InputStream)
      {
        msg.setInputStream((InputStream) data);
      }
      else
      {
        msg.setInputStream(new ByteArrayInputStream(data.toString().getBytes()));
      }
      service.put(qm.getName(),msg);
      
      // Wir ersetzen die Nutzdaten gegen die erzeugte UUID.
      qm.setData(msg.getUuid());
    }
    
  }


  /**
   * Implementierung des Consumers fuer GET.
   */
  private class Get implements MessageConsumer
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
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      QueryMessage qm = (QueryMessage) message;
      String uuid = qm.getName();

      if (uuid == null || uuid.length() == 0)
      {
        Logger.error("message contains no uuid");
        return;
      }

      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");

      MessageData msg = new MessageData();
      msg.setUuid(uuid);
      
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      msg.setOutputStream(bos);
      service.get(msg);
      
      // Wir schreiben die abgerufenen Daten in die Message
      qm.setData(bos.toByteArray());
    }
    
  }

  /**
   * Implementierung des Consumers fuer DEL.
   */
  private class Del implements MessageConsumer
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
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      QueryMessage qm = (QueryMessage) message;
      String uuid = qm.getName();

      if (uuid == null || uuid.length() == 0)
      {
        Logger.error("message contains no uuid");
        return;
      }

      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");

      MessageData msg = new MessageData();
      msg.setUuid(uuid);
      
      service.delete(msg);
    }
    
  }
  
  private class List implements MessageConsumer
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
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      QueryMessage qm = (QueryMessage) message;
      String channel = qm.getName();

      if (channel == null || channel.length() == 0)
      {
        Logger.error("message contains no channel");
        return;
      }

      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      qm.setData(Arrays.asList(service.list(channel)));
    }
  }


  /**
   * Implementierung des Consumers fuer PUT META.
   */
  private class PutMeta implements MessageConsumer
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
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      QueryMessage qm = (QueryMessage) message;
      String uuid = qm.getName();
      Object data = qm.getData();

      if (uuid == null || uuid.length() == 0)
      {
        Logger.error("message contains no uuid");
        return;
      }

      if (data == null || !(data instanceof Map))
      {
        Logger.error("message contains no valid meta data");
        return;
      }

      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");

      MessageData msg = new MessageData();
      msg.setUuid(uuid);
      msg.setProperties((Map)data);
      service.setProperties(msg);
    }
    
  }


  /**
   * Implementierung des Consumers fuer GET META.
   */
  private class GetMeta implements MessageConsumer
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
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      QueryMessage qm = (QueryMessage) message;
      String uuid = qm.getName();

      if (uuid == null || uuid.length() == 0)
      {
        Logger.error("message contains no uuid");
        return;
      }

      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");

      MessageData msg = new MessageData();
      msg.setUuid(uuid);
      service.getProperties(msg);
      
      // Wir speichern die Meta-Daten in der Message
      qm.setData(msg.getProperties());
    }
    
  }

  
  /**
   * Implementierung des Consumers fuer NEXT.
   */
  private class Next implements MessageConsumer
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
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      QueryMessage qm = (QueryMessage) message;
      String channel = qm.getName();

      if (channel == null || channel.length() == 0)
      {
        Logger.error("message contains no channel");
        return;
      }

      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      qm.setData(service.next(channel));
    }
    
  }

}

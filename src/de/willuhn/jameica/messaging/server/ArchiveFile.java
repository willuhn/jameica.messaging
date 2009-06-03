/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/ArchiveFile.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/03 16:26:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.io.fs.FSException;
import de.willuhn.io.fs.File;
import de.willuhn.jameica.messaging.MessageData;

/**
 * Implementierung eines File-Objektes via FS-API.
 */
public class ArchiveFile implements File
{
  private ArchiveFileSystem fs = null;
  private String channel       = null;
  private String name          = null;
  private MessageData msg      = null;
  
  /**
   * ct.
   * @param fs das Filesystem.
   * @param channel der Channel.
   * @param name der Dateiname.
   */
  ArchiveFile(ArchiveFileSystem fs,String channel, String name)
  {
    this.fs      = fs;
    this.channel = channel;
    this.name    = name;
  }

  /**
   * @see de.willuhn.io.fs.File#delete()
   */
  public void delete() throws FSException
  {
    MessageData data = getMessage();
    if (data.getUuid() != null)
    {
      try
      {
        this.fs.getService().delete(data);
      }
      catch (FSException e)
      {
        throw e;
      }
      catch (Exception e2)
      {
        throw new FSException(e2);
      }
    }
  }

  /**
   * @see de.willuhn.io.fs.File#exists()
   */
  public boolean exists() throws FSException
  {
    return getMessage().getUuid() != null;
  }

  /**
   * @see de.willuhn.io.fs.File#getInputStream()
   */
  public InputStream getInputStream() throws FSException
  {
    try
    {
      PipedInputStream pis = new PipedInputStream();
      PipedOutputStream pos = new PipedOutputStream(pis);

      final MessageData data = getMessage();
      data.setOutputStream(pos);
      
      // Muss in einem extra Thread erfolgen, weil sonst die Pipe haengt.
      new Thread()
      {
        public void run()
        {
          try
          {
            fs.getService().get(data);
          }
          catch (RuntimeException re)
          {
            throw re;
          }
          catch (Exception e)
          {
            throw new RuntimeException(e);
          }
        }
      }.start();
      
      return pis;
    }
    catch (FSException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      throw new FSException(e2);
    }
  }

  /**
   * @see de.willuhn.io.fs.File#getOutputStream()
   */
  public OutputStream getOutputStream() throws FSException
  {
    try
    {
      PipedOutputStream pos = new PipedOutputStream();
      PipedInputStream pis = new PipedInputStream(pos);

      final MessageData data = getMessage();
      data.setInputStream(pis);

      // Dateiname speichern
      Map<String,String> props = data.getProperties();
      if (props == null)
      {
        props = new HashMap<String,String>();
        data.setProperties(props);
      }
      props.put(MessageData.PROPERTY.filename.toString(),this.name);

      // Muss in einem extra Thread erfolgen, weil sonst die Pipe haengt.
      new Thread()
      {
        public void run()
        {
          try
          {
            fs.getService().put(channel,data);
          }
          catch (RuntimeException re)
          {
            throw re;
          }
          catch (Exception e)
          {
            throw new RuntimeException(e);
          }
        }
      }.start();
      return pos;
    }
    catch (FSException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      throw new FSException(e2);
    }
  }

  /**
   * @see de.willuhn.io.fs.File#lastModified()
   */
  public long lastModified() throws FSException
  {
    Map<String,String> props = getMessage().getProperties();
    if (props == null)
      return 0;
    String modified = props.get(MessageData.PROPERTY.modified.toString());
    if (modified == null || modified.length() == 0)
      return 0;
    return Long.parseLong(modified);
  }

  /**
   * @see de.willuhn.io.fs.File#length()
   */
  public long length() throws FSException
  {
    Map<String,String> props = getMessage().getProperties();
    if (props == null)
      return 0;
    String size = props.get(MessageData.PROPERTY.filesize.toString());
    if (size == null || size.length() == 0)
      return 0;
    return Long.parseLong(size);
  }
  
  /**
   * Laedt die Nachricht.
   * @return
   * @throws FSException
   */
  private synchronized MessageData getMessage() throws FSException
  {
    if (this.msg == null)
    {
      this.msg = new MessageData();

      // Message suchen
      try
      {
        String[] uuids = this.fs.getService().list(this.channel);
        if (uuids == null || uuids.length == 0)
          return this.msg; // Leere neue Datei
        
        for (int i=0;i<uuids.length;++i)
        {
          this.msg.setUuid(uuids[i]);

          // Meta-Daten laden
          this.fs.getService().get(this.msg);

          // Checken, ob UUID = Dateiname
          if (uuids[i].equals(this.name))
            return this.msg;
          
          // Meta-Daten nach Dateiname checken
          Map<String,String> props = this.msg.getProperties();
          if (props != null)
          {
            String filename = props.get(MessageData.PROPERTY.filename.toString());
            if (filename != null && filename.equals(this.name))
              return this.msg;
          }
        }
        
        // Wenn wir hier angekommen sind, wurde die Datei nicht gefunden.
        // Also wird es eine neue.
      }
      catch (FSException e)
      {
        throw e;
      }
      catch (Exception e2)
      {
        throw new FSException(e2);
      }
    }
    return this.msg;
  }

}


/**********************************************************************
 * $Log: ArchiveFile.java,v $
 * Revision 1.1  2009/06/03 16:26:58  willuhn
 * @N Anbindung an FS-API
 *
 **********************************************************************/

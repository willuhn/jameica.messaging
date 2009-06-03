/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/Attic/WebdavStore.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/03 14:35:14 $
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
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.webdav.ITransaction;
import net.sf.webdav.IWebdavStore;
import net.sf.webdav.StoredObject;
import net.sf.webdav.exceptions.WebdavException;
import de.willuhn.jameica.messaging.MessageData;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.StorageService;
import de.willuhn.jameica.system.Application;

/**
 * Implementierung des WebDAV-Storage.
 */
public class WebdavStore implements IWebdavStore
{
  /**
   * @see net.sf.webdav.IWebdavStore#begin(java.security.Principal)
   */
  public ITransaction begin(Principal p) throws WebdavException
  {
    return null;
  }

  /**
   * @see net.sf.webdav.IWebdavStore#checkAuthentication(net.sf.webdav.ITransaction)
   */
  public void checkAuthentication(ITransaction t) throws WebdavException
  {
  }

  /**
   * @see net.sf.webdav.IWebdavStore#commit(net.sf.webdav.ITransaction)
   */
  public void commit(ITransaction t) throws WebdavException
  {
  }

  /**
   * @see net.sf.webdav.IWebdavStore#rollback(net.sf.webdav.ITransaction)
   */
  public void rollback(ITransaction t) throws WebdavException
  {
  }


  /**
   * @see net.sf.webdav.IWebdavStore#createFolder(net.sf.webdav.ITransaction, java.lang.String)
   */
  public void createFolder(final ITransaction t, final String name) throws WebdavException
  {
    doAction(new StorageAction()
    {
      public Object doAction(StorageService service) throws Exception
      {
        service.create(name);
        return null;
      }
    });
  }

  /**
   * @see net.sf.webdav.IWebdavStore#createResource(net.sf.webdav.ITransaction, java.lang.String)
   */
  public void createResource(final ITransaction t, final String name) throws WebdavException
  {
    doAction(new StorageAction()
    {
      public Object doAction(StorageService service) throws Exception
      {
        String[] s = split(name);
        
        MessageData msg = new MessageData();
        Map<String,String> props = new HashMap<String,String>();
        props.put(MessageData.PROPERTY.filename.toString(),s[1]);
        if (t != null)
        {
          Principal p = t.getPrincipal();
          if (p != null)
            props.put(MessageData.PROPERTY.username.toString(),p.getName());
        }
        msg.setProperties(props);
        msg.setInputStream(new ByteArrayInputStream(new byte[0])); // leere Datei
        service.put(s[0],msg);
        return null;
      }
    });
  }

  /**
   * @see net.sf.webdav.IWebdavStore#getChildrenNames(net.sf.webdav.ITransaction, java.lang.String)
   */
  public String[] getChildrenNames(final ITransaction t, final String name) throws WebdavException
  {
    return (String[]) doAction(new StorageAction()
    {
      public Object doAction(StorageService service) throws Exception
      {
        List<String> list = new ArrayList<String>();

        String[] subchannels = service.listChannels(name);
        if (subchannels != null && subchannels.length > 0)
          list.addAll(Arrays.asList(subchannels));
        
        // Wir muessen hier noch die Dateinamen rausfischen
        String[] uuids = service.list(name);
        if (uuids != null && uuids.length > 0)
        {
          List<String> files = new ArrayList<String>();
          for (int i=0;i<uuids.length;++i)
          {
            String filename = uuids[i];

            // Checken, ob wir einen Dateinamen in den Properties haben
            MessageData d = new MessageData();
            d.setUuid(uuids[i]);
            service.get(d);
            Map<String,String> props = d.getProperties();
            if (props != null)
            {
              String s = props.get(MessageData.PROPERTY.filename.toString());
              if (s != null)
                filename = s;
            }
            files.add(filename);
          }
          Collections.sort(files);
          list.addAll(files);
        }
        return list.toArray(new String[list.size()]);
      }
    });
  }

  /**
   * @see net.sf.webdav.IWebdavStore#getResourceContent(net.sf.webdav.ITransaction, java.lang.String)
   */
  public InputStream getResourceContent(final ITransaction t, final String name) throws WebdavException
  {
    return (InputStream) doAction(new StorageAction()
    {
      public Object doAction(StorageService service) throws Exception
      {
        MessageData d = find(name);
        if (d == null)
          throw new WebdavException("resource " + name + " not found");

        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);

        d.setOutputStream(pos);
        service.get(d);
        return pis;
      }
    });
  }

  /**
   * @see net.sf.webdav.IWebdavStore#getResourceLength(net.sf.webdav.ITransaction, java.lang.String)
   */
  public long getResourceLength(final ITransaction t, final String name) throws WebdavException
  {
    return (Long) doAction(new StorageAction()
    {
      public Object doAction(StorageService service) throws Exception
      {
        MessageData d = find(name);
        if (d == null)
          throw new WebdavException("resource " + name + " not found");
        
        Map<String,String> props = d.getProperties();
        if (props == null)
            return -1;
          
        String length = props.get(MessageData.PROPERTY.filesize.toString());
        if (length == null || length.length() == 0)
            return -1;

        return Long.parseLong(length);
      }
    });
  }

  /**
   * @see net.sf.webdav.IWebdavStore#getStoredObject(net.sf.webdav.ITransaction, java.lang.String)
   */
  public StoredObject getStoredObject(final ITransaction t, final String name) throws WebdavException
  {
    return (StoredObject) doAction(new StorageAction()
    {
      public Object doAction(StorageService service) throws Exception
      {
        StoredObject o = new StoredObject();
        o.setCreationDate(new Date(0L));
        o.setLastModified(new Date(0L));

        MessageData d = find(name);
        
        if (d == null)
        {
          // Datei existiert nicht. Mal schauen, ob es ein Verzeichnis gibt
          String[] dirs = service.listChannels(name);

          // Existiert ueberhaupt nicht
          if (dirs == null)
            return o; // TODO: Eigentlich muesste man hier NULL liefern, dann kommt in KDE aber ein Fehler

          o.setFolder(true);
          return o;
        }
        
        Map<String,String> props = d.getProperties();
        if (props != null)
        {
          String length = props.get(MessageData.PROPERTY.filesize.toString());
          if (length != null && length.length() > 0)
              o.setResourceLength(Long.parseLong(length));
            
          String created = props.get(MessageData.PROPERTY.created.toString());
          if (created != null && created.length() > 0)
            o.setCreationDate(new Date(Long.parseLong(created)));
            
          String modified = props.get(MessageData.PROPERTY.modified.toString());
          if (modified != null && modified.length() > 0)
            o.setLastModified(new Date(Long.parseLong(modified)));
        }
        return o;
      }
    });
  }

  /**
   * @see net.sf.webdav.IWebdavStore#removeObject(net.sf.webdav.ITransaction, java.lang.String)
   */
  public void removeObject(final ITransaction t, final String name) throws WebdavException
  {
    doAction(new StorageAction()
    {
      public Object doAction(StorageService service) throws Exception
      {
        MessageData d = find(name);
        if (d == null)
          throw new WebdavException("resource " + name + " not found");

        service.delete(d);
        return null;
      }
    });
  }

  /**
   * @see net.sf.webdav.IWebdavStore#setResourceContent(net.sf.webdav.ITransaction, java.lang.String, java.io.InputStream, java.lang.String, java.lang.String)
   */
  public long setResourceContent(final ITransaction t, final String name, final InputStream data, final String contentType, final String encoding) throws WebdavException
  {
    return (Long) doAction(new StorageAction()
    {
      public Object doAction(StorageService service) throws Exception
      {
        MessageData d = find(name);
        if (d == null)
        {
          // Nachricht neu anlegen
          d = new MessageData();
        }

        Map<String,String> props = d.getProperties();
        if (props == null)
        {
          props = new HashMap<String,String>();
          d.setProperties(props);
        }
        
        String[] s = split(name);
        props.put(MessageData.PROPERTY.filename.toString(),s[1]);

        if (t != null)
        {
          Principal p = t.getPrincipal();
          if (p != null)
            props.put(MessageData.PROPERTY.username.toString(),p.getName());
        }
        
        d.setInputStream(data);
        service.put(s[0],d);
        
        String length = props.get(MessageData.PROPERTY.filesize.toString());
        if (length == null || length.length() == 0)
          return -1;
        return Long.parseLong(length);
      }
    });
  }
  
  /**
   * Sucht nach einer Nachricht anhand UUID oder Dateiname.
   * @param name UUID oder Dateiname.
   * @return Nachricht oder NULL, wenn sie nicht gefunden wurde.
   * @throws Exception
   */
  private MessageData find(final String name) throws Exception
  {
    return (MessageData) doAction(new StorageAction()
    {
      public Object doAction(StorageService service) throws Exception
      {
        // Nachricht suchen
        String[] s = split(name);
        String[] uuids = service.list(s[0]);
        if (uuids == null || uuids.length == 0)
          return null; // Ordner ist leer
        
        for (int i=0;i<uuids.length;++i)
        {
          MessageData d = new MessageData();
          d.setUuid(uuids[i]);
          service.get(d);

          // UUID passt
          if (uuids[i].equals(s[1]))
            return d;

          // Metadaten
          Map<String,String> props = d.getProperties();
          if (props == null)
            continue;

          // Stimmt Dateiname oder UUID ueberein?
          String filename = props.get(MessageData.PROPERTY.filename.toString());
          if ((filename != null && filename.equals(s[1])) || (uuids[i].equals(s[1])))
            return d;
        }
        return null;
      }
    });
  }
  
  /**
   * Zerlegt eine URI in Channel und Dateiname.
   * @param uri URI.
   * @return Array mit zwei Elementen.
   * Element 1 enthaelt den Channel.
   * Element 2 den Namen der Datei.
   */
  private String[] split(String uri)
  {
    String[] s = new String[2];
    int index = uri.lastIndexOf("/");
    if (index == -1)
      s[1] = uri;
    else
    {
      s[0] = uri.substring(0,index);
      s[1] = uri.substring(index+1);
    }
    return s;
  }
  /**
   * Hilfsfunktion zum Ausfuehren der Aktionen.
   * @param a auszufuehrende Aktion.
   * @return Rueckgabe-Wert der Aktion.
   * @throws WebdavException
   */
  private Object doAction(StorageAction a) throws WebdavException
  {
    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      return a.doAction(service);
    }
    catch (WebdavException we)
    {
      throw we;
    }
    catch (Exception e)
    {
      throw new WebdavException(e);
    }
  }

  /**
   * Vereinfacht das Arbeiten auf dem Storage-Service.
   */
  private interface StorageAction
  {
    public Object doAction(StorageService service) throws Exception;
  }
}


/**********************************************************************
 * $Log: WebdavStore.java,v $
 * Revision 1.1  2009/06/03 14:35:14  willuhn
 * @N WebDAV-Connector (in progress)
 *
 **********************************************************************/

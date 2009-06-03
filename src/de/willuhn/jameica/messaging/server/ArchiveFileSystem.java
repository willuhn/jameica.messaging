/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/ArchiveFileSystem.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/06/03 16:28:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.server;

import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.willuhn.io.fs.FSException;
import de.willuhn.io.fs.File;
import de.willuhn.io.fs.FileSystem;
import de.willuhn.jameica.messaging.MessageData;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.StorageService;
import de.willuhn.jameica.system.Application;

/**
 * Implementierung der FS-API.
 */
public class ArchiveFileSystem implements FileSystem
{
  private StorageService service = null;
  
  /**
   * Protokoll-Name.
   */
  public final static String PROTOCOL = "archive";

  /**
   * @see de.willuhn.io.fs.FileSystem#init(java.net.URI)
   */
  public void init(URI uri) throws FSException
  {
    try
    {
      // TODO URI auswerten. Sowohl fuer das Verzeichnis als auch fuer Remote-Support
      // Zur Zeit kann das Modul nur innerhalb der JVM genutzt werden, was nicht
      // so viel bringt.
      this.service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
    }
    catch (Exception e)
    {
      throw new FSException(e);
    }
  }

  /**
   * @see de.willuhn.io.fs.FileSystem#close()
   */
  public void close() throws FSException
  {
    this.service = null;
  }

  /**
   * @see de.willuhn.io.fs.FileSystem#create(java.lang.String)
   */
  public File create(String name) throws FSException
  {
    return create(null,name);
  }

  /**
   * @see de.willuhn.io.fs.FileSystem#create(java.lang.String, java.lang.String)
   */
  public File create(String dir, String name) throws FSException
  {
    return new ArchiveFile(this,dir,name);
  }

  /**
   * @see de.willuhn.io.fs.FileSystem#list(java.io.FilenameFilter)
   */
  public String[] list(FilenameFilter filter) throws FSException
  {
    return list(null,filter);
  }

  /**
   * @see de.willuhn.io.fs.FileSystem#list(java.lang.String, java.io.FilenameFilter)
   */
  public String[] list(final String dir, final FilenameFilter filter) throws FSException
  {
    try
    {
      List<String> files = new ArrayList<String>();
      
      String[] uuids = getService().list(dir);
      for (int i=0;i<uuids.length;++i)
      {
        String filename = uuids[i];

        // Checken, ob wir einen Dateinamen in den Properties haben
        MessageData d = new MessageData();
        d.setUuid(uuids[i]);
        getService().get(d);
        Map<String,String> props = d.getProperties();
        if (props != null)
        {
          String s = props.get(MessageData.PROPERTY.filename.toString());
          if (s != null)
            filename = s;
        }
        
        if (filter != null)
        {
          if (!filter.accept(new java.io.File(dir),filename))
            continue;
        }
        files.add(filename);
      }
      Collections.sort(files);
      return files.toArray(new String[files.size()]);
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
   * @see de.willuhn.io.fs.FileSystem#listDirs(java.io.FilenameFilter)
   */
  public String[] listDirs(FilenameFilter filter) throws FSException
  {
    return listDirs(null,filter);
  }

  /**
   * @see de.willuhn.io.fs.FileSystem#listDirs(java.lang.String, java.io.FilenameFilter)
   */
  public String[] listDirs(final String dir, final FilenameFilter filter) throws FSException
  {
    try
    {
      String[] channels = getService().listChannels(dir);
      if (filter == null)
        return channels;

      List<String> dirs = new ArrayList<String>();
      for (int i=0;i<channels.length;++i)
      {
        if (filter.accept(new java.io.File(dir),channels[i]))
          dirs.add(channels[i]);
      }
      Collections.sort(dirs);
      return dirs.toArray(new String[dirs.size()]);
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
   * Prueft, ob das Filesystem geoeffnet ist.
   * @throws FSException
   */
  StorageService getService() throws FSException
  {
    if (this.service == null)
      throw new FSException("filesystem closed");
    return this.service;
  }
}


/**********************************************************************
 * $Log: ArchiveFileSystem.java,v $
 * Revision 1.2  2009/06/03 16:28:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/06/03 16:26:58  willuhn
 * @N Anbindung an FS-API
 *
 **********************************************************************/

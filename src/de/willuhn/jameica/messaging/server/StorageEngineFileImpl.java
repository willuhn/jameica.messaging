/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/Attic/StorageEngineFileImpl.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/01/17 09:48:37 $
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
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.StorageEngine;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Implementierung einer Storage-Engine, die direkt im Dateisystem speichert.
 */
public class StorageEngineFileImpl implements StorageEngine
{
  // Maximal-Anzahl von Nachrichten pro Channel
  private final static int MAX_MESSAGES = 10000;

  /**
   * @see de.willuhn.jameica.messaging.rmi.StorageEngine#get(java.lang.String, java.io.OutputStream)
   */
  public synchronized void get(String uuid, OutputStream os) throws IOException
  {
    if (os == null)
      throw new IOException("no outputstream given");

    File f = find(uuid);

    InputStream is = null;
    try
    {
      Logger.debug("reading message [UUID: " + uuid + "]");
      is = new BufferedInputStream(new FileInputStream(f));
      long started = System.currentTimeMillis();

      byte[] buf = new byte[4096];
      long count = 0;
      int read   = 0;
      while ((read = is.read(buf)) > -1)
      {
        read = is.read(buf);
        if (read > 0) // Nur schreiben, wenn wirklich was gelesen wurde
          os.write(buf,0,read);
        count += read;
      }
      os.flush(); // Stellt sicher, dass alles geschrieben wurde, bevor wir den InputStream schliessen
      Logger.info("message [UUID: " + uuid + " sent (" + count + " bytes in " + (System.currentTimeMillis() - started) + " ms)");
    }
    finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        }
        catch (Exception e)
        {
          Logger.error("error while closing file",e);
        }
      }
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.StorageEngine#next(java.lang.String)
   */
  public synchronized String next(String channel) throws IOException
  {
    File dir = new File(getWorkdir().getAbsolutePath(),escape(channel));
    if (!dir.exists())
      throw new IOException("channel does not exist");

    File[] files = dir.listFiles(new FileFilter() {
      /**
       * @see java.io.FileFilter#accept(java.io.File)
       */
      public boolean accept(File pathname)
      {
        return pathname.isFile() && pathname.canRead();
      }
    });

    // Bevor wir sortieren, schauen wir, ob in dem Verzeichnis ueberhaupt was drin ist
    if (files == null || files.length == 0)
      return null; // Channel leer

    // Wir sortieren nach "LastModified". Alte Dateien zuerst.
    List list = Arrays.asList(files); 
    Collections.sort(list, new Comparator() {
      public int compare(Object f1, Object f2)
      {
        return (int) (((File)f1).lastModified() - ((File)f2).lastModified()); 
      }
    });
    File found = (File) list.get(0);
    return found.getName();
  }
  
  /**
   * @see de.willuhn.jameica.messaging.rmi.StorageEngine#delete(java.lang.String)
   */
  public synchronized void delete(String uuid) throws IOException
  {
    if (uuid == null || uuid.length() == 0)
      throw new IOException("no UUID given");

    File workdir = getWorkdir();
    File current = find(uuid);
    
    while (current.getAbsolutePath().startsWith(workdir.getAbsolutePath()))
    {
      if (!current.exists() || !current.canWrite())
        return;
      File parent = current.getParentFile();
      if (parent != null && workdir.equals(parent))
        return; // Das Work-Verzeichnis selbst
      if (current.isDirectory())
      {
        String[] content = current.list();
        if (content != null && content.length > 0)
          return; // Verzeichnis noch nicht leer
      }
      Logger.info("delete " + current.getAbsolutePath());
      current.delete();
      current = parent;
    }

  }
  
  /**
   * @see de.willuhn.jameica.messaging.rmi.StorageEngine#put(java.lang.String, java.io.InputStream)
   */
  public synchronized String put(String channel, InputStream is) throws IOException
  {
    if (is == null)
      throw new IOException("no message given");

    channel = escape(channel);
    File dir = new File(getWorkdir().getAbsolutePath(),channel);
    if (!dir.exists() && !dir.mkdirs())
      throw new IOException("unable to create message dir");
    
    String[] size = dir.list();
    if (size != null && size.length >= MAX_MESSAGES)
      throw new IOException("message limit (" + + MAX_MESSAGES + " exceeded for channel " + channel);

    OutputStream os = null;
    try
    {
      String uuid = UUID.randomUUID().toString();
      File target = new File(dir,uuid);

      os = new BufferedOutputStream(new FileOutputStream(target));
      long started = System.currentTimeMillis();

      byte[] buf = new byte[4096];
      long count = 0;
      int read   = 0;
      while ((read = is.read(buf)) > -1)
      {
        read = is.read(buf);
        if (read > 0) // Nur schreiben, wenn wirklich was gelesen wurde
          os.write(buf,0,read);
        count += read;
      }
      os.flush();
      Logger.info("[channel: " + channel + "] message received [UUID: " + uuid + "] (" + count + " bytes in " + (System.currentTimeMillis() - started) + " ms)");
      return uuid;
    }
    finally
    {
      if (os != null)
      {
        try
        {
          os.close();
        } catch (Exception e)
        {
          Logger.error("error while closing file",e);
        }
      }
    }
  }
  
  /**
   * Sucht eine Datei anhand der UUID.
   * @param uuid die UUID.
   * @return die Datei - niemals null.
   * @throws IOException wenn die Datei nicht gefunden wurde.
   */
  private File find(String uuid) throws IOException
  {
    if (uuid == null || uuid.length() == 0)
      throw new IOException("no UUID given");

    FileFinder finder = new FileFinder(getWorkdir());
    finder.matches("^" + uuid + "$");
    File[] result = finder.findRecursive();
    if (result == null || result.length == 0)
      throw new IOException("message not found [UUID: " + uuid + "]");
    return result[0];
  }
  
  
  /**
   * Liefert das Workdir.
   * @return das Work-Dir.
   * @throws IOException Wenn das Workdir nicht beschreibbar ist oder nicht erstellt werden konnte.
   */
  private File getWorkdir() throws IOException
  {
    PluginResources res = Application.getPluginLoader().getPlugin(Plugin.class).getResources();
    Settings settings   = res.getSettings();
    File workdir = new File(settings.getString("workdir",res.getWorkPath()),"archive");
    if ((workdir.isDirectory() && workdir.canWrite()) || workdir.mkdirs())
      return workdir;
    throw new IOException("unable to create workdir or not writable: " + workdir.getAbsolutePath());
  }

  /**
   * Bereinigt einen String um alles, was nicht zu einem Verzeichnis-/Dateinamen gehoert.
   * @param name der zu escapende String.
   * @return der escapte String.
   */
  private String escape(String name)
  {
    if (name == null || name.length() == 0)
      return "default";

    // Doppelpunkte gegen doppelte Unterstriche ersetzen
    name = name.replaceAll("(:){1,}","__");

    // Alle Leerzeichen gegen Unterstrich ersetzen
    name = name.replaceAll(" ","_");

    // Mehrfachpunkte gegen einzelne Punkte ersetzen
    name = name.replaceAll("(\\.){2,}",".");

    // Wir nehmen alle Zeichen bis auf Buchstaben, Zahlen, Punkt und Unterstrich raus 
    name = name.replaceAll("[^A-Za-z0-9\\-_\\.]","");

    // Unterstriche am Ende ersetzen wir
    name = name.replaceAll("(_){1,}$","");

    // Vorbereiten der Verzeichnisse
    name = name.replaceAll("\\.",File.separator);

    // und kuerzen noch auf maximal 255 Zeichen
    if (name.length() > 255)
      name = name.substring(0,254);

    return name;
  }

}


/*********************************************************************
 * $Log: StorageEngineFileImpl.java,v $
 * Revision 1.5  2008/01/17 09:48:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2008/01/16 23:31:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2008/01/16 16:44:47  willuhn
 * @N Verwendung von UUIDs fuer die Vergabe der Dateinamen
 * @N Doppel-Funktion des Systems als Archiv und Queue
 *
 * Revision 1.2  2007/12/14 12:04:08  willuhn
 * @C TCP-Listener verwendet jetzt Stream-API
 *
 * Revision 1.1  2007/12/14 11:28:08  willuhn
 * @N Storage-Engine
 *
 **********************************************************************/
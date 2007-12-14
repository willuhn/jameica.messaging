/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/Attic/StorageEngineFileImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/12/14 12:04:08 $
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.text.DecimalFormat;

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
  // Maximal-Anzahl von Nachrichten pro Verzeichnis
  private final static int MAX_MESSAGES = 10000;
  private final static DecimalFormat decimalformat = new DecimalFormat("00000000");

  /**
   * @see de.willuhn.jameica.messaging.rmi.StorageEngine#get(java.lang.String, java.io.OutputStream)
   */
  public synchronized void get(String channel, OutputStream os) throws IOException
  {
    String logPrefix = "[channel: " + channel + "] ";
    if (os == null)
    {
      Logger.info(logPrefix + " no outputstream given");
      return;
    }

    InputStream is = null;
    File target    = prepare(channel,true);

    if (target == null || !target.exists())
    {
      Logger.debug(logPrefix + "queue empty");
      return;
    }
    
    try
    {
      Logger.debug(logPrefix + "reading message file " + target.getAbsolutePath());
      is = new BufferedInputStream(new FileInputStream(target));
      long started = System.currentTimeMillis();

      byte[] buf = new byte[4096];
      long count = 0;
      int read   = 0;
      do
      {
        read = is.read(buf);
        if (read > 0)
          os.write(buf,0,read);
        count += read;
      }
      while (read != -1);
      os.flush();
      Logger.info("[channel: " + channel + "] message sent (" + count + " bytes in " + (System.currentTimeMillis() - started) + " ms)");
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
      cleanup(target);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.rmi.StorageEngine#put(java.lang.String, java.io.InputStream)
   */
  public synchronized void put(String channel, InputStream is) throws IOException
  {
    String logPrefix = "[channel: " + channel + "] ";
    if (is == null)
    {
      Logger.info(logPrefix + " got empty message, ignoring");
      return;
    }
    
    OutputStream os = null;
    try
    {
      File target = prepare(channel,false);
      Logger.debug(logPrefix + "writing message file " + target.getAbsolutePath());
      os = new BufferedOutputStream(new FileOutputStream(target));
      long started = System.currentTimeMillis();

      byte[] buf = new byte[4096];
      long count = 0;
      int read   = 0;
      do
      {
        read = is.read(buf);
        if (read > 0)
          os.write(buf,0,read);
        count += read;
      }
      while (read != -1);
      os.flush();
      Logger.info("[channel: " + channel + "] message received (" + count + " bytes in " + (System.currentTimeMillis() - started) + " ms)");
    }
    catch (IOException e)
    {
      throw new RemoteException("unable to queue message",e);
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
   * Raeumt die Dateien und Queue-Verzeichnisse wieder weg.
   * @param file die zu loeschende Datei.
   * @throws RemoteException
   */
  private synchronized void cleanup(File file) throws RemoteException
  {
    if (file == null)
      return;

    File workdir = getWorkdir();
    File current = file;
    
    while (current.getAbsolutePath().startsWith(workdir.getAbsolutePath()))
    {
      if (!current.exists())
        return;
      File parent = current.getParentFile();
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
   * Liefert das Workdir.
   * @return das Work-Dir.
   * @throws RemoteException Wenn das Workdir nicht beschreibbar ist oder nicht erstellt werden konnte.
   */
  private File getWorkdir() throws RemoteException
  {
    PluginResources res = Application.getPluginLoader().getPlugin(Plugin.class).getResources();
    Settings settings   = res.getSettings();
    File workdir = new File(settings.getString("workdir",res.getWorkPath()),"queue");
    if ((workdir.isDirectory() && workdir.canWrite()) || workdir.mkdirs())
      return workdir;
    throw new RemoteException("unable to create workdir or not writable: " + workdir.getAbsolutePath());
  }
  
  /**
   * Bereitet die Speicherung der Nachricht vor.
   * @param channel Channel.
   * @param read true, wenn die Datei zum Lesen gesucht wird, sonst false.
   * @return Zugehoerige Datei.
   * @throws RemoteException
   */
  private synchronized File prepare(String channel, boolean read) throws RemoteException
  {
    channel = escape(channel);

    File dir = new File(getWorkdir().getAbsolutePath(),channel);
    if (!dir.exists() && !dir.mkdirs())
      throw new RemoteException("unable to create message dir " + dir.getAbsolutePath());

    if (read)
    {
      // Bevor wir alles durchsuchen, checken wir, ob ueberhaupt
      // was in dem Verzeichnis ist
      String[] names = dir.list();
      if (names == null || names.length == 0)
        return null; // Queue leer
    }
    File f = null;
    for (int i=0;i<MAX_MESSAGES;++i)
    {
      f = new File(dir,decimalformat.format(i) + ".msg");
      // Bei read=true nehmen wir die erste gefundene Datei
      // Bei read=false die erste, die noch nicht existiert
      if (read)
      {
        if (f.exists()) // gefunden
          return f;
        continue; // Weitersuchen
      }
      
      // Schreiben
      if (f.exists())
        continue; // Weitersuchen
      return f;
    }
    
    if (read)
      return null; // Das kann eigentlich gar nicht sein, weil wir dann schon oben rausgeflogen waeren
    
    throw new RemoteException("too much messages in this folder, last tested file: " + f);
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

    // und kuerzen noch auf maximal 40 Zeichen
    if (name.length() > 255)
      name = name.substring(0,254);

    return name;
  }

}


/*********************************************************************
 * $Log: StorageEngineFileImpl.java,v $
 * Revision 1.2  2007/12/14 12:04:08  willuhn
 * @C TCP-Listener verwendet jetzt Stream-API
 *
 * Revision 1.1  2007/12/14 11:28:08  willuhn
 * @N Storage-Engine
 *
 **********************************************************************/
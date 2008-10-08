/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rest/Commands.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/10/08 16:01:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import de.willuhn.jameica.messaging.MessageData;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.StorageService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * REST-Kommandos.
 */
public class Commands
{
  @de.willuhn.jameica.webadmin.rest.annotation.InputStream
  private InputStream is = null;
  
  @de.willuhn.jameica.webadmin.rest.annotation.OutputStream
  private OutputStream os = null;
  
  @de.willuhn.jameica.webadmin.rest.annotation.Writer
  private PrintWriter writer = null;

  /**
   * Liefert die Datei mit der angegebenen UUID.
   * @param uuid UUID.
   * @throws IOException
   */
  public void get(String uuid) throws IOException
  {
    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData data = new MessageData();
      data.setOutputStream(os);
      data.setUuid(uuid);
      service.get(data);
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      Logger.error("unable to fetch file, uuid: " + uuid,e2);
      throw new IOException("unable to fetch file, uuid: " + uuid + " - " + e2.getMessage());
    }
  }

  /**
   * Speichert die Daten aus dem Request im angegebenen Channel.
   * @param channel Channel.
   * @throws IOException
   */
  public void put(String channel) throws IOException
  {
    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData data = new MessageData();
      data.setInputStream(is);
      service.put(channel,data);
      writer.print(data.getUuid());
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      Logger.error("unable to store file, channel: " + channel,e2);
      throw new IOException("unable to store file, channel: " + channel + " - " + e2.getMessage());
    }
  }

}


/*********************************************************************
 * $Log: Commands.java,v $
 * Revision 1.2  2008/10/08 16:01:40  willuhn
 * @N REST-Services via Injection (mittels Annotation) mit Context-Daten befuellen
 *
 * Revision 1.1  2008/10/07 23:45:41  willuhn
 * @N Connector fuer Zugriff via HTTP-REST - noch in Arbeit
 *
 **********************************************************************/
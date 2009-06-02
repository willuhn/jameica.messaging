/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rest/Commands.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/06/02 22:59:48 $
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import de.willuhn.jameica.messaging.MessageData;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.StorageService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.rest.annotation.Path;
import de.willuhn.jameica.webadmin.rest.annotation.Request;
import de.willuhn.jameica.webadmin.rest.annotation.Response;
import de.willuhn.logging.Logger;

/**
 * REST-Kommandos.
 */
public class Commands
{
  @Request
  private HttpServletRequest request = null;
  
  @Response
  private HttpServletResponse response = null;
  
  /**
   * Liefert die Datei mit der angegebenen UUID.
   * @param uuid UUID.
   * @throws IOException
   */
  @Path("/message/get/(.*)")
  public void get(String uuid) throws IOException
  {
    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData data = new MessageData();
      data.setOutputStream(response.getOutputStream());
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
   * Liefert die Metadaten zur angegebenen UUID.
   * @param uuid UUID.
   * @throws IOException
   */
  @Path("/message/getmeta/(.*)")
  public void getMeta(String uuid) throws IOException
  {
    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData data = new MessageData();
      data.setUuid(uuid);
      service.getProperties(data);
      response.getWriter().print(new JSONObject(data.getProperties()).toString());
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      Logger.error("unable to fetch meta data, uuid: " + uuid,e2);
      throw new IOException("unable to fetch meta data, uuid: " + uuid + " - " + e2.getMessage());
    }
  }

  /**
   * Speichert die Daten aus dem Request im angegebenen Channel.
   * @param channel Channel.
   * @throws IOException
   */
  @Path("/message/put/(.*)")
  public void put(String channel) throws IOException
  {
    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData data = new MessageData();
      data.setInputStream(request.getInputStream());
      data.setProperties(request.getParameterMap());
      service.put(channel,data);
      response.getWriter().print(data.getUuid());
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

  /**
   * Liefert die naechste UUID aus dem Channel.
   * @param channel Channel.
   * @throws IOException
   */
  @Path("/message/next/(.*)")
  public void next(String channel) throws IOException
  {
    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      String uuid = service.next(channel);
      if (uuid != null)
        response.getWriter().print(uuid);
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      Logger.error("unable to fetch next uuid from channel: " + channel,e2);
      throw new IOException("unable to fetch next uuid from channel: " + channel + " - " + e2.getMessage());
    }
  }

  /**
   * Loescht die Datei mit der UUID aus dem Archiv.
   * @param uuid UUID.
   * @throws IOException
   */
  @Path("/message/delete/(.*)")
  public void delete(String uuid) throws IOException
  {
    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      MessageData data = new MessageData();
      data.setUuid(uuid);
      service.delete(data);
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      Logger.error("unable to delete file, uuid: " + uuid,e2);
      throw new IOException("unable to delete file, channel: " + uuid + " - " + e2.getMessage());
    }
  }

}


/*********************************************************************
 * $Log: Commands.java,v $
 * Revision 1.7  2009/06/02 22:59:48  willuhn
 * @N Funktion zum Abrufen der Meta-Daten
 *
 * Revision 1.6  2008/10/21 22:33:44  willuhn
 * @N Markieren der zu registrierenden REST-Kommandos via Annotation
 *
 * Revision 1.5  2008/10/08 22:05:52  willuhn
 * @N REST-Kommandos vervollstaendigt
 *
 * Revision 1.4  2008/10/08 21:38:38  willuhn
 * @C Nur noch zwei Annotations "Request" und "Response"
 *
 * Revision 1.3  2008/10/08 17:55:11  willuhn
 * @N SOAP-Connector (in progress)
 *
 * Revision 1.2  2008/10/08 16:01:40  willuhn
 * @N REST-Services via Injection (mittels Annotation) mit Context-Daten befuellen
 *
 * Revision 1.1  2008/10/07 23:45:41  willuhn
 * @N Connector fuer Zugriff via HTTP-REST - noch in Arbeit
 *
 **********************************************************************/
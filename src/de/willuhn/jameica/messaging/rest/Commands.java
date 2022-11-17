/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.rest;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import de.willuhn.jameica.messaging.MessageData;
import de.willuhn.jameica.messaging.Plugin;
import de.willuhn.jameica.messaging.rmi.StorageService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.annotation.Path;
import de.willuhn.jameica.webadmin.annotation.Request;
import de.willuhn.jameica.webadmin.annotation.Response;
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

      Map<String,String> props = new HashMap<String,String>();
      Enumeration<String> e = request.getParameterNames();
      while (e.hasMoreElements())
      {
        String key = e.nextElement();
        props.put(key,request.getParameter(key));
      }
      data.setProperties(props);
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
   * Liefert die Liste der UUIDs aus dem Channel.
   * @param channel Channel.
   * @throws IOException
   */
  @Path("/message/list/(.*)")
  public void list(String channel) throws IOException
  {
    try
    {
      StorageService service = (StorageService) Application.getServiceFactory().lookup(Plugin.class,"storage");
      String[] uuids = service.list(channel);
      response.getWriter().print(new JSONArray(uuids != null ? uuids : new String[0]).toString());
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      Logger.error("unable to fetch uuid list from channel: " + channel,e2);
      throw new IOException("unable to fetch uuid list from channel: " + channel + " - " + e2.getMessage());
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

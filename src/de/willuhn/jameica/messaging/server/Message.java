package de.willuhn.jameica.messaging.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;


/**
 * Bean fuer eine einzelne Message.
 * Wird nur serverseitig verwendet.
 */
public class Message
{
  private InputStream inputStream   = null;
  private OutputStream outputStream = null;
  private String uuid               = null;
  private Map attributes            = null;
  
  /**
   * Liefert optionale Attribute fuer die Message.
   * @return optionale Attribute.
   */
  public Map getAttributes()
  {
    return attributes;
  }
  
  /**
   * Speichert optionale Attribute fuer die Message.
   * @param attributes
   */
  public void setAttributes(Map attributes)
  {
    this.attributes = attributes;
  }
  
  /**
   * Liefert einen InputStream fuer die Nutzdaten.
   * @return InputStream fuer die Nutzdaten.
   */
  public InputStream getInputStream()
  {
    return inputStream;
  }
  
  /**
   * Speichert einen InputStream fuer die Nutzdaten.
   * @param inputStream InputStream fuer die Nutzdaten.
   */
  public void setInputStream(InputStream inputStream)
  {
    this.inputStream = inputStream;
  }
  
  /**
   * Liefert einen OutputStream fuer die Nutzdaten.
   * @return OutputStream fuer die Nutzdaten.
   */
  public OutputStream getOutputStream()
  {
    return outputStream;
  }
  
  /**
   * Speichert einen OutputStream fuer die Nutzdaten.
   * @param outputStream OutputStream fuer die Nutzdaten.
   */
  public void setOutputStream(OutputStream outputStream)
  {
    this.outputStream = outputStream;
  }
  
  /**
   * Liefert die UUID.
   * @return UUID.
   */
  public String getUuid()
  {
    return uuid;
  }
  
  /**
   * Speichert die UUID.
   * @param uuid UUID.
   */
  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }
  
}


/**********************************************************************
 * $Log: Message.java,v $
 * Revision 1.1  2008/10/06 23:30:45  willuhn
 * @N Support fuer Properties in Messages
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;


/**
 * Bean fuer eine einzelne Message.
 * Wird nur serverseitig verwendet.
 */
public class MessageData
{
  private InputStream inputStream           = null;
  private OutputStream outputStream         = null;
  private String uuid                       = null;
  private Map properties                    = null;
  
  /**
   * Liefert optionale Attribute fuer die Message.
   * @return optionale Attribute.
   */
  public Map getProperties()
  {
    return properties;
  }
  
  /**
   * Speichert optionale Attribute fuer die Message.
   * @param properties
   */
  public void setProperties(Map properties)
  {
    this.properties = properties;
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
 * $Log: MessageData.java,v $
 * Revision 1.2  2009/05/29 16:24:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2008/10/07 23:03:34  willuhn
 * @C "queue" und "archive" entfernt. Zugriff jetzt direkt ueber Connectoren
 *
 * Revision 1.1  2008/10/06 23:30:45  willuhn
 * @N Support fuer Properties in Messages
 *
 **********************************************************************/

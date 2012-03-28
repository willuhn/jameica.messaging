/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/ConnectorRestServiceImpl.java,v $
 * $Revision: 1.11 $
 * $Date: 2012/03/28 22:28:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.server;

import java.rmi.RemoteException;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.rest.Commands;
import de.willuhn.jameica.messaging.rmi.ConnectorRestService;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Implementierung des Connectors fuer Zugriff via REST-URLs.
 */
public class ConnectorRestServiceImpl implements ConnectorRestService
{
  private Commands bean           = null;
  private RestConsumer consumer   = new RestConsumer();

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "connector.rest";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return !this.isStarted();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return this.bean != null;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (this.isStarted())
    {
      Logger.warn("service allready started, skipping request");
      return;
    }

    // Wir checken, ob das Webadmin-Plugin verfuegbar ist
    Plugin p = Application.getPluginLoader().getPlugin("de.willuhn.jameica.webadmin.Plugin");
    if (p == null)
    {
      Logger.info("plugin jameica.webadmin not installed, skipping REST service");
      return;
    }
    this.bean = new Commands();
    
    // Wir registrieren uns explizit - fuer den Fall, dass der REST-Service schon laeuft
    Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.register").sendMessage(new QueryMessage(bean));
    
    // Wir registrieren und implizit - und lassen uns benachrichtigen, wenn der REST-Service startet.
    Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.start").registerMessageConsumer(this.consumer);
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if (!this.isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }

    try
    {
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.unregister").sendMessage(new QueryMessage(this.bean));
    }
    finally
    {
      this.bean = null;

      // Wir wollen kuenftig auch nicht mehr benachrichtigt, wenn der REST-Service startet.
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.start").unRegisterMessageConsumer(this.consumer);
    }
  }
  
  /**
   * Hilfsklasse zum Registrieren der REST-Kommandos.
   */
  private class RestConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{Message.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.register").sendMessage(new QueryMessage(bean));
    }
    
  }

}


/**********************************************************************
 * $Log: ConnectorRestServiceImpl.java,v $
 * Revision 1.11  2012/03/28 22:28:19  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.10  2008/12/09 16:50:03  willuhn
 * @N Abhaengigkeiten optional deklariert
 *
 * Revision 1.9  2008/10/21 22:33:44  willuhn
 * @N Markieren der zu registrierenden REST-Kommandos via Annotation
 *
 * Revision 1.8  2008/10/08 23:26:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2008/10/08 23:18:39  willuhn
 * @B bugfixing
 * @N SoapTest
 *
 * Revision 1.6  2008/10/08 22:08:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2008/10/08 22:05:52  willuhn
 * @N REST-Kommandos vervollstaendigt
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

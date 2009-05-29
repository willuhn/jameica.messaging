/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/ConnectorSoapServiceImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/05/29 15:53:06 $
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

import javax.jws.WebMethod;
import javax.jws.WebService;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.messaging.rmi.ConnectorSoapService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Implementierung des SOAP-Connectors.
 */
@WebService(endpointInterface="de.willuhn.jameica.messaging.rmi.ConnectorSoapService")
public class ConnectorSoapServiceImpl extends AbstractConnectorWebServiceImpl implements ConnectorSoapService
{
  /**
   * ct.
   */
  public ConnectorSoapServiceImpl()
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  @WebMethod(exclude=true)
  public String getName() throws RemoteException
  {
    return "connector.soap";
  }

  /**
   * @see de.willuhn.jameica.messaging.server.AbstractConnectorWebServiceImpl#isStartable()
   */
  @WebMethod(exclude=true)
  public boolean isStartable() throws RemoteException
  {
    return super.isStartable();
  }

  /**
   * @see de.willuhn.jameica.messaging.server.AbstractConnectorWebServiceImpl#isStarted()
   */
  @WebMethod(exclude=true)
  public boolean isStarted() throws RemoteException
  {
    return super.isStarted();
  }

  /**
   * @see de.willuhn.jameica.messaging.server.AbstractConnectorWebServiceImpl#stop(boolean)
   */
  @WebMethod(exclude=true)
  public void stop(boolean arg0) throws RemoteException
  {
    super.stop(arg0);
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  @WebMethod(exclude=true)
  public void start() throws RemoteException
  {
    if(this.isStarted())
    {
      Logger.warn("service allready started, skipping request");
      return;
    }
    super.start();
    // wir lassen uns benachrichtigen, wenn wir den SOAP-Service deployen koennen.
    Application.getMessagingFactory().registerMessageConsumer(new SoapConsumer());
  }

  /**
   * Hilfsklasse zum Registrieren des SOAP-Services.
   */
  private class SoapConsumer implements MessageConsumer
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
      return new Class[]{SystemMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      SystemMessage msg = (SystemMessage) message;
      if (msg.getStatusCode() == SystemMessage.SYSTEM_STARTED)
        Application.getMessagingFactory().getMessagingQueue("jameica.soap.publish").sendMessage(new QueryMessage("/message",ConnectorSoapServiceImpl.this));
    }
    
  }

}


/**********************************************************************
 * $Log: ConnectorSoapServiceImpl.java,v $
 * Revision 1.3  2009/05/29 15:53:06  willuhn
 * @N Gemeinsame Basis-Klasse fuer Web-Connectoren
 *
 * Revision 1.2  2008/10/08 23:24:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2008/10/08 23:18:39  willuhn
 * @B bugfixing
 * @N SoapTest
 *
 **********************************************************************/

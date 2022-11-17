/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
   * @see de.willuhn.datasource.Service#getName()
   */
  @WebMethod(exclude=true)
  public String getName() throws RemoteException
  {
    return "connector.soap";
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  @WebMethod(exclude=true)
  public void start() throws RemoteException
  {
    if(this.isStarted())
    {
      Logger.warn("service already started, skipping request");
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

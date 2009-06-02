/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/server/ConnectorXmlRpcServiceImpl.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/06/02 22:34:44 $
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

import de.willuhn.jameica.messaging.rmi.ConnectorXmlRpcService;


/**
 * Implementierung des XML-RPC-Connectors.
 */
public class ConnectorXmlRpcServiceImpl extends AbstractConnectorWebServiceImpl implements ConnectorXmlRpcService
{
  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "connector.xmlrpc";
  }
}


/**********************************************************************
 * $Log: ConnectorXmlRpcServiceImpl.java,v $
 * Revision 1.5  2009/06/02 22:34:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2009/05/29 15:53:06  willuhn
 * @N Gemeinsame Basis-Klasse fuer Web-Connectoren
 *
 * Revision 1.3  2008/10/08 23:18:39  willuhn
 * @B bugfixing
 * @N SoapTest
 *
 * Revision 1.2  2008/10/08 17:55:11  willuhn
 * @N SOAP-Connector (in progress)
 *
 * Revision 1.1  2008/10/07 23:03:34  willuhn
 * @C "queue" und "archive" entfernt. Zugriff jetzt direkt ueber Connectoren
 *
 **********************************************************************/

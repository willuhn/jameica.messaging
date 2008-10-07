/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/ConnectorTcpService.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/10/07 23:03:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.rmi;

import de.willuhn.datasource.Service;


/**
 * Connector fuer den Zugriff via Plain TCP (z.Bsp. Telnet).
 */
public interface ConnectorTcpService extends Service
{

}


/**********************************************************************
 * $Log: ConnectorTcpService.java,v $
 * Revision 1.1  2008/10/07 23:03:34  willuhn
 * @C "queue" und "archive" entfernt. Zugriff jetzt direkt ueber Connectoren
 *
 * Revision 1.1  2007/12/13 23:31:38  willuhn
 * @N initial import
 *
 **********************************************************************/

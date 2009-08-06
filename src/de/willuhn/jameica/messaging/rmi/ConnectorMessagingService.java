/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/ConnectorMessagingService.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/06 17:06:07 $
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
 * Service, der einen Jameica-Messageconsumer fuer den Empfang von Nachrichten erstellt.
 */
public interface ConnectorMessagingService extends Service
{

}


/**********************************************************************
 * $Log: ConnectorMessagingService.java,v $
 * Revision 1.1  2009/08/06 17:06:07  willuhn
 * @N Connector, der Archiv-Nachrichten mittels Jameica-Messaging entgegennimmt. Auf diese Weise kann man bequem aus einem anderen Plugin Daten archivieren, ohne ueber TCP gehen zu muessen
 *
 **********************************************************************/

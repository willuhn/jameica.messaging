/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rmi/Attic/QueueService.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/01/16 16:44:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.rmi;


/**
 * Message-Service, der als Queue arbeitet. Nachrichten koennen
 * durch Angabe des Channels chronologisch abgerufen werden,
 * Achtung: Die Nachricht wird automatisch vom Server geloescht,
 * nachdem sie abgerufen wurde.
 */
public interface QueueService extends MessageService
{
}


/*********************************************************************
 * $Log: QueueService.java,v $
 * Revision 1.4  2008/01/16 16:44:47  willuhn
 * @N Verwendung von UUIDs fuer die Vergabe der Dateinamen
 * @N Doppel-Funktion des Systems als Archiv und Queue
 *
 * Revision 1.3  2007/12/14 12:04:08  willuhn
 * @C TCP-Listener verwendet jetzt Stream-API
 *
 * Revision 1.2  2007/12/14 09:56:59  willuhn
 * @N Channel-Angabe mit Punkt-Notation
 *
 * Revision 1.1  2007/12/13 23:31:38  willuhn
 * @N initial import
 *
 * Revision 1.1  2007/12/13 16:11:51  willuhn
 * @N Generischer Message-Queue-Service
 *
 **********************************************************************/
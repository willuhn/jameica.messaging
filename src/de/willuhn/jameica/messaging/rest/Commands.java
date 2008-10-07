/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/rest/Commands.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/10/07 23:45:41 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.rest;

import java.io.IOException;

/**
 * REST-Kommandos.
 */
public class Commands
{
  /**
   * Pattern fuer das GET-Kommando.
   */
  public final static String PATTERN_GET = "/messaging/get/(.*)";
  
  /**
   * @param uuid
   * @throws IOException
   */
  public void get(String uuid) throws IOException
  {
    // TODO: Hier muesste ich eigentlich noch "setContext(Context)"
    // implementieren, um an den OutputStream zum Browser zu kommen.
    // Dann haette ich aber (wegen der Klasse "Context") eine
    // Compile-Abhaengigkeit zu jameica.webadmin
    throw new IOException("fetch data for uuid: " + uuid + " not implemented");
  }
}


/*********************************************************************
 * $Log: Commands.java,v $
 * Revision 1.1  2008/10/07 23:45:41  willuhn
 * @N Connector fuer Zugriff via HTTP-REST - noch in Arbeit
 *
 **********************************************************************/
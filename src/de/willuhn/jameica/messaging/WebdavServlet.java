/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/Attic/WebdavServlet.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/03 14:35:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import javax.servlet.ServletException;

import net.sf.webdav.WebDavServletBean;
import de.willuhn.jameica.messaging.server.WebdavStore;

/**
 * Unsere eigene Implementierung der Servlet-Bean.
 */
public class WebdavServlet extends WebDavServletBean
{

  /**
   * @see javax.servlet.GenericServlet#init()
   */
  public void init() throws ServletException
  {
    super.init(new WebdavStore(), "", "", 0,false);
  }
}


/**********************************************************************
 * $Log: WebdavServlet.java,v $
 * Revision 1.1  2009/06/03 14:35:14  willuhn
 * @N WebDAV-Connector (in progress)
 *
 **********************************************************************/

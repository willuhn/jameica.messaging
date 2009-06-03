/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/src/de/willuhn/jameica/messaging/Attic/WebDeployer.java,v $
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

import java.io.File;

import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer;

/**
 * Deployer fuer das Pmtool-Webfrontend.
 */
public class WebDeployer extends AbstractWebAppDeployer
{
  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer#getContext()
   */
  protected String getContext()
  {
    return "/message";
  }

  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer#getPath()
   */
  protected String getPath()
  {
    Manifest mf = Application.getPluginLoader().getManifest(Plugin.class);
    return mf.getPluginDir() + File.separator + "webapps" + File.separator + "message";
  }
}


/*********************************************************************
 **********************************************************************/
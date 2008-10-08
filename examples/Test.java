/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.messaging/examples/Test.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/10/08 23:18:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;

import de.willuhn.jameica.messaging.rmi.ConnectorSoapService;

/**
 * Test-Client fuer den Zugriff via SOAP.
 */
public class Test
{
  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    // URL
    String url = "https://localhost:8080/soap/message";
    
    JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(ConnectorSoapService.class);
    factory.setAddress(url);
    
    ConnectorSoapService client = (ConnectorSoapService) factory.create();
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Ggf SSL initialisieren
    if (url.startsWith("https://"))
    {
      org.apache.cxf.endpoint.Client proxy = ClientProxy.getClient(client);

      HTTPConduit conduit = (HTTPConduit) proxy.getConduit();
      
      TLSClientParameters tcp = new TLSClientParameters();
      tcp.setDisableCNCheck(true);
      tcp.setTrustManagers(new TrustManager[]{new DummyTrustManager()});
      conduit.setTlsClientParameters(tcp);
      
      // Authentifizierung noetig?
      AuthorizationPolicy auth = conduit.getAuthorization();
      if (auth == null) auth = new AuthorizationPolicy();
      auth.setUserName("admin");
      auth.setPassword("test");
    }
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // 1. Message senden
    System.out.println("Sende Message");
    byte[] data = "Das ist ein Test".getBytes();
    
    HashMap metadata = new HashMap();
    metadata.put("filename","foobar.doc");
    metadata.put("date",new Date().toString());

    String uuid = client.put("test.soap",data,metadata);
    System.out.println("UUID: " + uuid);
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // 2. Message abrufen
    System.out.println("Message abrufen");
    data     = client.get(uuid);
    metadata = client.getProperties(uuid);
    System.out.println("Daten: " + new String(data));
    System.out.println("Meta-Daten: " + metadata.toString());
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // 3. Message loeschen
    System.out.println("Message loeschen");
    client.delete(uuid);
    ////////////////////////////////////////////////////////////////////////////

  }
  
  /**
   * Dummy-Trustmanager.
   * ACHTUNG: Hier findet keine Zertifikatspruefung statt. Nur zum Testen nutzen!
   */
  private static class DummyTrustManager implements X509TrustManager
  {
    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers()
    {
      return null;
    }
    
  }
}


/*********************************************************************
 * $Log: Test.java,v $
 * Revision 1.1  2008/10/08 23:18:39  willuhn
 * @B bugfixing
 * @N SoapTest
 *
 **********************************************************************/
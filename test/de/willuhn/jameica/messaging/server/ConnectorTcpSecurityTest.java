/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.messaging.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Security policy tests for the explicit legacy TCP compatibility mode.
 */
public class ConnectorTcpSecurityTest
{
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  /** Listener startup requires explicit opt-in. */
  @Test
  public void listenerIsOptIn()
  {
    Assert.assertFalse(ConnectorTcpServiceImpl.isLegacyListenerEnabled(false));
    Assert.assertTrue(ConnectorTcpServiceImpl.isLegacyListenerEnabled(true));
  }

  /** Regex and non-canonical identifiers must not select archive files. */
  @Test
  public void rejectNonCanonicalUuids() throws Exception
  {
    Assert.assertEquals("123e4567-e89b-42d3-a456-426614174000",StorageServiceFileImpl.validateUuid("123e4567-e89b-42d3-a456-426614174000"));
    Assert.assertEquals("123E4567-E89B-42D3-A456-426614174000",StorageServiceFileImpl.validateUuid("123E4567-E89B-42D3-A456-426614174000"));
    String[] invalid = new String[]{".*","123e4567-e89b-12d3-a456","../archive"};
    for (String uuid:invalid)
    {
      try
      {
        StorageServiceFileImpl.validateUuid(uuid);
        Assert.fail("accepted invalid UUID: " + uuid);
      }
      catch (IOException expected)
      {
        // expected
      }
    }
  }

  /** Oversized request bodies fail before the extra byte is returned. */
  @Test
  public void rejectOversizedBody() throws Exception
  {
    BoundedInputStream input = new BoundedInputStream(new ByteArrayInputStream(new byte[]{1,2,3,4,5}),4);
    Assert.assertEquals(1,input.read());
    Assert.assertEquals(2,input.read());
    Assert.assertEquals(3,input.read());
    Assert.assertEquals(4,input.read());
    try
    {
      input.read();
      Assert.fail("oversized body accepted");
    }
    catch (IOException expected)
    {
      // expected
    }
  }

  /** Bulk reads enforce the same boundary and never replace valid data. */
  @Test
  public void rejectOversizedBulkBodyWithoutPartialFile() throws Exception
  {
    File target = folder.newFile("message");
    Files.write(target.toPath(),"original".getBytes(StandardCharsets.UTF_8));
    byte[] body = new byte[4097];
    BoundedInputStream input = new BoundedInputStream(new ByteArrayInputStream(body),4096);
    try
    {
      StorageServiceFileImpl.replaceFromStream(target,input);
      Assert.fail("oversized body accepted");
    }
    catch (IOException expected)
    {
      // expected
    }
    Assert.assertEquals("original",Files.readString(target.toPath(),StandardCharsets.UTF_8));
    Assert.assertArrayEquals(new String[]{"message"},folder.getRoot().list());
  }

  /** The serialized aggregate metadata, not only one request, is bounded. */
  @Test
  public void rejectOversizedAggregateMetadata() throws Exception
  {
    Properties properties = new Properties();
    StringBuilder value = new StringBuilder();
    for (int i=0;i<StorageServiceFileImpl.MAX_METADATA_BYTES;++i)
      value.append('x');
    properties.setProperty("key",value.toString());
    try
    {
      StorageServiceFileImpl.encodeProperties(properties,"test");
      Assert.fail("oversized aggregate metadata accepted");
    }
    catch (IOException expected)
    {
      // expected
    }
  }
}

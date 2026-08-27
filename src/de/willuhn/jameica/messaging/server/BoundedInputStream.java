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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream that rejects data after a fixed byte limit.
 */
final class BoundedInputStream extends FilterInputStream
{
  private final long limit;
  private long count;

  /**
   * @param input source stream.
   * @param limit maximum number of bytes.
   */
  BoundedInputStream(InputStream input, long limit)
  {
    super(input);
    if (limit < 0)
      throw new IllegalArgumentException("negative input limit");
    this.limit = limit;
  }

  @Override
  public int read() throws IOException
  {
    int value = super.read();
    if (value >= 0)
      add(1);
    return value;
  }

  @Override
  public int read(byte[] data, int offset, int length) throws IOException
  {
    int read = super.read(data,offset,length);
    if (read > 0)
      add(read);
    return read;
  }

  private void add(long bytes) throws IOException
  {
    this.count += bytes;
    if (this.count > this.limit)
      throw new IOException("request body exceeds limit of " + this.limit + " bytes");
  }
}

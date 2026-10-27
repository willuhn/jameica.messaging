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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** Output stream that rejects data after a fixed byte limit. */
final class BoundedOutputStream extends FilterOutputStream
{
  private final long limit;
  private long count;

  BoundedOutputStream(OutputStream output, long limit)
  {
    super(output);
    if (limit < 0)
      throw new IllegalArgumentException("negative output limit");
    this.limit = limit;
  }

  @Override
  public void write(int value) throws IOException
  {
    ensureCapacity(1);
    this.out.write(value);
    this.count++;
  }

  @Override
  public void write(byte[] data, int offset, int length) throws IOException
  {
    ensureCapacity(length);
    this.out.write(data,offset,length);
    this.count += length;
  }

  private void ensureCapacity(int additional) throws IOException
  {
    if (additional < 0 || this.count > this.limit - additional)
      throw new IOException("output exceeds limit of " + this.limit + " bytes");
  }
}

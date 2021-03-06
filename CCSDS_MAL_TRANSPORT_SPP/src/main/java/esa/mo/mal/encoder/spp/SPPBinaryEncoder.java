/* ----------------------------------------------------------------------------
 * Copyright (C) 2015      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : CCSDS MO SPP Transport Framework
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.mal.encoder.spp;

import esa.mo.mal.encoder.binary.fixed.FixedBinaryEncoder.FixedStreamHolder;
import esa.mo.mal.encoder.gen.GENEncoder;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALListEncoder;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.Duration;
import org.ccsds.moims.mo.mal.structures.FineTime;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.Time;
import org.ccsds.moims.mo.mal.structures.ULong;
import org.ccsds.moims.mo.mal.structures.URI;

/**
 * Implements the MALEncoder and MALListEncoder interfaces for a SPP binary encoding.
 */
public class SPPBinaryEncoder extends GENEncoder
{
  private static final byte[] PADDING =
  {
    0, 0, 0, 0, 0, 0, 0, 0
  };
  protected static final BigInteger ZERO = new BigInteger("0");
  protected static final BigInteger MAX_ULONG = new BigInteger("18446744073709551615");
  private final boolean smallLengthField;

  /**
   * Constructor.
   *
   * @param os Output stream to write to.
   * @param smallLengthField True if length field is 16bits, otherwise assumed to be 32bits.
   */
  public SPPBinaryEncoder(final OutputStream os, final boolean smallLengthField)
  {
    super(new SPPStreamHolder(os, smallLengthField));

    this.smallLengthField = smallLengthField;
  }

  @Override
  public MALListEncoder createListEncoder(List value) throws MALException
  {
    try
    {
      if (smallLengthField)
      {
        outputStream.addUnsignedShort((short) value.size());
      }
      else
      {
        outputStream.addUnsignedInt((short) value.size());
      }

      return this;
    }
    catch (IOException ex)
    {
      throw new MALException(ENCODING_EXCEPTION_STR, ex);
    }
  }

  @Override
  public void encodeNullableBoolean(final Boolean value) throws MALException
  {
    try
    {
      if (null != value)
      {
        outputStream.addNotNull();
        encodeBoolean(value);
      }
      else
      {
        outputStream.addIsNull();
      }
    }
    catch (IOException ex)
    {
      throw new MALException(ENCODING_EXCEPTION_STR, ex);
    }
  }

  @Override
  public void encodeULong(final ULong value) throws IllegalArgumentException, MALException
  {
    try
    {
      BigInteger v = value.getValue();
      if (-1 == v.signum())
      {
        v = ZERO;
      }
      else if (0 > MAX_ULONG.compareTo(v))
      {
        v = MAX_ULONG;
      }

      byte[] buf = v.toByteArray();
      int pad = 8 - (buf.length - 1);
      if (0 < pad)
      {
        outputStream.directAdd(PADDING, 0, pad);
      }
      outputStream.directAdd(buf, 1, buf.length - 1);
    }
    catch (IOException ex)
    {
      throw new MALException(ENCODING_EXCEPTION_STR, ex);
    }
  }

  @Override
  public void encodeNullableULong(final ULong value) throws MALException
  {
    try
    {
      if (null != value)
      {
        outputStream.addNotNull();
        encodeULong(value);
      }
      else
      {
        outputStream.addIsNull();
      }
    }
    catch (IOException ex)
    {
      throw new MALException(ENCODING_EXCEPTION_STR, ex);
    }
  }

  @Override
  public void encodeDuration(Duration value) throws MALException
  {
    long tm = (long)(value.getValue() * 1000);

    int ms = (int) (tm % 1000);
    int s = (int) (tm / 1000);

    try
    {
      outputStream.directAdd(java.nio.ByteBuffer.allocate(4).putInt(s).array(), 0, 4);
      outputStream.directAdd(java.nio.ByteBuffer.allocate(4).putInt(ms).array(), 1, 3);
    }
    catch (IOException ex)
    {
      throw new MALException(ENCODING_EXCEPTION_STR, ex);
    }
  }

  @Override
  public void encodeFineTime(FineTime value) throws MALException
  {
    long tm = value.getValue();

    int ms = (int) (tm % 1000);
    int s = (int) (tm / 1000);

    try
    {
      outputStream.directAdd(java.nio.ByteBuffer.allocate(4).putInt(s).array(), 0, 4);
      outputStream.directAdd(java.nio.ByteBuffer.allocate(4).putInt(ms).array(), 1, 3);
    }
    catch (IOException ex)
    {
      throw new MALException(ENCODING_EXCEPTION_STR, ex);
    }
  }

  @Override
  public void encodeTime(Time value) throws MALException
  {
    long tm = value.getValue();

    int ms = (int) (tm % 1000);
    int s = (int) (tm / 1000);

    try
    {
      outputStream.directAdd(java.nio.ByteBuffer.allocate(4).putInt(s).array(), 0, 4);
      outputStream.directAdd(java.nio.ByteBuffer.allocate(4).putInt(ms).array(), 1, 3);
    }
    catch (IOException ex)
    {
      throw new MALException(ENCODING_EXCEPTION_STR, ex);
    }
  }

  @Override
  public void encodeNullableBlob(Blob value) throws MALException
  {
    try
    {
      if ((null != value)
              && ((value.isURLBased() && (null != value.getURL()))
              || (!value.isURLBased() && (null != value.getValue()))))
      {
        outputStream.addNotNull();
        encodeBlob(value);
      }
      else
      {
        outputStream.addIsNull();
      }
    }
    catch (IOException ex)
    {
      throw new MALException(ENCODING_EXCEPTION_STR, ex);
    }
  }

  @Override
  public void encodeNullableString(String value) throws MALException
  {
    try
    {
      if (null != value)
      {
        outputStream.addNotNull();
        encodeString(value);
      }
      else
      {
        outputStream.addIsNull();
      }
    }
    catch (IOException ex)
    {
      throw new MALException(ENCODING_EXCEPTION_STR, ex);
    }
  }

  @Override
  public void encodeNullableIdentifier(Identifier value) throws MALException
  {
    try
    {
      if (null != value)
      {
        outputStream.addNotNull();
        encodeIdentifier(value);
      }
      else
      {
        outputStream.addIsNull();
      }
    }
    catch (IOException ex)
    {
      throw new MALException(ENCODING_EXCEPTION_STR, ex);
    }
  }

  @Override
  public void encodeNullableURI(URI value) throws MALException
  {
    try
    {
      if (null != value)
      {
        outputStream.addNotNull();
        encodeURI(value);
      }
      else
      {
        outputStream.addIsNull();
      }
    }
    catch (IOException ex)
    {
      throw new MALException(ENCODING_EXCEPTION_STR, ex);
    }
  }

  @Override
  protected byte internalEncodeAttributeType(byte value) throws MALException
  {
    return (byte) (value - 1);
  }

  /**
   * Extends the FixedStreamHolder class for handling SPP fields.
   */
  protected static class SPPStreamHolder extends FixedStreamHolder
  {
    private final boolean smallLengthField;

    /**
     * Constructor.
     *
     * @param outputStream The output stream to encode into.
     * @param smallLengthField True if length field is 16bits, otherwise assumed to be 32bits.
     */
    public SPPStreamHolder(OutputStream outputStream, final boolean smallLengthField)
    {
      super(outputStream);

      this.smallLengthField = smallLengthField;
    }

    @Override
    public void addBytes(byte[] val) throws IOException
    {
      if (null == val)
      {
        if (smallLengthField)
        {
          addSignedShort((short) -1);
        }
        else
        {
          addSignedInt(-1);
        }
      }
      else
      {
        if (smallLengthField)
        {
          addSignedShort((short) val.length);
        }
        else
        {
          addSignedInt(val.length);
        }
        directAdd(val);
      }
    }
  }
}

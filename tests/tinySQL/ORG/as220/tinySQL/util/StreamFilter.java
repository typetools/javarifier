//=============================================================================
/*
$Id: StreamFilter.java,v 1.1 2008-05-25 04:58:11 jaimeq Exp $
*/
//=============================================================================
package ORG.as220.tinySQL.util;

//Required Java's libraries;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;

/**
 * This object convert a <code>InputStream</code> in an array of bytes that we
 * can send to database through a <code>SQL</code> statement. We need to do this
 * because some fields are in binary type and the only way to store data is
 * sending bytes to database process it.
 * @author <a href='mailto:GuardianOfSteel@netscape.net'>Edson Alves Pereira</a> - 30/12/2001
 * @version $Revision: 1.1 $
 */
public class StreamFilter
{
  /**
   * Here is where we keep the informations that will be processed.
   */
  private InputStream isValue;

  /**
   * The default length of data that we read from the stream.
   */
  public static final short BLOCK_SIZE = 4096;

  /**
   * Build a new instance of this object.
   */
  public StreamFilter()
  {
    isValue = null;
  }

  /**
   * Create a instance of this object and automatic execute the tasks to convert
   * the data.
   * @param isValue_ A stream that must be converted to <code>bytes</code> or
   * <code>string</code>.
   */
  public StreamFilter(InputStream isValue_)
  {
    setValue(isValue_);
  }

  public void setValue(InputStream isValue_)
  {
    if (isValue_ != null)
      isValue = isValue_;
  }

  public InputStream getValue()
  {
    return isValue;
  }

  /**
   * Read all data inside the stream and convert to an array of bytes that's
   * easier to treat.
   * @return An array of bytes.
   */
  public synchronized byte[] StreamToBytes()
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte bData[ ] = new byte[BLOCK_SIZE];
    byte bCheckedByte = '\0';
    int iArrayLength = 0;

    try
    {
      //Reading what is stored in this stream:
      iArrayLength = readBlock(bData);

      //Inicializing the transfer:
      baos.write('\'');

      for (int iPos = 0; iPos < iArrayLength; iPos++)
      {
        /*Analysing if the current caracter is something that could make an error
        with the first write( ) above:*/
        bCheckedByte = checkByteOK(bData[iPos]);

        if (bCheckedByte != '\0')
          baos.write(bCheckedByte);

        //Now we can send the byte:
        baos.write(bData[iPos]);
      }

      //Closing the data that will be passed to the field:
      baos.write('\'');
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    return baos.toByteArray();
  }

  /**
   * Convert the data inside the stream in this object to a string
   * representation in the default plataform encoding system.
   * @return A string value from the stream passed to this object.
   */
  public synchronized String bytesToString()
  {
    return new String(StreamToBytes());
  }

  /**
   * Catch the values inside an object and save then in a stream.
   * @param oValue_ An object to be converted.
   * @return Teh stream that has the values from the object.
   * @exception IOException If we couldn't save data.
   */
  public static ByteArrayInputStream objectToBytes(Object oValue_)
      throws IOException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);

    //Passing the value to the stream:
    oos.writeObject(oValue_);

    //Saving data:
    oos.flush();
    oos.close();

    baos.flush();
    baos.close();

    byte bBytes[ ] = baos.toByteArray();

    return new ByteArrayInputStream(bBytes);
  }

  /**
   * Extract all data inside a <code>StreamReader</code> and save they in
   * a<code>ByteArrayInputStream</code> that is easyer to get the bytes and
   * create an array.
   * @param reader_ A <code>Reader</code> that contain data.
   * @return A <code>ByteArrayInputStream</code> to get the bytes.
   * @see ByteArrayInputStream
   * @see Reader
   */
  public static ByteArrayInputStream readerToBytes(Reader reader_)
  {
    return null;
  }

  /**
   * Verify if we need to use an special [ \ ] before the current caracter to
   * avoid break up the string array that is building now, otherwise send a
   * <code>null</code> value saying that nothing should be done.
   * @param bData_ A caracter that we check if could break the string.
   * @return A [ \ ] or <code>null</code> caracter if the parameter is ok.
   */
  private synchronized byte checkByteOK(byte bData_)
  {
    byte bByteCheck = '\0';

    /*If the caracter is one of theses values we need to put an [ \ ] before to
    avoid string errors:*/
    if (bData_ == '\0' || bData_ == '\'' || bData_ == '"' || bData_ == '\\')
      bByteCheck = '\\';

    return bByteCheck;
  }

  /**
   * Read a block of bytes that is passed to this method from the stream that we
   * have in this moment.
   * @param bData_ An array of byte to store the bytes from the stream.
   * @exception IOException If we couldn't read the stream.
   */
  private synchronized int readBlock(byte bData_[ ])
      throws IOException
  {
    return getValue().read(bData_);
  }
}

//=============================================================================

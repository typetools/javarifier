//=============================================================================
/*
$Id: tinySQLPreparedStatement.java,v 1.1 2008-05-25 04:57:52 jaimeq Exp $
*/
//=============================================================================
/**
 * tinySQLPreparedStatement
 *
 * Copyright 1996, Brian C. Jepson
 *                 (bjepson@ids.net)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */
package ORG.as220.tinySQL;

//Required Java's libraries;

import ORG.as220.tinySQL.sqlparser.ParameterValue;
import ORG.as220.tinySQL.sqlparser.ParserUtils;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Atráves do uso desta classe é possível executar com facilidade uma mesma
 * diretiva <code.SQL</code> diversas vezes, bastando para isto subistituir as
 * variáveis representadas pelo símbolo <b>?</b>, que é colocado pelo usuário
 * dentro do código <code.SQL</code> para ser utilizado como variável.<b>
 * Estas variáveis pode ter seus valores definidos utilizando os métodos
 * <code>setXXX( )</code> que são herdados da interface
 * <code>PreparedStatement</code>. Estes métodos utilizam como parâmetro sempre
 * o indice que representa a ordem da variável bind <code>SQL</code> ( <b>?</b>
 * ) dentro do comando que deverá ser enviado ao banco de dados e o valor
 * adequado que deve ser vínculado ao campo da tabela no banco de dados.
 * @author <a href='mailto:GuardianOfSteel@netscape.net'>Edson Alves Pereira</a> - 29/12/2001
 * @version $Revision: 1.1 $
 */
public class tinySQLPreparedStatement extends tinySQLStatement
    implements PreparedStatement
{
  private tinySQL sql;
  private Vector parameters;

  private int iParameterCount;

  //Constants:
  public static final String PARAM_NOT_FOUND = "SQL bind parameter not found.";

  //---------------------------------------------------------------------------
  /**
   * Create a new instance of this object.
   * @param con_ A connection to database.
   * @param stSQL_ A <code>SQL</code> statement that we'll create this object.
   * @param stCatalog_ A catalog to the database connection.
   */
  public tinySQLPreparedStatement(Connection con_,
                                  String stSQL_,
                                  String stCatalog_) throws tinySQLException
  {
    super((tinySQLConnection) con_);
    tinySQLConnection c = (tinySQLConnection) con_;
    sql = c.getDatabaseEngine();
    setStatement(sql.parse(this, stSQL_));
    parameters = getStatement().getParameters();

  }

  /**
   * Verify if thre're some empty parameter before send to the databse the
   * statement, in this case a exception will be raised showing to user that
   * it's an error.
   * @exception java.sql.SQLException If there're some empty parameters.
   */
  private void checkValuesFilled()
      throws SQLException
  {
    StringBuffer sbMsg = new StringBuffer();
    ParameterValue sp = null;

    /*I think that give all error messages that are possible once is better to
    improve the programmer work when he's trying to fix it.*/
    for (int i = 0; i < parameters.size(); i++)
    {
      //Casting the object to the correct one:
      sp = (ParameterValue) parameters.elementAt(i);

      /*If there're more that one parameter empty, we show all IDs for these
      parameters to the user known then:*/
      if (sp.isEmpty())
        sbMsg.append(
            "No value specified for parameter [ " + i + " ].\n");
    }

    //Now the exception in the case of errors:
    if (sbMsg.length() > 0)
      throw new SQLException(sbMsg.toString());
  }

  /**
   * Gets the number, types and properties of a ResultSet object's columns.
   *
   * @returns the description of a ResultSet object's columns
   * @throws SQLException if the query produces no resultsets
   */
  public ResultSetMetaData getMetaData()
      throws SQLException
  {
    boolean result = execute();
    if (result == true)
    {
      ResultSet res = getStatement().getResultSet();
      ResultSetMetaData rsmd = res.getMetaData();
      res.close();
      return rsmd;
    }
    else
      throw new tinySQLException("Statement did not create a result set");
  }

  /**
   * execute the query
   *
   * @returns true if a resultset was produced, false otherwise
   * @throws SQLException if a database error occured or not all parameter
   * were filled
   */
  public boolean execute()
      throws tinySQLException
  {
    // a result set object
    //
    boolean result;

    // execute the query
    //
    result = getStatement().execute();
    return result;
  }

  //---------------------------------------------------------------------------
  /**
   * Send to database a <code>SQL</code> statement to create a search only to
   * bring rows and don't change nothing on database.
   * @exception SQLException If occour an error with the database access or with
   * the <code>SQL</code> sintaxe.
   */
  public ResultSet executeQuery()
      throws SQLException
  {
    boolean result = execute();
    if (result == true)
      return getResultSet();
    else
      throw new tinySQLException("Statement returned an UpdateCount\n" + sql);
  }

  //---------------------------------------------------------------------------
  public int executeUpdate()
      throws SQLException
  {
    boolean result = execute();
    if (result == false)
      return getUpdateCount();
    else
      throw new tinySQLException("Statement returned a ResultSet");
  }

  //---------------------------------------------------------------------------
  public void addBatch()
      throws SQLException
  {
    /*The first step is to check if the programmer passed all the values
    required by the parameters that there're in the SQL statement:*/
    checkValuesFilled();

    //Storing this sql:
    super.addBatch(getStatement());
  }

  //---------------------------------------------------------------------------
  public void clearParameters()
      throws java.sql.SQLException
  {
    Enumeration enum = parameters.elements();

    //Very simples isn't:
    while (enum.hasMoreElements())
    {
      ParameterValue val = (ParameterValue) enum.nextElement();
      val.clear();
    }
  }

  //---------------------------------------------------------------------------
  public void setArray(int iIndex_, Array aValue_)
      throws SQLException
  {
    //I'm not sure about that:
    if (aValue_ == null)
    {
      setNull(iIndex_, java.sql.Types.ARRAY);
    }
    else
    {
      ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
      val.setValue(aValue_.getArray());
    }
  }

  //---------------------------------------------------------------------------
  // The iLength_ parameter is for optimize only, if it is to big,
  // the JDBC-System may choose to read the stream util EOF is reached.
  public void setAsciiStream(int iIndex_,
                             InputStream isValue_,
                             int iLength_)
      throws SQLException
  {
    if (isValue_ == null)
    {
      setNull(iIndex_, java.sql.Types.VARCHAR);
    }
    else
    {
      ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
      val.setValue(isValue_);
    }
  }

  //---------------------------------------------------------------------------
  public void setBigDecimal(int iIndex_, BigDecimal bdValue_)
      throws SQLException
  {
    if (bdValue_ == null)
      setNull(iIndex_, java.sql.Types.DECIMAL);
    else
    {
      ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
      val.setValue(bdValue_);
    }
  }

  //---------------------------------------------------------------------------
  public void setBinaryStream(int iIndex_,
                              InputStream isValue_,
                              int iLength_)
      throws SQLException
  {
    //Could be there's something more:
    setAsciiStream(iIndex_, isValue_, iLength_);
  }

  //---------------------------------------------------------------------------
  public void setBlob(int iIndex_, Blob blobValue_)
      throws SQLException
  {
    //Opps i did it again!
    setAsciiStream(iIndex_, blobValue_.getBinaryStream(), -1);
  }

  //---------------------------------------------------------------------------
  public void setBoolean(int iIndex_, boolean bValue_)
      throws SQLException
  {
    ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
    val.setValue(new Boolean(bValue_));
  }

  //---------------------------------------------------------------------------
  public void setByte(int iIndex_, byte bValue_)
      throws SQLException
  {
    ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
    val.setValue(new Byte(bValue_).toString());
  }

  //---------------------------------------------------------------------------
  public void setBytes(int iIndex_, byte bValue_[ ])
      throws SQLException
  {
    ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
    val.setValue(bValue_);
  }

  //---------------------------------------------------------------------------
  public void setCharacterStream(int iIndex_,
                                 Reader rValue_,
                                 int iLength_)
      throws SQLException
  {
    ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
    val.setValue(rValue_);
  }

  //---------------------------------------------------------------------------
  public void setClob(int iIndex_, Clob clobValue_)
      throws SQLException
  {
    setCharacterStream(iIndex_, clobValue_.getCharacterStream(), -1);
  }

  //---------------------------------------------------------------------------
  public void setDate(int iIndex_, java.sql.Date dtValue_)
      throws SQLException
  {
    setDate(iIndex_, dtValue_, null);
  }

  //---------------------------------------------------------------------------
  public void setDate(int iIndex_,
                      java.sql.Date dtValue_,
                      Calendar cal_)
      throws SQLException
  {
    if (dtValue_ == null)
      setNull(iIndex_, java.sql.Types.DATE);
    else
    {
      ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
      val.setValue(dtValue_);
    }
  }

  //---------------------------------------------------------------------------
  public void setDouble(int iIndex_, double dValue_)
      throws SQLException
  {
    ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
    val.setValue(new Double(dValue_));
  }

  //---------------------------------------------------------------------------
  public void setFloat(int iIndex_, float fValue_)
      throws SQLException
  {
    ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
    val.setValue(new Float(fValue_));
  }

  //---------------------------------------------------------------------------
  public void setInt(int iIndex_, int iValue_)
      throws SQLException
  {
    ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
    val.setValue(new Integer(iValue_));
  }

  //---------------------------------------------------------------------------
  public void setLong(int iIndex_, long lValue_)
      throws SQLException
  {
    ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
    val.setValue(new Long(lValue_));
  }

  //---------------------------------------------------------------------------
  public void setNull(int iIndex_, int iSqlType_)
      throws SQLException
  {
    ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
    val.setValue(null);
  }

  //---------------------------------------------------------------------------
  public void setNull(int iIndex_,
                      int iSqlType_,
                      String stTypeName_)
      throws SQLException
  {
    ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
    val.setValue(null);
  }

  //---------------------------------------------------------------------------
  public void setObject(int iIndex_, Object oValue_)
      throws SQLException
  {
    ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
    val.setValue(oValue_);
  }

  //---------------------------------------------------------------------------
  public void setObject(int iIndex_,
                        Object oValue_,
                        int iSqlType_)
      throws SQLException
  {
    setObject(iIndex_, oValue_, iSqlType_, 0);
  }

  //---------------------------------------------------------------------------
  public void setObject(int iIndex_,
                        Object oValue_,
                        int iSqlType_,
                        int iScale_)
      throws SQLException
  {
    if (oValue_ == null)
      setNull(iIndex_, java.sql.Types.OTHER);
    else
    {
      switch (iSqlType_)
      {
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
        case Types.BIGINT:
        case Types.REAL:
        case Types.FLOAT:
        case Types.DOUBLE:
        case Types.DECIMAL:
        case Types.NUMERIC:
          {
            setBigDecimal(iIndex_, ParserUtils.convertToNumber(oValue_));
            break;
          }
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
          {
            setString(iIndex_, ParserUtils.convertToString(oValue_));
            break;
          }
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
          {
            if (oValue_ instanceof String)
              setBytes(iIndex_, ((String) oValue_).getBytes());
            else
              setBytes(iIndex_, (byte[]) oValue_);
            break;
          }
        case Types.DATE:
          {
            setDate(iIndex_, ParserUtils.convertToDate(oValue_));
            break;
          }
        case Types.TIMESTAMP:
          {
            setTimestamp(iIndex_, ParserUtils.convertToTimestamp(oValue_));
            break;
          }
        case Types.TIME:
          {
            setTime(iIndex_, ParserUtils.convertToTime(oValue_));
            break;
          }
        case Types.OTHER:
          {
            ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
            val.setValue(oValue_);
            break;
          }

        default:
          throw new java.sql.SQLException("Unknown Types value", "S1000");
      }
    }
  }

  //---------------------------------------------------------------------------
  public void setRef(int iIndex_, Ref refValue_)
      throws SQLException
  {
    //I'm don't known if that's right:
    if (refValue_ == null)
      setNull(iIndex_, java.sql.Types.REF);
    else
    {
      ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
      val.setValue(refValue_.getBaseTypeName());
    }
  }

  //---------------------------------------------------------------------------
  public void setShort(int iIndex_, short sValue_)
      throws SQLException
  {
    ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
    val.setValue(new Short(sValue_));
  }

  //---------------------------------------------------------------------------
  public void setString(int iIndex_, String stValue_)
      throws SQLException
  {
    if (stValue_ == null)
      setNull(iIndex_, java.sql.Types.VARCHAR);
    else
    {
      ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
      val.setValue(stValue_);
    }
  }

  //---------------------------------------------------------------------------
  public void setTime(int iIndex_, Time tmValue_)
      throws SQLException
  {
    setTime(iIndex_, tmValue_, null);
  }

  //---------------------------------------------------------------------------
  public void setTime(int iIndex_,
                      Time tmValue_,
                      Calendar cal_)
      throws SQLException
  {
    if (tmValue_ == null)
      setNull(iIndex_, java.sql.Types.TIME);
    else
    {
      ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
      val.setValue(tmValue_);
    }
  }

  //---------------------------------------------------------------------------
  public void setTimestamp(int iIndex_, Timestamp tmsValue_)
      throws SQLException
  {
    setTimestamp(iIndex_, tmsValue_, null);
  }

  //---------------------------------------------------------------------------
  public void setTimestamp(int iIndex_,
                           Timestamp tmsValue_,
                           Calendar cal_)
      throws SQLException
  {
    if (tmsValue_ == null)
      setNull(iIndex_, java.sql.Types.TIMESTAMP);
    else
    {
      ParameterValue val = (ParameterValue) parameters.elementAt(iIndex_ - 1);
      val.setValue(tmsValue_);
    }
  }

  //---------------------------------------------------------------------------
  public void setUnicodeStream(int iIndex_,
                               InputStream isValue_,
                               int iLength_)
      throws java.sql.SQLException
  {
    if (isValue_ == null)
      setNull(iIndex_, java.sql.Types.VARCHAR);
    else
      setBinaryStream(iIndex_, isValue_, iLength_);
  }
  //---------------------------------------------------------------------------
  /////////////////////////////////////////////////////////////////////////////
  //---------------------------------------------------------------------------
}

//=============================================================================

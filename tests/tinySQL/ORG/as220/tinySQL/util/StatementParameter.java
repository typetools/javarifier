//=============================================================================
/*
$Id: StatementParameter.java,v 1.1 2008-05-25 04:58:11 jaimeq Exp $
*/
//=============================================================================
package ORG.as220.tinySQL.util;

//Required Java's libraries;

import java.io.InputStream;

/**
 * This objet store the value and informations about a parameter that will be
 * part of a <code>SQL</code> statement and passed to be executed by database.
 * @author <a href='mailto:GuardianOfSteel@netscape.net'>Edson Alves Pereira</a> - 29/12/2001
 * @version $Revision: 1.1 $
 */
public class StatementParameter
{
  /**
   * Guard the identificationof this parameter.
   */
  private int iId;

  /**
   * The value of this parameter stored inside a stream.
   */
  private InputStream isValue;

  /**
   * A value that can be easy manipulated.by the statement and the database.
   */
  private String stValue;

  /**
   * Inform if the value is a stream.
   */
  private boolean bIsStream;

  /**
   * Indicates where in the <code>SQL</code> statement this parameters is, this
   * safe time searching again.
   */
  private ParameterPosition ppPosition;

  /**
   * Default constructor, build a new instance of this object.
   */
  public StatementParameter()
  {
    init();
  }

  /**
   * Required inicializations for this object.
   */
  protected void init()
  {
    iId = 0;

    bIsStream = false;

    ppPosition = null;
    isValue = null;
    stValue = null;
  }

  /**
   * Define an identification for this parameter, as the parameters is
   * identifyed by a number we need this to known whitch it's. This numbar must
   * be lounder that 0.
   * @param iId_ An number to identify the parameter.
   */
  public void setId(int iId_)
  {
    if (iId_ > 0)
      iId = iId_;
  }

  /**
   * Show the identification of this parameter.
   * @return The number that is the order of this parameter to a
   * <code.SQL</code> statement.
   */
  public int getId()
  {
    return iId;
  }

  /**
   * Define a value that we can use to store inside a stream.
   * @param isValue_ The value that could be any kind of stream supported by
   * database.
   */
  public void setStreamValue(InputStream isValue_)
  {
    if (isValue_ != null)
    {
      isValue = isValue_;
      bIsStream = true;
    }
  }

  /**
   * Gives the value stored in this object that will be put togheter with a
   * <code>SQL</code> statement.
   * @return A stream to be passed to the database.
   */
  public InputStream getStreamValue()
  {
    return isValue;
  }

  /**
   * If the value is simple you can use this method to store in until pass to
   * the database.
   * @param stValue_ A value that will be passed to database.
   */
  public void setValue(String stValue_)
  {
    if (stValue_ != null)
    {
      stValue = stValue_;
      bIsStream = false;
    }
  }

  /**
   * Bring a simple value that is inside this parameter.
   * @return The value of this parameter.
   */
  public String getValue()
  {
    return stValue;
  }

  /**
   * Informs if the value contained in this parameter should be triet as a
   * stream.
   * @return True if the value is a stream and false if not.
   */
  public boolean isStream()
  {
    return bIsStream;
  }

  /**
   * Define where in the <code>SQL</code> statement this parameters is located.
   * @param ppPosition_ The place where this parameter is.
   */
  public void setPosition(ParameterPosition ppPosition_)
  {
    if (ppPosition_ != null)
      ppPosition = ppPosition_;
  }

  /**
   * Notify where is location of this parameter inside the <code>SQL</code>
   * statement.
   * @return The location of this parameter.
   */
  public ParameterPosition getPosition()
  {
    return ppPosition;
  }

  /**
   * Informs if there is some value stored in this parameters.
   * @return True if there's a value inside this object or false otherwise.
   */
  public boolean isEmpty()
  {
    return (getValue() != null || getStreamValue() != null) ? false : true;
  }

  /**
   * Clear the value inside this parameter.
   */
  public void clear()
  {
    if (!isEmpty())
    {
      if (isStream())
        isValue = null;
      else
        stValue = null;
    }
  }
}

//=============================================================================
//=============================================================================
/*
$Id: ParameterPosition.java,v 1.1 2008-05-25 04:58:10 jaimeq Exp $
*/
//=============================================================================
package ORG.as220.tinySQL.util;

/**
 * Here you can specify where the <code>PreparedStastement</code> should put a
 * parametr's value inside a <code.SQL</code> text, this place is marked by the
 * start of the string until reach the parameter see the example bellow;<br>
 * <p><code>String stSQL = "select NOME, STREET from PERSON where ID =
 * ?";</code><br>
 * The parameter is located in the 43th position, so you must inicialize this
 * objet as;<br>
 * <p><code>ParameterPosition pp = new ParameterPosition( 0, 43 );</code><br>
 * We need do this because <code>tinySQLPreparedStastement</code> use this
 * information to put the value stored inside <code>StatementParameter</code>
 * after the end of the position with this,
 * <code>tinySQLPreparedStatement</code> can cut a mount of <code>SQL</code>
 * before the <b>?</b> and put value. When the whole process is finished we have
 * the complete <code>SQL</code> statement ready to the database work.
 * @author <a href='mailto:GuardianOfSteel@netscape.net'>Edson Alves Pereira</a> - 29/12/2001
 * @version $Revision: 1.1 $
 */
public class ParameterPosition
{
  private int iStart;
  private int iEnd;

  public ParameterPosition()
  {
    iStart = 0;
    iEnd = 0;
  }

  public ParameterPosition(int iStart_,
                           int iEnd_)
  {
    setStart(iStart_);
    setEnd(iEnd_);
  }

  public void setStart(int iStart_)
  {
    if (iStart_ >= 0)
      iStart = iStart_;
  }

  public int getStart()
  {
    return iStart;
  }

  public void setEnd(int iEnd_)
  {
    if (iEnd_ >= 0)
      iEnd = iEnd_;
  }

  public int getEnd()
  {
    return iEnd;
  }
}

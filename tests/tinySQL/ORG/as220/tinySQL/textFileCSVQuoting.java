package ORG.as220.tinySQL;

public class textFileCSVQuoting extends textFileQuoting
{
  private String separator;

  public textFileCSVQuoting()
  {
  }

  public void init()
  {
    separator = getDatabase().getProperties().getProperty("quoting.csv.separator", ",");
  }

  public String doQuoting(String jdbcString, int collength)
      throws tinySQLException
  {
    if (jdbcString.length() > collength)
      throw new tinySQLException("Quoting limit exceeded for native string");

    boolean quoting = isQuotingNeeded(jdbcString);
    if (quoting)
    {
      StringBuffer retval = new StringBuffer();
      retval.append("\"");
      applyQuote(retval, jdbcString);
      retval.append("\"");
      if (retval.length() > collength)
      {
        throw new tinySQLException("Quoting limit exceeded for quoted string");
      }
      return retval.toString();
    }
    else
      return jdbcString;
  }

  public String undoQuoting(String nativeString)
      throws tinySQLException
  {
    boolean quoting = isQuotingNeeded(nativeString);
    if (quoting)
    {
      StringBuffer b = new StringBuffer(nativeString.length());
      int length = nativeString.length() - 1;
      int start = 1;

      int pos = start;
      while (pos != -1)
      {
        pos = nativeString.indexOf('\\', start);
        if (pos == -1)
        {
          b.append(nativeString.substring(start, length));
        }
        else
        {
          b.append(nativeString.substring(start, pos));
          if ((pos + 1) == length)
          {
            // End of the string reached, no more searching needed.
            pos = -1;
          }
          else
          {
            char nextChar = nativeString.charAt(pos + 1);
            b.append(nextChar);
            start = pos + 2;
          }
        }
      }
      return b.toString();
    }
    else
      return nativeString;
  }

  private boolean isQuotingNeeded(String str)
  {
    if (str.indexOf(separator) != -1)
      return true;

    if (str.indexOf('\n') != -1)
      return true;

    if (str.indexOf('\"', 1) != -1)
      return true;

    return false;
  }

  private void applyQuote(StringBuffer b, String jdbcString)
  {
    int length = jdbcString.length();

    for (int i = 0; i < length; i++)
    {
      char c = jdbcString.charAt(i);
      if (c == '"')
      {
        b.append("\\\"");
      }
      else if (c == '\\')
      {
        b.append("\\\\");
      }
      else
        b.append(c);
    }
  }

  public static void main(String[] args) throws Exception
  {
    textFileCSVQuoting quoting = new textFileCSVQuoting();
    quoting.separator = ",";

    quoting.doTest("\"Scary, isn't it?\", she said.");
    quoting.doTest("Not really!");
    quoting.doTest("It won't work, but who cares?");
    quoting.doTest("Not really! \" yo\" ");

  }

  private void doTest(String test) throws Exception
  {

    String quoted = doQuoting(test, 100);
    String back = undoQuoting(quoted);

    System.out.println("Test : " + test);
    System.out.println("Quot : " + quoted);
    System.out.println("UnQu : " + back);
  }
}

package ORG.as220.tinySQL;

import ORG.as220.tinySQL.util.CharacterEntityParser;

public class textFileHTMLQuoting extends textFileQuoting
{
  public textFileHTMLQuoting()
  {
  }

  public String doQuoting(String jdbcString, int collength) throws tinySQLException
  {
    if (jdbcString.length() > collength)
      throw new tinySQLException("Quoting limit exceeded for native string");

    String retval = CharacterEntityParser.quoteString(jdbcString);
    if (retval.length() > collength)
      throw new tinySQLException("Quoting limit exceeded for quoted string");

    return retval;
  }

  public String undoQuoting(String nativeString)
  {
    String retval = CharacterEntityParser.parseEntityValue(nativeString);
    return retval;
  }

  public static void main(String[] args) throws Exception
  {
    java.sql.DriverManager.setLogStream(System.out);
    textFileHTMLQuoting quoting = new textFileHTMLQuoting();

    quoting.doTest("\"Scary, isn't it?\", she said.");
    quoting.doTest("Not really!");
    quoting.doTest("It won't work, but who cares?");
    quoting.doTest("Not really! \" yo\" ");
    quoting.doTest("H‰gar Boﬂ");

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

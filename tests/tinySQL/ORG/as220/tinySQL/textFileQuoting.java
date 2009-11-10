package ORG.as220.tinySQL;


public class textFileQuoting
{
  private textFile db;

  public textFileQuoting()
  {
  }

  public void setDatabase(textFile tf)
  {
    this.db = tf;
  }

  public textFile getDatabase()
  {
    return db;
  }

  public void init()
  {
  }

  public String doQuoting(String jdbcString, int collength)
      throws tinySQLException
  {
    if (jdbcString.length() > collength)
      throw new tinySQLException("Quoting limit exceeded");

    return jdbcString;
  }

  public String undoQuoting(String nativeString)
      throws tinySQLException
  {
    return nativeString;
  }
}

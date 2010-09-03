package ORG.as220.tinySQL;

/**
 * An empty default implementation of an simple ordering. This implementation
 * does no translation and returned the physical order of the table.
 */
public class tinySQLDefaultIndex implements tinySQLIndex
{
  private tinySQLTableView table;

  public int getRowCount() throws tinySQLException
  {
    return table.getRowCount();
  }

  // navigation-functions as defined by result set but returning
  // the rownumber of the physical table.
  //
  // Translates virtual positions into physical row numbers
  public int translateRow(int row) throws tinySQLException
  {
    return row;
  }

  public int reverseTranslation(int row) throws tinySQLException
  {
    return row;
  }


}

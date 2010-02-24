package ORG.as220.tinySQL;

/**
 * An interface to iterate over a ordered table.
 */
public interface tinySQLIndex
{
  public int getRowCount() throws tinySQLException;

  // navigation-functions as defined by result set but returning
  // the rownumber of the physical table.
  public int translateRow(int row) throws tinySQLException;

  public int reverseTranslation(int row) throws tinySQLException;
}

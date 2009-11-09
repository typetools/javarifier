package ORG.as220.tinySQL;

public interface tinySQLLookupIndex extends tinySQLIndex
{
  /**
   * Lookup the primary key. This is always supported.
   * if getKeyCount returns 0, the absolute rownumber is
   * used as primary key.
   */
  public int lookup(Object key);

  /**
   * Lookup a row with multiple key columns.
   * Needed for Order-by column, column2, ...
   */
  public int lookup(Object[] keys);

  /**
   * How many keys are in this index
   */
  public int getKeyCount();
}

package ORG.as220.tinySQL;

import java.lang.ref.WeakReference;
import java.util.Vector;

public class tsWeakPhysicalRow extends tsPhysicalRow
{
  private tinySQLTableView[] views;
  private int[] viewPositions;

  public void put(int col, Object data) throws tinySQLException
  {
    if (data == null)
      super.put(col, null);
    else
      super.put(col, new WeakReference(data));
  }

  public Object get(int col) throws tinySQLException
  {
    WeakReference ref = (WeakReference) super.get(col);
    if (ref == null)
      return null;

    Object o = ref.get();
    if (o == null)
    {
      // we have been clearedm reload
      return refresh(col);
    }
    else
      return o;
  }

  protected Object refresh(int col) throws tinySQLException
  {
    tsColumn coldef = getColumnDefinition(col);
    tinySQLTableView table = coldef.getTable();
    Object o = table.getColumn(coldef.getTablePosition());
    put(col, o);
    return o;
  }

  public tsWeakPhysicalRow(tsPhysicalRow copycon)
  {
    super(copycon);
  }

  public tsWeakPhysicalRow(Vector rows)
  {
    super(rows);
  }

  public void setRowPositions(Vector tsTableViews) throws tinySQLException
  {
    views = (tinySQLTableView[]) tsTableViews.toArray(new tinySQLTableView[tsTableViews.size()]);
    for (int i = 0; i < views.length; i++)
    {
      viewPositions[i] = views[i].getCurrentRecordNumber();
    }
  }

  public void refresh() throws tinySQLException
  {
    for (int i = 0; i < views.length; i++)
    {
      views[i].absolute(viewPositions[i]);
    }
  }
}

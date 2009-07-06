/*
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
 *
 */
package ORG.as220.tinySQL.sqlparser;

/**
 * A function to operate on the current row.
 * This is currently not implemented. Implementation needs some factory,
 * as the parser has not to know about every function implemented.
 */
public interface Function extends LValue
{
  public String getName();

  public void setParameter(int pos, LValue par);

  public LValue getParameter(int pos);
}

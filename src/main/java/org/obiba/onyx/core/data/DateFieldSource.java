/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import java.util.Calendar;
import java.util.Date;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * class used to extract a special field (YEAR, MONTH, DAY, HOUR, etc. as specified by Calendar) from date provided by
 * any source
 */
public class DateFieldSource extends AbstractDataSourceDataModifier {

  private static final long serialVersionUID = 1L;

  private DateField field;

  /**
   * Constructor, given a field.
   * @param dataSource
   * @param field
   */
  public DateFieldSource(IDataSource iDataSource, int field) {
    this(iDataSource, DateField.fromField(field));
  }

  public DateFieldSource(IDataSource iDataSource, DateField field) {
    super(iDataSource);
    this.field = field;
  }

  @Override
  protected Data modify(Data data, Participant participant) {
    if(data == null) return null;
    if(!data.getType().equals(DataType.DATE)) throw new IllegalArgumentException("DataType " + DataType.DATE + " expected, " + data.getType() + " received.");

    Date date = data.getValue();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    return (DataBuilder.build(cal.get(getField().toCalendarField())));
  }

  public DateField getField() {
    return field;
  }

  public void setField(DateField field) {
    this.field = field;
  }

  @Override
  public String toString() {
    return "DateField[" + super.toString() + "]." + field;
  }

}

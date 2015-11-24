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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;

/**
 * Get the current date, or one of its field, optionally having modified it.
 */
public class CurrentDateSource implements IDataSource {

  private static final long serialVersionUID = 1L;

  private DateField field;

  private List<DateModifier> dateModifiers;

  /**
   * returns {@link Data} containing the current complete date or a specified field of the current date
   * @param participant
   * @return
   */
  public Data getData(Participant participant) {
    // Returns the current time in the current timezone
    Calendar cal = Calendar.getInstance();

    for(DateModifier dateModifier : getDateModifiers()) {
      dateModifier.modify(cal, participant);
    }

    return (field == null) ? DataBuilder.buildDate(cal.getTime()) : DataBuilder.buildInteger(cal.get(field.toCalendarField()));
  }

  public String getUnit() {
    return null;
  }

  // Constructors
  public CurrentDateSource() {
    this.field = null;
  }

  public CurrentDateSource(int field) {
    this.field = DateField.fromField(field);
  }

  public CurrentDateSource(DateField field) {
    this.field = field;
  }

  public CurrentDateSource(List<DateModifier> dateModifiers) {
    this.field = null;
    this.dateModifiers = dateModifiers;
  }

  public CurrentDateSource(int field, List<DateModifier> dateModifiers) {
    this.field = DateField.fromField(field);
    this.dateModifiers = dateModifiers;
  }

  public CurrentDateSource(DateField field, List<DateModifier> dateModifiers) {
    this.field = field;
    this.dateModifiers = dateModifiers;
  }

  public List<DateModifier> getDateModifiers() {
    return (dateModifiers != null) ? dateModifiers : (dateModifiers = new ArrayList<DateModifier>());
  }

  public CurrentDateSource addDateModifier(DateModifier dateModifier) {
    if(dateModifier != null) {
      getDateModifiers().add(dateModifier);
    }
    return this;
  }

  public DateField getField() {
    return field;
  }

  @Override
  public String toString() {
    String rval = "CurrentDate";
    if(getDateModifiers().size() > 0) {
      rval += getDateModifiers();
    }
    if(field != null) {
      rval += "." + field;
    }
    return rval;
  }
}

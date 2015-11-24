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

import org.obiba.onyx.core.domain.participant.Participant;

/**
 * 
 */
public class ParticipantPropertyDataSource extends AbstractBeanPropertyDataSource {

  private static final long serialVersionUID = 1L;

  /*
   * Returns participant as bean
   */
  @Override
  public Object getBean(Participant participant) {
    return participant;
  }

  public ParticipantPropertyDataSource(String property) {
    super(property);
  }

  public ParticipantPropertyDataSource(String property, String unit) {
    super(property);
    setUnit(unit);
  }

  @Override
  public String toString() {
    return "ParticipantProperty[" + super.toString() + "]";
  }
}

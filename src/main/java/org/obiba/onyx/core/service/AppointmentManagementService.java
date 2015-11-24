/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service;

import java.util.Date;
import java.util.List;

import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateStats;
import org.springframework.batch.core.ExitStatus;

/**
 * Interface that manages the process of updating the appointment list
 */
public interface AppointmentManagementService {

  /**
   * executes the process of updating the appointments
   */
  public ExitStatus updateAppointments();

  /**
   * Save the AppointmentUpdateStats
   * @param appointmentUpdateStats
   */
  public void saveAppointmentUpdateStats(AppointmentUpdateStats appointmentUpdateStats);

  /**
   * Get the last AppointmentUpdateStats
   * @return AppointmentUpdateStats
   */
  public AppointmentUpdateStats getLastAppointmentUpdateStats();

  /**
   * Get the AppointmentUpdate JobExecution corresponding to the specified date
   * @return Job
   */
  public List<AppointmentUpdateLog> getLogListForDate(Date date);

}

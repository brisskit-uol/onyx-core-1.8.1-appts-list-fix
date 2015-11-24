/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant.impl;

import java.util.Date;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateStats;
import org.obiba.onyx.core.service.AppointmentManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterProcess;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.annotation.OnReadError;

/**
 * Listener responsible for tracking the different events that happen during the appointment list update process
 */
public class AppointmentListUpdateListener {

  private static final Logger log = LoggerFactory.getLogger(AppointmentListUpdateListener.class);

  private AppointmentManagementService appointmentManagementService;

  private int addedParticipants;

  private int updatedParticipants;

  private int ignoredParticipants;

  private int unreadableParticipants;

  public AppointmentListUpdateListener() {
    super();
    if(log.isDebugEnabled()) {
      log.debug("within:AppointmentListUpdateListener()");
    }
    initListener();
  }

  /**
   * Increments the unreadableParticipants counter whenever the ParticipantReader throws an exception.
   * @throws Exception
   */
  @OnReadError
  public void onParticipantReadError() throws Exception {
    addUnreadableParticipant();
  }

  /**
   * Called before the step of the AppointmentUpdateList job. Clears the previous participantProcessor data (needed when
   * several lists are submitted)
   * @param stepExecution
   * @return
   */
  @BeforeStep
  public void beforeStep(StepExecution stepExecution) {
    ParticipantProcessor.initProcessor();
    initListener();
  }

  /**
   * Called on the return of the ParticipantProcessor. Increments: 1) the ignoredParticipants counter when the result is
   * null, 2) the addedParticipants counter when result is not null and item.getId() is null, 3) the updatedParticipants
   * counter when result and item.getId() are not null
   * @param item
   * @param result
   */
  @AfterProcess
  public void afterParticipantProcessed(Participant item, Participant result) {
    if(item != null) {
      if(result == null) {
        addIgnoredParticipant();
      } else if(item.getId() != null) {
        addUpdatedParticipant();
      } else {
        addAddedParticipant();
      }
    }
  }

  /**
   * Called after the step of the AppointmentUpdateList job. Persists the job resuming data.
   * @param stepExecution
   * @return
   */
  @AfterStep
  public ExitStatus afterUpdateCompleted(StepExecution stepExecution) {
    ExitStatus status = stepExecution.getExitStatus();

    if(status.getExitCode().equals("COMPLETED")) {
      AppointmentUpdateStats appointmentUpdateStats = new AppointmentUpdateStats(stepExecution.getJobParameters().getDate("date"), getAddedParticipants(), getUpdatedParticipants(), getIgnoredParticipants(), getUnreadableParticipants());
      appointmentUpdateStats.setFileName(stepExecution.getExecutionContext().get("fileName").toString());
      appointmentManagementService.saveAppointmentUpdateStats(appointmentUpdateStats);
    }

    AppointmentUpdateLog.addErrorLog(stepExecution.getExecutionContext(), new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.INFO, "End updating appointments"));

    return status;
  }

  private void initListener() {
    if(log.isDebugEnabled()) {
      log.debug("e:AppointmentListUpdateListener.initListener()");
    }
    setAddedParticipants(0);
    setUpdatedParticipants(0);
    setIgnoredParticipants(0);
    setUnreadableParticipants(0);
    if(log.isDebugEnabled()) {
      log.debug("x:AppointmentListUpdateListener.initListener()");
    }
  }

  public int getAddedParticipants() {
    return addedParticipants;
  }

  public int getUpdatedParticipants() {
    return updatedParticipants;
  }

  public int getIgnoredParticipants() {
    return ignoredParticipants;
  }

  public int getUnreadableParticipants() {
    return unreadableParticipants;
  }

  public void setAddedParticipants(int addedParticipants) {
    this.addedParticipants = addedParticipants;
  }

  public void setUpdatedParticipants(int updatedParticipants) {
    this.updatedParticipants = updatedParticipants;
  }

  public void setIgnoredParticipants(int ignoredParticipants) {
    this.ignoredParticipants = ignoredParticipants;
  }

  public void setUnreadableParticipants(int unreadableParticipants) {
    this.unreadableParticipants = unreadableParticipants;
  }

  public void addAddedParticipant() {
    this.addedParticipants++;
  }

  public void addIgnoredParticipant() {
    this.ignoredParticipants++;
  }

  public void addUpdatedParticipant() {
    this.updatedParticipants++;
  }

  public void addUnreadableParticipant() {
    this.unreadableParticipants++;
  }

  public void setAppointmentManagementService(AppointmentManagementService appointmentManagementService) {
    this.appointmentManagementService = appointmentManagementService;
  }
}

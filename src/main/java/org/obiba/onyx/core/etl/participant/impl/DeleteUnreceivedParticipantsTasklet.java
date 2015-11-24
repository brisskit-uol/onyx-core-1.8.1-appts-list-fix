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

import java.io.File;
import java.io.IOException;

import org.obiba.onyx.core.service.ParticipantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Spring Batch Tasklet called in DeleteUnreceivedParticipantsStep to delete the participants without interview
 */
public class DeleteUnreceivedParticipantsTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(DeleteUnreceivedParticipantsTasklet.class);

  private ParticipantService participantService;

  private AbstractParticipantReader participantReader;

  public RepeatStatus execute(StepContribution stepContribution, ChunkContext context) throws Exception {
    if(log.isDebugEnabled()) {
      log.debug("e:DeleteUnreceivedParticipantsTasklet.execute() ");
    }

    try {
      if(isUpdateAvailable()) {
        log.debug("About to invoke cleanUpAppointment()");
        participantService.cleanUpAppointment();
        log.debug("About to set exit status: UPDATE");
        stepContribution.setExitStatus(new ExitStatus("UPDATE"));
      } else {
        log.debug("About to set exit status: NO UPDATE");
        stepContribution.setExitStatus(new ExitStatus("NO UPDATE"));
      }
      log.debug("About to return repeat status: FINISHED");
      return RepeatStatus.FINISHED;

    } finally {

      if(log.isDebugEnabled()) {
        log.debug("x:DeleteUnreceivedParticipantsTasklet.execute() ");
      }

    }
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setParticipantReader(AbstractParticipantReader participantReader) {
    this.participantReader = participantReader;
  }

  private boolean isUpdateAvailable() throws IOException {
    if(log.isDebugEnabled()) {
      log.debug("e:DeleteUnreceivedParticipantsTasklet.isUpdateAvailable() ");
    }
    boolean retValue = false;
    try {
      // Resource resource = participantReader.getInputDirectory();
      // File inputDirectory = resource.getFile();
      File inputDirectory = participantReader.getBrisskitInputDirectory();
      log.debug("inputDirectory: " + inputDirectory.getAbsolutePath());
      File[] files = inputDirectory.listFiles(participantReader.getFilter());
      log.debug("found " + files.length + " files.");
      for(int i = 0; i < files.length; i++) {
        log.debug("file " + i + ": " + files[i].getName());
      }
      if(files.length > 0) {
        retValue = true;
      }
      log.debug("returning " + retValue);
      return retValue;
    } finally {
      if(log.isDebugEnabled()) {
        log.debug("x:DeleteUnreceivedParticipantsTasklet.isUpdateAvailable() ");
      }
    }
    // return (participantReader.getInputDirectory().getFile().listFiles(participantReader.getFilter()).length > 0);
  }
}

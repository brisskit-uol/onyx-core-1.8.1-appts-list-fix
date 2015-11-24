/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.stage.StageExecutionMemento;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.LoggingTransitionListener;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultActiveInterviewServiceImpl extends PersistenceManagerAwareService implements ActiveInterviewService {

  private static final Logger log = LoggerFactory.getLogger(DefaultActiveInterviewServiceImpl.class);

  private InterviewManager interviewManager;

  private UserSessionService userSessionService;

  private ModuleRegistry moduleRegistry;

  @SuppressWarnings("unused")
  // Bean provides clean up by releasing the interview destroyed.
  private ActiveInterviewReleaseBean activeInterviewReleaseBean;

  public void setInterviewManager(InterviewManager interviewManager) {
    this.interviewManager = interviewManager;
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

  public void setModuleRegistry(ModuleRegistry moduleRegistry) {
    this.moduleRegistry = moduleRegistry;
  }

  public void setActiveInterviewReleaseBean(ActiveInterviewReleaseBean activeInterviewReleaseBean) {
    this.activeInterviewReleaseBean = activeInterviewReleaseBean;
  }

  public Participant getParticipant() {
    return interviewManager.getInterviewedParticipant();
  }

  public Interview getInterview() {
    return getParticipant().getInterview();
  }

  public User getOperator() {
    return userSessionService.getUser();
  }

  public IStageExecution getStageExecution(Stage stage) {
    Participant currentParticipant = getParticipant();
    if(currentParticipant == null) return null;

    // try to find it in memory
    StageExecutionContext exec = retrieveStageExecutionContext(currentParticipant, stage);

    if(exec == null) {
      Module module = moduleRegistry.getModule(stage.getModule());
      exec = (StageExecutionContext) module.createStageExecution(currentParticipant.getInterview(), stage);

      // Add a transition listener to log (persist) the stage transitions.
      LoggingTransitionListener transitionListener = new LoggingTransitionListener();
      transitionListener.setActiveInterviewService(this);
      exec.addTransitionListener(transitionListener);

      for(StageExecutionContext sec : getStageExecutionContexts(currentParticipant)) {
        if(exec.getStage().getStageDependencyCondition() != null) {
          if(exec.getStage().getStageDependencyCondition().isDependentOn(stage, sec.getStage().getName())) {
            sec.addTransitionListener(exec);
          }
        }
      }

      storeStageExecutionContext(currentParticipant, exec);

      // try to find previous state in memento
      StageExecutionMemento template = new StageExecutionMemento();
      template.setStage(stage.getName());
      template.setInterview(getInterview());
      StageExecutionMemento memento = getPersistenceManager().matchOne(template);
      if(memento != null) {
        exec.restoreFromMemento(memento);
      }
    }

    return exec;
  }

  public IStageExecution getStageExecution(String stageName) {
    Stage stage = moduleRegistry.getStage(stageName);
    if(stage == null) {
      log.warn("No stage with name '{}' is registered.", stageName);
      return null;
    }
    return getStageExecution(stage);
  }

  public Stage getInteractiveStage() {
    // Return the first stage that is interactive
    // Does not check that only one stage is interactive.
    for(Stage stage : moduleRegistry.listStages()) {
      // It's important to call getSageExecution here since the method will create it if it does not exist.
      IStageExecution stageExecution = getStageExecution(stage);
      if(stageExecution.isInteractive()) {
        return stage;
      }
    }
    return null;
  }

  public void doAction(Stage stage, Action action) {
    if(action == null) throw new IllegalArgumentException("The [action] parameter must not be null.");
    if(action.getActionType() == null) throw new IllegalArgumentException("The [type] member of the [action] parameter must not be null.");

    if(action.getActionDefinitionCode() == null) {
      action.setActionDefinitionCode("action." + action.getActionType());
    }

    action.setInterview(getParticipant().getInterview());
    if(stage != null) {
      action.setStage(stage.getName());
    }
    action.setDateTime(new Date());
    action.setUser(userSessionService.getUser());
    getPersistenceManager().save(action);

    if(stage != null) {
      IStageExecution exec = getStageExecution(stage);
      action.getActionType().act(exec, action);
    }
  }

  public Action getCurrentAction() {
    Action template = new Action();
    template.setInterview(getInterview());
    List<Action> matches = persistenceManager.match(template, new SortingClause("dateTime", false));

    return !matches.isEmpty() ? matches.get(0) : null;
  }

  public void updateAction(Action action) {
    getPersistenceManager().save(action);
  }

  public void setStatus(InterviewStatus status) {
    Interview interview = getInterview();
    if(interview != null) {
      interview.setStatus(status);
      if(status.equals(InterviewStatus.CANCELLED) || status.equals(InterviewStatus.COMPLETED) || status.equals(InterviewStatus.CLOSED)) {
        interview.setEndDate(new Date());
      }
      getPersistenceManager().save(interview);
    }
  }

  public List<Action> getInterviewComments() {
    List<Action> comments = new ArrayList<Action>();

    for(Action action : getInterviewActions()) {
      if(action.getComment() != null) {
        comments.add(action);
      }
    }

    return comments;
  }

  public List<Action> getInterviewComments(String stageName) {
    if(stageName == null) throw new IllegalArgumentException("The argument stageName cannot be null.");

    List<Action> stageComments = new ArrayList<Action>();

    Action template = new Action();
    template.setInterview(getInterview());
    template.setStage(stageName);

    List<Action> matches = getPersistenceManager().match(template, new SortingClause("dateTime", false));
    for(Action action : matches) {
      if(action.getComment() != null) {
        stageComments.add(action);
      }
    }

    return stageComments;
  }

  public List<Action> getInterviewActions() {
    Action template = new Action();
    template.setInterview(getInterview());
    List<Action> actions = getPersistenceManager().match(template, new SortingClause("dateTime", false));

    return actions;
  }

  public List<Action> getInterviewActions(String stageName) {
    if(stageName == null) throw new IllegalArgumentException("The argument stageName cannot be null.");
    List<Action> actions = new ArrayList<Action>();

    for(Action action : getInterviewActions()) {
      if(stageName.equals(action.getStage())) {
        actions.add(action);
      }
    }

    return actions;
  }

  @SuppressWarnings("incomplete-switch")
  public Action getStatusAction() {
    Interview interview = getInterview();
    Action template = new Action();
    template.setInterview(interview);
    switch(interview.getStatus()) {
    case CANCELLED:
      template.setActionType(ActionType.STOP);
      break;
    case COMPLETED:
      template.setActionType(ActionType.COMPLETE);
      break;
    }

    List<Action> actions = getPersistenceManager().match(template, new SortingClause("dateTime", false));

    if(actions.size() > 0) return actions.get(0);
    return null;
  }

  public void reinstateInterview() {
    InterviewStatus newStatus = getReinstatedStatus();
    if(newStatus == null) {
      throw new RuntimeException("cannot reinstate an interview that is neither completed nor cancelled");
    }

    setStatus(newStatus);
  }

  public void storeStageExecutionContext(Participant participant, StageExecutionContext exec) {
    getStageContexts().put(exec.getStage().getName(), exec);
  }

  public StageExecutionContext retrieveStageExecutionContext(Participant participant, Stage stage) {
    return getStageContexts().get(stage.getName());
  }

  public Collection<StageExecutionContext> getStageExecutionContexts(Participant participant) {
    return Collections.unmodifiableCollection(getStageContexts().values());
  }

  private Map<String, StageExecutionContext> getStageContexts() {
    return interviewManager.getStageContexts();
  }

  private InterviewStatus getReinstatedStatus() {
    InterviewStatus reinstatedStatus = null;

    InterviewStatus currentStatus = getInterview().getStatus();

    if(currentStatus == InterviewStatus.CLOSED) {
      reinstatedStatus = InterviewStatus.IN_PROGRESS;
    } else if(currentStatus == InterviewStatus.CANCELLED) {
      reinstatedStatus = interviewHadBeenCompleted() ? InterviewStatus.COMPLETED : InterviewStatus.IN_PROGRESS;
    }

    return reinstatedStatus;
  }

  private boolean interviewHadBeenCompleted() {
    for(StageExecutionContext sec : getStageContexts().values()) {
      if(sec.isFinal()) return true;
    }
    return false;
  }
}

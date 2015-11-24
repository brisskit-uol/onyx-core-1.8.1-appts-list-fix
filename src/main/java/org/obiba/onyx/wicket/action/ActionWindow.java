/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.action;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class ActionWindow extends Dialog {

  private static final Logger log = LoggerFactory.getLogger(ActionWindow.class);

  private static final int DEFAULT_INITIAL_HEIGHT = 33;

  private static final int DEFAULT_INITIAL_WIDTH = 32;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  private WindowClosedCallback additionnalWindowClosedCallback;

  private ActionDefinitionPanel content;

  public ActionWindow(String id) {
    super(id);

    setOptions(Dialog.Option.OK_CANCEL_OPTION, "Continue");
    setHeightUnit("em");
    setWidthUnit("em");
    setInitialHeight(DEFAULT_INITIAL_HEIGHT);
    setInitialWidth(DEFAULT_INITIAL_WIDTH);
    setType(Type.PLAIN);

    setCloseButtonCallback(new CloseButtonCallback() {
      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
        ActionDefinitionPanel pane = ActionWindow.this.getWindowContent();

        if(status != null && status.equals(Dialog.Status.ERROR)) {
          FeedbackWindow feedback = pane.getFeedback();
          feedback.setContent(new FeedbackPanel("content"));
          feedback.show(target);
          return false;
        }

        return true;
      }
    });

    setWindowClosedCallback(new WindowClosedCallback() {
      public void onClose(AjaxRequestTarget target, Status status) {
        if(status != null && !status.equals(Dialog.Status.CANCELLED) && !status.equals(Dialog.Status.WINDOW_CLOSED)) {
          ActionDefinitionPanel pane = ActionWindow.this.getWindowContent();
          Action action = pane.getAction();
          Stage stage = null;
          if(getDefaultModel() != null && getDefaultModelObject() != null) {
            stage = (Stage) getDefaultModelObject();
          }
          doAction(target, stage, action);
        }

        if(additionnalWindowClosedCallback != null) {
          additionnalWindowClosedCallback.onClose(target, status);
          additionnalWindowClosedCallback = null;
        }
      }
    });

  }

  /**
   * On action performed.
   * @param target
   */
  public abstract void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action);

  public void show(AjaxRequestTarget target, IModel<Stage> stageModel, ActionDefinition actionDefinition) {
    show(target, stageModel, actionDefinition, null);
  }

  public void show(AjaxRequestTarget target, IModel<Stage> stageModel, ActionDefinition actionDefinition, WindowClosedCallback additionnalWindowClosedCallback) {
    if(actionDefinition.somethingAsked()) {
      this.additionnalWindowClosedCallback = additionnalWindowClosedCallback;
      setDefaultModel(stageModel);
      ActionDefinitionPanel component = new ActionDefinitionPanel(getContentId(), actionDefinition, target) {
      };
      component.add(new AttributeModifier("class", true, new Model("obiba-content window-action-content")));
      setContent(content = component);

      final IModel labelModel = new MessageSourceResolvableStringModel(actionDefinition.getLabel());
      if(stageModel != null && stageModel.getObject() != null) {
        Model titleModel = new Model() {
          @Override
          public Serializable getObject() {
            Stage stage = (Stage) ActionWindow.this.getDefaultModelObject();
            MessageSourceResolvableStringModel stageDescriptionModel = new MessageSourceResolvableStringModel(stage.getDescription());
            return stageDescriptionModel.getObject() + ": " + labelModel.getObject();
          }
        };
        setTitle(titleModel);
      } else {
        setTitle(labelModel);
      }

      show(target);
    } else {
      // Do action without showing the window (nothing to ask).
      doAction(target, stageModel.getObject(), new Action(actionDefinition));
    }
  }

  public ActionDefinitionPanel getWindowContent() {
    return content;
  }

  private void doAction(AjaxRequestTarget target, Stage stage, Action action) {
    log.debug("action=" + action);
    activeInterviewService.doAction(stage, action);
    onActionPerformed(target, stage, action);
  }
}

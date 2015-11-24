/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.test;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * 
 */
public abstract class TestForm extends Panel {

  public TestForm() {
    super(null);
  }

  public TestForm(String id) {
    super(id);
    Form form = new Form("form");
    add(form);
    form.add(populateContent("content"));
  }

  public abstract Component populateContent(String id);

}

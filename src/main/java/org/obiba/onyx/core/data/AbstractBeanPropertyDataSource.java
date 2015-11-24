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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Date;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;

/**
 * builds a data of the specified type from the bean property
 */
public abstract class AbstractBeanPropertyDataSource implements IDataSource {

  private static final long serialVersionUID = 1L;

  private String property;

  private String unit;

  @Override
  public Data getData(Participant participant) {

    Object object = getBean(participant);

    if(object == null) return null;

    Data data = null;
    try {
      for(PropertyDescriptor pd : Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors()) {
        if(property.equals(pd.getName())) {
          Object propertyValue = pd.getReadMethod().invoke(object);

          if(propertyValue != null && propertyValue.getClass().isEnum()) {
            propertyValue = propertyValue.toString();
          }

          if(propertyValue != null) {
            data = DataBuilder.build((Serializable) propertyValue);
          } else {
            Class<?> valueClass = pd.getReadMethod().getReturnType();
            if(valueClass.isAssignableFrom(Boolean.class)) {
              data = DataBuilder.buildBoolean((Boolean) null);
            } else if(valueClass.isAssignableFrom(Date.class) || valueClass.isAssignableFrom(java.sql.Date.class)) {
              data = DataBuilder.buildDate((Date) null);
            } else if(valueClass.isAssignableFrom(Double.class) || valueClass.isAssignableFrom(Float.class)) {
              data = DataBuilder.buildDecimal((Double) null);
            } else if(valueClass.isAssignableFrom(Integer.class) || valueClass.isAssignableFrom(Long.class)) {
              data = DataBuilder.buildInteger((Integer) null);
            } else if(valueClass.isAssignableFrom(String.class)) {
              data = DataBuilder.buildText((String) null);
            }
          }
        }
      }
    } catch(Exception e) {
      throw new IllegalArgumentException("Could not resolve " + object.getClass().getSimpleName() + " property " + property, e);
    }

    return data;
  }

  @Override
  public String getUnit() {
    return unit;
  }

  protected AbstractBeanPropertyDataSource(String property) {
    this.property = property;
  }

  public abstract Object getBean(Participant participant);

  public void setProperty(String property) {
    this.property = property;
  }

  public AbstractBeanPropertyDataSource setUnit(String unit) {
    this.unit = unit;
    return this;
  }

  @Override
  public String toString() {
    return property;
  }

  public String getProperty() {
    return property;
  }

}

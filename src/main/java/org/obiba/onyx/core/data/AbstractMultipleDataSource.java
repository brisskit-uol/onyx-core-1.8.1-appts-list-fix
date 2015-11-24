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
import java.util.List;

/**
 * Holds a list of data sources.
 */
public abstract class AbstractMultipleDataSource implements IDataSource {

  private static final long serialVersionUID = 1L;

  private List<IDataSource> dataSources;

  public List<IDataSource> getDataSources() {
    return (dataSources != null) ? dataSources : (dataSources = new ArrayList<IDataSource>());
  }

  public AbstractMultipleDataSource addDataSource(IDataSource dataSource) {
    if(dataSource != null) {
      getDataSources().add(dataSource);
    }
    return this;
  }

  public AbstractMultipleDataSource addDataSources(@SuppressWarnings("hiding") IDataSource... dataSources) {
    if(dataSources != null) {
      for(IDataSource dataSource : dataSources) {
        addDataSource(dataSource);
      }
    }
    return this;
  }

  public AbstractMultipleDataSource() {
  }

  @Override
  public String toString() {
    return getDataSources().toString();
  }

}

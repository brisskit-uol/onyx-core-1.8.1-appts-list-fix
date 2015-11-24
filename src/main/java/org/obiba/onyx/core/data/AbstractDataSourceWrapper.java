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

/**
 * Abstract class used to chain data sources.
 */
public abstract class AbstractDataSourceWrapper implements IDataSource {

  private static final long serialVersionUID = 1L;

  private IDataSource dataSource;

  public IDataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(IDataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public String toString() {
    return dataSource.toString();
  }

}

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

import java.util.List;

import org.obiba.magma.ValueSet;
import org.obiba.onyx.core.domain.statistics.ExportLog;

/**
 * Maintains a list of exported entities. Provides a way of keeping track of what was exported and to which destination.
 */
public interface ExportLogService {

  /**
   * Saves an {@link ExportLog}. The {@link ValueSet} capture start date and capture end date may not overlap with
   * another ExportLog from the same entity (and destination?).
   * @param exportLog
   * @throws IllegalArgumentException If ValueSet overlaps.
   */
  public void save(ExportLog exportLog);

  /**
   * Returns a list of {@link ExportLog}s for a particular VariableEntity type and destination.
   * @param entityTypeName name of the VariableEntity type. (e.g. Participant)
   * @param destination name of the destination. (e.g. DCC)
   * @param ascending <code>true</code> returns a list sorted in chronological order (by export date),
   * <code>false</code> returns a list in reverse chronological order
   * @return list of ExportLogs
   */
  public List<ExportLog> getExportLogs(String entityTypeName, String destination, boolean ascending);

  /**
   * Returns a list of {@link ExportLog}s for a particular entity, of a particular type and destination.
   * @param entityTypeName name of the VariableEntity type. (e.g. Participant)
   * @param identifier entity identifier
   * @param destination name of the destination. (e.g. DCC)
   * @param ascending <code>true</code> returns a list sorted in chronological order (by export date),
   * <code>false</code> returns a list in reverse chronological order
   * @return
   */
  public List<ExportLog> getExportLogs(String entityTypeName, String identifier, String destination, boolean ascending);

  /**
   * Returns the last {@link ExportLog} for a particular VariableEntity.
   * @param entityTypeName
   * @param identifier
   * @return
   */
  public ExportLog getLastExportLog(String entityTypeName, String identifier);

  /**
   * Returns the last {@link ExportLog} for a particular VariableEntity and destination.
   * @param entityTypeName
   * @param identifier
   * @param destination
   * @return
   */
  public ExportLog getLastExportLog(String entityTypeName, String identifier, String destination);

}

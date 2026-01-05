package org.folio.qm.service.population;

import static org.folio.qm.convertion.elements.Constants.LEADER_LENGTH;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.convertion.elements.LeaderItem;
import org.folio.qm.domain.model.BaseQuickMarcRecord;

@Log4j2
public abstract class LeaderMarcPopulationService implements MarcPopulationService {

  @Override
  public void populate(BaseQuickMarcRecord qmRecord) {
    log.trace("populate:: Populating leader values for format: {}", qmRecord.getMarcFormat());
    var initialLeader = qmRecord.getLeader();
    if (LEADER_LENGTH != initialLeader.length()) {
      log.warn("populate:: Invalid leader length: {}. Expected: {}. Skipping population",
        initialLeader.length(), LEADER_LENGTH);
      return;
    }

    var leader = populateValues(initialLeader, getConstantLeaderItems());
    qmRecord.setLeader(leader);
    log.trace("populate:: Leader values populated successfully");
  }

  protected abstract List<LeaderItem> getConstantLeaderItems();

  /**
   * Populates values.
   *
   * @param leader      to populate default values
   * @param leaderItems leader items that should be populated if leader has not acceptable value
   * @return cleaned leader
   */
  protected String populateValues(String leader, List<LeaderItem> leaderItems) {
    var leaderBuilder = new StringBuilder(leader);

    leaderItems.stream()
      .filter(leaderItem -> !leaderItem.getPossibleValues().contains(leaderBuilder.charAt(leaderItem.getPosition())))
      .forEach(leaderItem -> setLeaderChar(leaderItem, leaderBuilder));

    return leaderBuilder.toString();
  }

  private void setLeaderChar(LeaderItem leaderItem, StringBuilder leaderBuilder) {
    leaderBuilder.setCharAt(leaderItem.getPosition(), leaderItem.getPossibleValues().getFirst());
  }
}

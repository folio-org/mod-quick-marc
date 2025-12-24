//package org.folio.support;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import java.util.List;
//import java.util.UUID;
//import org.folio.qm.client.model.Instance;
//import org.folio.qm.service.support.PrecedingSucceedingTitlesHelper;
//import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitle;
//import org.folio.spring.testing.type.UnitTest;
//import org.junit.jupiter.api.Test;
//
//@UnitTest
//class PrecedingSucceedingTitlesHelperTest {
//
//  private static final String PRECEDING_TITLE = "Preceding Title";
//  private static final String SUCCEEDING_TITLE = "Succeeding Title";
//  private static final String PRECEDING_HRID = "P-HRID";
//  private static final String SUCCEEDING_HRID = "S-HRID";
//
//  private final PrecedingSucceedingTitlesHelper helper = new PrecedingSucceedingTitlesHelper();
//
//  @Test
//  void updatePrecedingSucceedingTitles_shouldReturnBoth_whenBothPresent() {
//    var instanceId = UUID.randomUUID().toString();
//    var precedingId = UUID.randomUUID().toString();
//    var succeedingId = UUID.randomUUID().toString();
//    var preceding = new InstancePrecedingSucceedingTitle(null, precedingId, null, PRECEDING_TITLE, PRECEDING_HRID,
//      null, null);
//    var succeeding = new InstancePrecedingSucceedingTitle(null, null, succeedingId, SUCCEEDING_TITLE, SUCCEEDING_HRID,
//      null, null);
//    var instance = createInstance(instanceId, List.of(preceding), List.of(succeeding));
//
//    var result = helper.updatePrecedingSucceedingTitles(instance);
//    assertEquals(2, result.getTotalRecords());
//    assertEquals(2, result.getPrecedingSucceedingTitles().size());
//    assertTrue(result.getPrecedingSucceedingTitles().stream().anyMatch(t -> PRECEDING_TITLE.equals(t.getTitle())));
//    assertTrue(result.getPrecedingSucceedingTitles().stream().anyMatch(t -> SUCCEEDING_TITLE.equals(t.getTitle())));
//  }
//
//  @Test
//  void updatePrecedingSucceedingTitles_shouldReturnOnlyPreceding_whenOnlyPrecedingPresent() {
//    var instanceId = UUID.randomUUID().toString();
//    var precedingId = UUID.randomUUID().toString();
//    var precedingList = List.of(
//      new InstancePrecedingSucceedingTitle(null, precedingId, null, PRECEDING_TITLE, PRECEDING_HRID, null, null));
//    var instance = createInstance(instanceId, precedingList, null);
//
//    var result = helper.updatePrecedingSucceedingTitles(instance);
//    assertEquals(1, result.getTotalRecords());
//    assertEquals(PRECEDING_TITLE, result.getPrecedingSucceedingTitles().getFirst().getTitle());
//  }
//
//  @Test
//  void updatePrecedingSucceedingTitles_shouldReturnOnlySucceeding_whenOnlySucceedingPresent() {
//    var instanceId = UUID.randomUUID().toString();
//    var succeedingId = UUID.randomUUID().toString();
//    var succeedingList = List.of(
//      new InstancePrecedingSucceedingTitle(null, null, succeedingId, SUCCEEDING_TITLE, SUCCEEDING_HRID, null, null));
//    var instance = createInstance(instanceId, null, succeedingList);
//
//    var result = helper.updatePrecedingSucceedingTitles(instance);
//    assertEquals(1, result.getTotalRecords());
//    assertEquals(SUCCEEDING_TITLE, result.getPrecedingSucceedingTitles().getFirst().getTitle());
//  }
//
//  @Test
//  void updatePrecedingSucceedingTitles_shouldReturnEmpty_whenNonePresent() {
//    var instance = createInstance(UUID.randomUUID().toString(), null, null);
//
//    var result = helper.updatePrecedingSucceedingTitles(instance);
//    assertEquals(0, result.getTotalRecords());
//    assertTrue(result.getPrecedingSucceedingTitles().isEmpty());
//  }
//
//  @Test
//  void updatePrecedingSucceedingTitles_shouldHandleEmptyLists() {
//    var instance = createInstance(UUID.randomUUID().toString(), List.of(), List.of());
//
//    var result = helper.updatePrecedingSucceedingTitles(instance);
//    assertEquals(0, result.getTotalRecords());
//    assertTrue(result.getPrecedingSucceedingTitles().isEmpty());
//  }
//
//  private Instance createInstance(String id,
//                                  List<InstancePrecedingSucceedingTitle> preceding,
//                                  List<InstancePrecedingSucceedingTitle> succeeding) {
//    Instance instance = new Instance();
//    instance.setId(id);
//    instance.setPrecedingTitles(preceding);
//    instance.setSucceedingTitles(succeeding);
//    return instance;
//  }
//}

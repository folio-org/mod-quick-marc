package org.folio.qm.holder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.folio.qm.utils.APITestUtils.TENANT_ID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.folio.qm.holder.impl.TenantsHolderImpl;

@ExtendWith(MockitoExtension.class)
class TenantsHolderImplTest {

  @Spy
  private TenantsHolderImpl tenantsHolder;

  @Test
  void shouldAddElement() {
    tenantsHolder.add(TENANT_ID);

    assertTrue(tenantsHolder.getAll().contains(TENANT_ID));
  }

  @Test
  void shouldRemoveElement() {
    tenantsHolder.add(TENANT_ID);
    tenantsHolder.remove(TENANT_ID);

    assertFalse(tenantsHolder.getAll().contains(TENANT_ID));
  }

  @Test
  void shouldReturnElementCount() {
    tenantsHolder.add(TENANT_ID);

    assertEquals(1, tenantsHolder.count());
  }
}

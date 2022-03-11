package org.folio.qm.support.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import org.folio.qm.support.extension.impl.PostgresContainerExtension;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(PostgresContainerExtension.class)
public @interface EnablePostgres {
}

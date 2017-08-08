/*
 * Copyright (c) 2017, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.jdbc;

import static org.junit.Assert.assertEquals;
import static org.postgresql.jdbc.TypeInfoCacheTestParameters.parseableParsingParams;

import org.postgresql.jdbc.TypeInfoCache.PgType;
import org.postgresql.jdbc.TypeInfoCacheTestUtil.PgTypeStruct;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TypeInfoCachePgTypeQuotingTest {

  private final PgType pgType;
  private final String qualifiedName;
  private final String onPathName;

  @Parameterized.Parameters(name = "{0}")
  public static Iterable<Object[]> data() {
    Collection<Object[]> params = new ArrayList<>();
    for (Object[] c : parseableParsingParams()) {
      params.add(new Object[]{c[0], c[1], c[2]});
    }
    return params;
  }

  public TypeInfoCachePgTypeQuotingTest(PgTypeStruct typeStruct, String qualifiedName, String onPathName) {
    this.pgType = typeStruct.toPgType();
    this.qualifiedName = qualifiedName;
    this.onPathName = onPathName;
  }

  @Test
  public void testQualifiedName() {
    assertEquals("type name string", qualifiedName, pgType.qualifiedName());
  }

  @Test
  public void testOnPathName() {
    assertEquals("type name string", onPathName, pgType.onPathName());
  }

}

/*
 * Copyright (c) 2005, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.jdbc;

import static org.junit.Assert.assertNull;
import static org.postgresql.jdbc.TypeInfoCacheTestParameters.unparseableParsingParams;

import org.postgresql.jdbc.TypeInfoCache.ParsedTypeName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TypeInfoCacheParsedTypeNameFromStringUnparseableTest {

  @Parameters(name = "{0}")
  public static Iterable<Object[]> data() {
    return unparseableParsingParams();
  }

  private final String nameString;

  public TypeInfoCacheParsedTypeNameFromStringUnparseableTest(String nameString) {
    this.nameString = nameString;
  }

  @Test
  public void testFromStringUnparseableTest() throws Exception {
    assertNull(ParsedTypeName.fromString(nameString));
  }
}

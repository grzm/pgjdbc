/*
 * Copyright (c) 2017, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.jdbc;

import static org.junit.Assert.assertEquals;
import static org.postgresql.jdbc.TypeInfoCacheTestParameters.parseableParsingParams;

import org.postgresql.jdbc.TypeInfoCache.ParsedTypeName;
import org.postgresql.jdbc.TypeInfoCacheTestUtil.PgTypeStruct;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TypeInfoCacheParsedTypeNameFromStringTest {

  @SuppressWarnings("unchecked")
  @Parameterized.Parameters(name = "{0} → {2} (onPath: {1})")
  public static Iterable<Object[]> data() {
    Collection<Object[]> params = new ArrayList<>();
    for (Object[] c : parseableParsingParams()) {
      PgTypeStruct type = (PgTypeStruct) c[0];

      String qualifiedName = (String) c[1];
      boolean onPath = false;
      //noinspection ConstantConditions
      params.add(new Object[]{qualifiedName, onPath, type});

      String onPathName = (String) c[2];
      onPath = true;
      //noinspection ConstantConditions
      params.add(new Object[]{onPathName, onPath, type});

      //noinspection unchecked
      for (Object[] nameStringData : (ArrayList<Object[]>) c[3]) {
        String nameString = (String) nameStringData[0];
        onPath = (boolean) nameStringData[1];
        params.add(new Object[]{nameString, onPath, type});
      }
    }
    return params;
  }

  private final String nameString;
  private final PgTypeStruct typeStruct;


  public TypeInfoCacheParsedTypeNameFromStringTest(String nameString, boolean onPath, PgTypeStruct typeStruct) {
    this.nameString = nameString;
    this.typeStruct = onPath ? PgTypeStruct.createOnPath(typeStruct) : typeStruct;
  }

  @Test
  public void testParsedTypeNameFromString() {
    ParsedTypeName typeName = ParsedTypeName.fromString(nameString);
    assertEquals(typeStruct, new PgTypeStruct(typeName));
  }
}

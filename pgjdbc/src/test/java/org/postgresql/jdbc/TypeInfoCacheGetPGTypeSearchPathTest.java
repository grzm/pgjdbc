/*
 * Copyright (c) 2017, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.jdbc;

import static org.junit.Assert.assertEquals;
import static org.postgresql.jdbc.TypeInfoCacheTestUtil.join;
import static org.postgresql.jdbc.TypeInfoCacheTestUtil.quotify;

import org.postgresql.core.TypeInfo;
import org.postgresql.jdbc.TypeInfoCacheTestUtil.PgTypeSet;
import org.postgresql.jdbc.TypeInfoCacheTestUtil.PgTypeStruct;
import org.postgresql.test.jdbc2.BaseTest4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@RunWith(Parameterized.class)
public class TypeInfoCacheGetPGTypeSearchPathTest extends BaseTest4 {

  private static final String[] DEFAULT_SEARCH_PATH = new String[0];

  private final String nameString;
  private final PgTypeStruct type;
  private final PgTypeStruct arrayType;
  private final ArrayList<PgTypeStruct> types;
  private final String searchPath;

  private TypeInfo typeInfo;
  private PgTypeSet typeSet;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    typeSet = PgTypeSet.createAndInstall(types, con);
    typeInfo = ((PgConnection) con).getTypeInfo();
    if (searchPath != null) {
      String searchPathSql = "SET search_path TO " + searchPath;
      con.createStatement().execute(searchPathSql);
    }
  }

  @Override
  public void tearDown() throws SQLException {
    typeSet.uninstall(con);
    super.tearDown();
  }

  @Parameterized.Parameters(name = "{0} Path: {4}; Types: {3}")
  public static Iterable<Object[]> data() {
    Collection<Object[]> cases = new ArrayList<>();

    /*
    Each case is: types, search path schemas, and a map of type name strings to the
    PgTypeStruct instance it should match, with PgTypeStruct.UNSPECIFIED for no match.

    Note:
    ParsedTypeName.fromString lowers unquoted type names, so type name string "NS.TYPE" will be
    parsed as (nspname, typname) = ('ns', 'type'), and type name string "TYPE" will be parsed as
    the unqualified type (typname) = ('type'). This accounts for matching the lowered type names.
     */

    // baseline
    cases.add(new Object[]{
        new PgTypeStruct[]{new PgTypeStruct("pg_catalog", "text")},
        DEFAULT_SEARCH_PATH,
        new HashMap<String, PgTypeStruct>() {
          {
            put("text", new PgTypeStruct("pg_catalog", "text"));
            put("%text%", new PgTypeStruct("pg_catalog", "text"));
            put("TEXT", new PgTypeStruct("pg_catalog", "text"));
            put("%TEXT%", PgTypeStruct.UNSPECIFIED);
          }
        },
    });

    cases.add(new Object[]{
        new PgTypeStruct[]{new PgTypeStruct("pg_catalog", "text"),
            new PgTypeStruct("public", "text")},
        DEFAULT_SEARCH_PATH,
        new HashMap<String, Object>() {
          {
            put("text", new PgTypeStruct("pg_catalog", "text"));
            put("%text%", new PgTypeStruct("pg_catalog", "text"));
            put("TEXT", new PgTypeStruct("pg_catalog", "text"));
            put("%TEXT%", PgTypeStruct.UNSPECIFIED);
          }
        },
    });

    // only quoted %TEXT% matches public.TEXT
    cases.add(new Object[]{
        new PgTypeStruct[]{
            new PgTypeStruct("pg_catalog", "text"),
            new PgTypeStruct("public", "TEXT")},
        DEFAULT_SEARCH_PATH,
        new HashMap<String, PgTypeStruct>() {
          {
            put("TEXT", new PgTypeStruct("pg_catalog", "text"));
            put("%TEXT%", new PgTypeStruct("public", "TEXT"));
          }
        },
    });

    /*
     The following three tests are to confirm a legacy bug is fixed. Types were matched even when
     they were not on the search path.
     For details, see
       - https://github.com/pgjdbc/pgjdbc/commit/818b84d28ac33cdc538fbfd4ccb6c6287d2003da
       - https://github.com/pgjdbc/pgjdbc/commit/b383f6d2c8f19e2b5b867039ca96071ba8d495e1
     */
    cases.add(new Object[]{
        new PgTypeStruct[]{
            new PgTypeStruct("a", "type"),
            new PgTypeStruct("x", "type")
        },
        new String[]{"a"},
        new HashMap<String, PgTypeStruct>() {
          {
            put("type", new PgTypeStruct("a", "type"));
            put("%type%", new PgTypeStruct("a", "type"));
          }
        },
    });

    cases.add(new Object[]{
        new PgTypeStruct[]{
            new PgTypeStruct("x", "type")
        },
        DEFAULT_SEARCH_PATH,
        new HashMap<String, Object>() {
          {
            put("type", PgTypeStruct.UNSPECIFIED);
            put("%type%", PgTypeStruct.UNSPECIFIED);
          }
        },
    });
    cases.add(new Object[]{
        new PgTypeStruct[]{
            new PgTypeStruct("a", "type"),
            new PgTypeStruct("x", "type")
        },
        DEFAULT_SEARCH_PATH,
        new HashMap<String, Object>() {
          {
            put("type", PgTypeStruct.UNSPECIFIED);
          }
        },
    });

    /*
    The following two tests are to confirm that a legacy bug is fixed. Creation order does not
    affect which type is matched.
     */
    cases.add(new Object[]{
        new PgTypeStruct[]{
            new PgTypeStruct("a", "type"),
            new PgTypeStruct("b", "type")
        },
        new String[]{"a", "b"},
        new HashMap<String, Object>() {
          {
            put("type", new PgTypeStruct("a", "type"));
            put("%type%", new PgTypeStruct("a", "type"));
          }
        },
    });

    cases.add(new Object[]{
        new PgTypeStruct[]{
            new PgTypeStruct("b", "type"),
            new PgTypeStruct("a", "type")
        },
        new String[]{"a", "b"},
        new HashMap<String, PgTypeStruct>() {
          {
            put("type", new PgTypeStruct("a", "type"));
            put("%type%", new PgTypeStruct("a", "type"));
          }
        },
    });

    Collection<Object[]> params = new ArrayList<>();
    for (Object[] c : cases) {
      @SuppressWarnings("unchecked")
      ArrayList<PgTypeStruct> types = new ArrayList<>();
      types.addAll(Arrays.asList((PgTypeStruct[]) c[0]));
      @SuppressWarnings("unchecked")
      HashMap<String, Object> nameStrings = (HashMap<String, Object>) c[2];
      String searchPath = join(",", (String[]) c[1]);
      for (String nameString : nameStrings.keySet()) {
        Object obj = nameStrings.get(nameString);
        PgTypeStruct type;
        PgTypeStruct elementType;
        if (obj instanceof List) {
          @SuppressWarnings("unchecked")
          List<PgTypeStruct> expectedTypes = (List<PgTypeStruct>) obj;
          type = expectedTypes.get(0);
          elementType = PgTypeStruct.createArrayType(expectedTypes.get(1));
        } else {
          type = (PgTypeStruct) obj;
          elementType = PgTypeStruct.createArrayType(type);
        }
        params.add(
            new Object[]{quotify(nameString), type, elementType, types, searchPath});
      }
    }
    return params;
  }

  public TypeInfoCacheGetPGTypeSearchPathTest(String nameString, PgTypeStruct type,
      PgTypeStruct arrayType,
      ArrayList<PgTypeStruct> types, String searchPath) {
    this.nameString = nameString;
    this.type = type;
    this.arrayType = arrayType;
    this.types = types;
    this.searchPath = searchPath;
  }

  @Test
  public void testMatchesSearchPath() throws SQLException {
    int oid = typeInfo.getPGType(nameString);
    PgTypeStruct actualType = typeSet.type(oid);
    assertEquals(type, actualType);
  }

  @Test
  public void testArrayMatchesSearchPath() throws SQLException {
    typeSet.assumeSupportedType(con, arrayType);
    int arrayOid = typeInfo.getPGArrayType(nameString);
    PgTypeStruct actualArrayType = typeSet.type(arrayOid);
    assertEquals(arrayType, actualArrayType);
  }
}

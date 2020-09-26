package com.ragin.bdd.cucumber;

import java.util.regex.Pattern;

public final class BddLibConstants {
    public static final String BDD_LIB_NOT = "@bdd_lib_not ";
    public static final String BDD_LIB_NOT_EXISTS = "@bdd_lib_not_exist";
    public static final Pattern BDD_LIB_MATCHER_PATTERN = Pattern.compile("(\\$\\{json-unit\\.matches:)(\\w*)(.*)");
}

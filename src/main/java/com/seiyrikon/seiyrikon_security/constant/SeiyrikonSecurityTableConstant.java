package com.seiyrikon.seiyrikon_security.constant;

public class SeiyrikonSecurityTableConstant {
    //tables names
    public static final String defaultRoleTable = "role";
    public static final String defaultUserRoleTable = "user_role";

    //table pk types
    public static final String defaultIdType = "BIGINT";

    //role table columns
    public static final String defaultRoleIdColumn = "id";
    public static final String defaultRoleNameColumn = "name";

    //user_role table columns
    public static final String defaultUserRoleUserIdColumn = "user_id";
    public static final String defaultUserRoleRoleIdColumn = "role_id";
}

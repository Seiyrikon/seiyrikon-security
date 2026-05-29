package com.seiyrikon.seiyrikon_security.configuration;

import com.seiyrikon.seiyrikon_security.constant.SeiyrikonSecurityTableConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seiyrikon-security.table-configuration")
@Data
public class SeiyrikonSecurityTableProperties {
    //table names
    private String roleTable = SeiyrikonSecurityTableConstant.defaultRoleTable;
    private String userRoleTable = SeiyrikonSecurityTableConstant.defaultUserRoleTable;

    //table id types
    private String roleIdType = SeiyrikonSecurityTableConstant.defaultIdType;
    private String userIdType = SeiyrikonSecurityTableConstant.defaultIdType;

    //role table columns
    private String roleIdColumn = SeiyrikonSecurityTableConstant.defaultRoleIdColumn;
    private String roleNameColumn = SeiyrikonSecurityTableConstant.defaultRoleNameColumn;

    //user_role table columns
    private String userRoleRoleIdColumn = SeiyrikonSecurityTableConstant.defaultUserRoleRoleIdColumn;
    private String userRoleUserIdColumn = SeiyrikonSecurityTableConstant.defaultUserRoleUserIdColumn;

    private boolean autoCreateSeiyrikonSecurityTable = true;
}

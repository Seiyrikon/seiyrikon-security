package com.seiyrikon.seiyrikon_security.configuration;

import com.seiyrikon.seiyrikon_security.constant.SeiyrikonSecurityConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seiyrikon-security.table-configuration")
@Data
public class SeiyrikonSecurityTableProperties {
    //table names
    private String roleTable = SeiyrikonSecurityConstant.defaultRoleTable;
    private String userRoleTable = SeiyrikonSecurityConstant.defaultUserRoleTable;

    //table id types
    private String roleIdType = SeiyrikonSecurityConstant.defaultIdType;
    private String userIdType = SeiyrikonSecurityConstant.defaultIdType;

    //role table columns
    private String roleIdColumn = SeiyrikonSecurityConstant.defaultRoleIdColumn;
    private String roleNameColumn = SeiyrikonSecurityConstant.defaultRoleNameColumn;

    //user_role table columns
    private String userRoleRoleIdColumn = SeiyrikonSecurityConstant.defaultUserRoleRoleIdColumn;
    private String userRoleUserIdColumn = SeiyrikonSecurityConstant.defaultUserRoleUserIdColumn;

    private boolean autoCreateSeiyrikonSecurityTable = true;
}

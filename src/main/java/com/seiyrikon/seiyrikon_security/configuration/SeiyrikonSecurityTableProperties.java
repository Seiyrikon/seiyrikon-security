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
    private String tokenBlacklistTable = SeiyrikonSecurityTableConstant.defaultTokenBlacklistTable;

    //table id types
    private String roleIdType = SeiyrikonSecurityTableConstant.defaultIdType;
    private String userIdType = SeiyrikonSecurityTableConstant.defaultIdType;
    private String tokenIdType =  SeiyrikonSecurityTableConstant.defaultStringIdType;

    //role table columns
    private String roleIdColumn = SeiyrikonSecurityTableConstant.defaultRoleIdColumn;
    private String roleNameColumn = SeiyrikonSecurityTableConstant.defaultRoleNameColumn;

    //user_role table columns
    private String userRoleRoleIdColumn = SeiyrikonSecurityTableConstant.defaultUserRoleRoleIdColumn;
    private String userRoleUserIdColumn = SeiyrikonSecurityTableConstant.defaultUserRoleUserIdColumn;

    //tkn_blcklst table columns
    private String tokenBlacklistTokenColumn = SeiyrikonSecurityTableConstant.defaultTokenColumn;
    private String tokenBlacklistExpiryColumn = SeiyrikonSecurityTableConstant.defaultExpiryColumn;

    private boolean autoCreateSeiyrikonSecurityTable = true;
}

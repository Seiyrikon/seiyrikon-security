package com.seiyrikon.seiyrikon_security.repository;

import com.seiyrikon.seiyrikon_security.tables.composite.user_role.SeiyrikonSecurityUserRole;
import com.seiyrikon.seiyrikon_security.tables.composite.user_role.id.SeiyrikonSecurityUserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SeiyrikonSecurityUserRoleRepository extends JpaRepository<SeiyrikonSecurityUserRole, SeiyrikonSecurityUserRoleId> {

    @Query("""
        select r.name from SeiyrikonSecurityRole r
        join SeiyrikonSecurityUserRole ur on r.id = ur.id.roleId
        where ur.id.userId = :userId
        """)
    List<String> findRoleNamesByUserId(@Param("userId") String userId);
}

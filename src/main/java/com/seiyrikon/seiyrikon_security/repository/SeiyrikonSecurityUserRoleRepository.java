package com.seiyrikon.seiyrikon_security.repository;

import com.seiyrikon.seiyrikon_security.configuration.SeiyrikonSecurityTableProperties;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SeiyrikonSecurityUserRoleRepository {

    private final EntityManager entityManager;
    private final SeiyrikonSecurityTableProperties properties;

    public List<String> getUserRoles(Object userId) {
        String sql = String.format(
                "SELECT r.%s FROM %s r " +
                "JOIN %s ur ON r.%s = ur.%s" +
                "WHERE ur.%s = :userId",
                properties.getRoleNameColumn(),
                properties.getRoleTable(),
                properties.getUserRoleTable(),
                properties.getRoleIdColumn(),
                properties.getUserRoleRoleIdColumn(),
                properties.getUserRoleUserIdColumn()
        );

        return entityManager.createNativeQuery(sql)
                .setParameter("userId", userId)
                .getResultList();
    }
}

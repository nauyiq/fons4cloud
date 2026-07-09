package com.fons.cloud.auth.core;

import com.fons.cloud.auth.common.AuthorizationResourceDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthorizationResourceRepositoryTest {

    private RMap<String, Object> authorizationResourceMap;
    private AuthorizationResourceRepository repository;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        RedissonClient redissonClient = mock(RedissonClient.class);
        authorizationResourceMap = mock(RMap.class);
        RSet<String> ignoredAccessTokenUriList = mock(RSet.class);
        RSet<String> identifierTokenUriList = mock(RSet.class);
        when(redissonClient.<String, Object>getMap("GLOBAL_AUTHORIZATION_RESOURCE")).thenReturn(authorizationResourceMap);
        when(redissonClient.<String>getSet("GLOBAL_IGNORED_ACCESS_TOKEN_API")).thenReturn(ignoredAccessTokenUriList);
        when(redissonClient.<String>getSet("GLOBAL_IDENTIFIER_TOKEN_API")).thenReturn(identifierTokenUriList);
        repository = new AuthorizationResourceRepository(redissonClient);
    }

    @Test
    void registerAuthorizationResourceShouldMergeStringValueFromRedis() {
        when(authorizationResourceMap.get("GET_/admin/services")).thenReturn("services:view");

        repository.registerAuthorizationResource(resource("GET_/admin/services", "services:edit"));

        verify(authorizationResourceMap).put(eq("GET_/admin/services"), org.mockito.ArgumentMatchers.argThat(value -> {
            assertThat(value).isInstanceOf(Set.class);
            assertThat(asStringSet(value)).containsExactlyInAnyOrder("services:view", "services:edit");
            return true;
        }));
    }

    @Test
    void registerAuthorizationResourceShouldMergeJsonArrayValueFromRedis() {
        when(authorizationResourceMap.get("POST_/admin/changes/drafts")).thenReturn("[\"changes:view\",\"changes:edit\"]");

        repository.registerAuthorizationResource(resource("POST_/admin/changes/drafts", "changes:publish"));

        verify(authorizationResourceMap).put(eq("POST_/admin/changes/drafts"), org.mockito.ArgumentMatchers.argThat(value -> {
            assertThat(value).isInstanceOf(Set.class);
            assertThat(asStringSet(value)).containsExactlyInAnyOrder("changes:view", "changes:edit", "changes:publish");
            return true;
        }));
    }

    @Test
    void registerAuthorizationResourceShouldMergeCollectionToStringValueFromRedis() {
        when(authorizationResourceMap.get("POST_/admin/access/resources/drafts")).thenReturn("[access:view, access:edit]");

        repository.registerAuthorizationResource(resource("POST_/admin/access/resources/drafts", "access:publish"));

        verify(authorizationResourceMap).put(eq("POST_/admin/access/resources/drafts"), org.mockito.ArgumentMatchers.argThat(value -> {
            assertThat(value).isInstanceOf(Set.class);
            assertThat(asStringSet(value)).containsExactlyInAnyOrder("access:view", "access:edit", "access:publish");
            return true;
        }));
    }

    private AuthorizationResourceDTO resource(String id, String authority) {
        AuthorizationResourceDTO dto = new AuthorizationResourceDTO();
        dto.setId(id);
        dto.setAuthorities(Set.of(authority));
        return dto;
    }

    @SuppressWarnings("unchecked")
    private Set<String> asStringSet(Object value) {
        return (Set<String>) value;
    }
}

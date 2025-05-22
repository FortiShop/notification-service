package org.fortishop.notificationservice.utils;

import jakarta.servlet.http.HttpServletRequest;

public class AuthHeaderUtils {

    public static Long extractMemberId(HttpServletRequest request) {
        String idHeader = request.getHeader("x-member-id");
        if (idHeader == null || idHeader.isBlank()) {
            throw new IllegalArgumentException("x-member-id 헤더가 없습니다.");
        }
        return Long.valueOf(idHeader);
    }

    public static void validateAdmin(HttpServletRequest request) {
        String role = request.getHeader("x-member-role");
        if (role == null || !role.equalsIgnoreCase("ROLE_ADMIN")) {
            throw new SecurityException("관리자 권한이 없습니다.");
        }
    }
}

package com.auction.common.message;

import com.auction.common.enums.UserRole;
import java.util.EnumSet;
import java.util.Set;

/**
 * Shared policy for self-registration role rules.
 */
public final class RegistrationPolicy {
    private static final Set<UserRole> SELF_REGISTER_ALLOWED_ROLES =
            EnumSet.of(UserRole.BIDDER, UserRole.SELLER); //Set chuyên dùng cho enum trong java, .of để tạo nhanh 1 tập enum từ các giá trị cho trước

    private RegistrationPolicy() {
    }

    public static boolean isSelfRegistrationAllowed(UserRole role) {
        return role != null && SELF_REGISTER_ALLOWED_ROLES.contains(role);
    }

    public static String invalidRoleMessage() {
        return "Only BIDDER or SELLER.";
    }
}


package org.hadiroyan.retailhub.util;

import java.util.UUID;

public class TestConstanst {

    // Test User IDs (fixed UUIDs for predictable testing)
    public static final UUID SUPER_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    public static final UUID DISABLED_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    public static final UUID OAUTH_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

    // Test User Emails
    public static final String SUPER_ADMIN_EMAIL = "test.superadmin@test.com";
    public static final String CUSTOMER_EMAIL = "test.customer@test.com";
    public static final String OWNER_EMAIL = "test.owner@test.com";
    public static final String DISABLED_USER_EMAIL = "test.disabled@test.com";
    public static final String OAUTH_USER_EMAIL = "test.oauth@test.com";

    // Test Password
    public static final String TEST_PASSWORD = "password123";

}
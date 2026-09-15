package com.pos.security;

/**
 * The three application roles. Every login redirects the frontend to a
 * different landing page depending on this value:
 * <ul>
 *   <li>{@link #SUPER_ADMIN} - full access, including user & role management</li>
 *   <li>{@link #ADMIN} - master data & reports, no user management</li>
 *   <li>{@link #USER} - cashier: point-of-sale screen only</li>
 * </ul>
 */
public enum Role {
    SUPER_ADMIN,
    ADMIN,
    USER
}

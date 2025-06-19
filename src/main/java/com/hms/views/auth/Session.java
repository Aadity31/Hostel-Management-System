package com.hms.views.auth;

/** Very simple holder for the currently‑logged‑in student. */
public final class Session {
    private static int loggedInStudentId = -1;

    private Session() {}  // no instances

    public static void setLoggedInStudentId(int id) {
        loggedInStudentId = id;
    }

    public static int getLoggedInStudentId() {
        return loggedInStudentId;
    }

    public static boolean isLoggedIn() {
        return loggedInStudentId > 0;
    }

    public static void clear() {
        loggedInStudentId = -1;
    }
}

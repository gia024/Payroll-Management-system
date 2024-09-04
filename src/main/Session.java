package main;

public class Session {
    private static String loggedInUsername;
    private static String role;

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    public static void setLoggedInUsername(String username) {
        loggedInUsername = username;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String userRole) {
        role = userRole;
    }
}

package com.toll.cli;

import com.toll.models.Admin;
import com.toll.services.AdminService;

public class AdminCLI {
    private final AdminService adminService;
    private final ConsoleReader reader;

    public AdminCLI(AdminService adminService, ConsoleReader reader) {
        this.adminService = adminService;
        this.reader = reader;
    }

    public void displayMenu() {
        System.out.println("\n--- [1] Admin Login & Session Management ---");
        if (adminService.isLoggedIn()) {
            System.out.println(" 1. Logout Current User (" + adminService.getCurrentUser().getUsername() + ")");
            System.out.println(" 2. Create Another Admin User");
            System.out.println(" 3. Return to Main Menu");
            int c = reader.readInt(" Choice: ", 1, 3);
            if (c == 1) {
                adminService.logout();
                System.out.println(" [✓] Successfully logged out.");
            } else if (c == 2) {
                String u = reader.readNonEmptyString(" New Admin Username: ", "Username");
                String p = reader.readNonEmptyString(" New Admin Password: ", "Password");
                String name = reader.readNonEmptyString(" Full Name: ", "Full Name");
                adminService.createAdmin(u, p, name, Admin.UserRole.ADMIN);
                System.out.println(" [✓] Admin user '" + u + "' created successfully.");
            }
        } else {
            System.out.println(" (Default Demo Admin credentials: username='admin', password='admin123')");
            String username = reader.readNonEmptyString(" Username: ", "Username");
            String password = reader.readNonEmptyString(" Password: ", "Password");
            if (adminService.login(username, password)) {
                System.out.println(" [✓] Login successful! Welcome, " + adminService.getCurrentUser().getFullName() + ".");
            } else {
                System.out.println(" [X] Login failed: Invalid username or password.");
            }
        }
        reader.pressEnterToContinue();
    }
}

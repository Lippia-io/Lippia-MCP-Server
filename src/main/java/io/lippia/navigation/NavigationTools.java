package io.lippia.navigation;

import com.crowdar.driver.DriverManager;

public class NavigationTools {
    public static void main(String[] args) {
        System.out.println("Navigation Tools Initialized");

        DriverManager.getDriverInstance().get("https://www.google.com");
    }
}

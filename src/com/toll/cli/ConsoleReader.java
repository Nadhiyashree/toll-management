package com.toll.cli;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConsoleReader {
    private final Scanner scanner;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ConsoleReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readString(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine().trim();
    }

    public String readNonEmptyString(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                throw new NoSuchElementException("End of input stream encountered.");
            }
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println(" [!] " + fieldName + " cannot be empty. Please try again.");
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                throw new NoSuchElementException("End of input stream encountered.");
            }
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.println(" [!] Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println(" [!] Invalid integer input. Please enter a valid number.");
            }
        }
    }

    public BigDecimal readBigDecimal(String prompt, BigDecimal min, BigDecimal max) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                throw new NoSuchElementException("End of input stream encountered.");
            }
            String input = scanner.nextLine().trim();
            try {
                BigDecimal val = new BigDecimal(input);
                if (min != null && val.compareTo(min) < 0) {
                    System.out.println(" [!] Amount must be at least Rs. " + min + ".");
                    continue;
                }
                if (max != null && val.compareTo(max) > 0) {
                    System.out.println(" [!] Amount must not exceed Rs. " + max + ".");
                    continue;
                }
                return val;
            } catch (Exception e) {
                System.out.println(" [!] Invalid monetary amount. Please enter a valid number (e.g. 150.00).");
            }
        }
    }

    public LocalDate readDate(String prompt, boolean allowDefaultToday) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return allowDefaultToday ? LocalDate.now() : null;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return allowDefaultToday ? LocalDate.now() : null;
            }
            try {
                return LocalDate.parse(input, dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println(" [!] Invalid date format. Please use format YYYY-MM-DD (e.g., 2026-10-25).");
            }
        }
    }

    public <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass) {
        T[] constants = enumClass.getEnumConstants();
        System.out.println("\n--- Select " + enumClass.getSimpleName() + " ---");
        for (int i = 0; i < constants.length; i++) {
            System.out.println(" [" + (i + 1) + "] " + constants[i].name());
        }
        int choice = readInt(prompt, 1, constants.length);
        return constants[choice - 1];
    }

    public void pressEnterToContinue() {
        System.out.print("\n Press [ENTER] to return...");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
}

package com.example.shortener;

import com.example.shortener.config.AppConfig;
import com.example.shortener.exception.AppException;
import com.example.shortener.model.ShortLink;
import com.example.shortener.repository.InMemoryLinkRepository;
import com.example.shortener.repository.LinkRepository;
import com.example.shortener.service.CleanupService;
import com.example.shortener.service.LinkService;
import com.example.shortener.service.NotificationService;
import com.example.shortener.util.GeneratorUtil;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static String currentUserId = null;

    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        LinkRepository repository = new InMemoryLinkRepository();
        NotificationService notificationService = new NotificationService();
        LinkService linkService = new LinkService(repository, config, notificationService);
        CleanupService cleanupService = new CleanupService(repository, notificationService, config);
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Link Shortener ===");

        while (true) {
            if (currentUserId == null) {
                System.out.print("> ");
            } else {
                System.out.print("> (" + currentUserId.substring(0, 8) + ") ");
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            String[] parts = input.split("\\s+");
            String cmd = parts[0].toLowerCase();

            try {
                if (cmd.equals("exit")) {
                    cleanupService.stop();
                    System.out.println("Выход.");
                    break;
                }
                
                if (currentUserId == null) {
                    currentUserId = GeneratorUtil.generateUUID();
                    System.out.println("Сгенерирован новый UUID пользователя: " + currentUserId);
                }

                if (cmd.equals("help")) {
                    printHelp(config);
                } 
                else if (cmd.equals("shorten") && parts.length >= 2) {
                    Integer limit = parts.length == 3 ? Integer.parseInt(parts[2]) : null;
                    ShortLink link = linkService.createLink(parts[1], currentUserId, limit);
                    System.out.println("Короткая ссылка: " + config.getBaseUrl() + link.getShortCode());
                } 
                else if (cmd.equals("go") && parts.length == 2) {
                    String originalUrl = linkService.resolveLink(parts[1], currentUserId);
                    System.out.println("Перенаправление на: " + originalUrl);
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(new URI(originalUrl));
                        } else {
                            System.out.println("Открытие браузера не поддерживается в данной среде.");
                        }
                    } catch (Exception e) {
                        System.out.println("Не удалось открыть браузер автоматически.");
                    }
                } 
                else if (cmd.equals("list")) {
                    List<ShortLink> links = repository.findByOwner(currentUserId);
                    if (links.isEmpty()) {
                        System.out.println("У вас нет созданных ссылок.");
                    } else {
                        for (ShortLink l : links) {
                            System.out.printf("%s -> %s | Клики: %d/%d | Живет до: %s\n", 
                                l.getShortCode(), l.getOriginalUrl(), l.getCurrentClicks(), l.getMaxClicks(),
                                new java.util.Date(l.getCreatedAtMillis() + l.getTtlMillis()));
                        }
                    }
                }
                else if (cmd.equals("edit") && parts.length == 3) {
                    linkService.editLimit(parts[1], currentUserId, Integer.parseInt(parts[2]));
                    System.out.println("Лимит обновлен.");
                }
                else if (cmd.equals("delete") && parts.length == 2) {
                    linkService.deleteLink(parts[1], currentUserId);
                    System.out.println("Ссылка удалена.");
                }
                else if (cmd.equals("notifications")) {
                    List<String> notes = notificationService.getAndClearNotifications(currentUserId);
                    if (notes.isEmpty()) {
                        System.out.println("Нет новых уведомлений.");
                    } else {
                        for (String note : notes) {
                            System.out.println("[УВЕДОМЛЕНИЕ] " + note);
                        }
                    }
                }
                else {
                    System.out.println("Неизвестная команда. Введите help.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Ошибка: ожидается число.");
            } catch (AppException e) {
                System.err.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Системная ошибка: " + e.getMessage());
            }
        }
    }

    private static void printHelp(AppConfig config) {
        System.out.println("Доступные команды:");
        System.out.println("shorten <url> [limit] - Сократить ссылку (лимит по умолчанию: " + config.getDefaultMaxClicks() + ")");
        System.out.println("go <shortCode> - Перейти по ссылке");
        System.out.println("list - Показать ваши ссылки");
        System.out.println("edit <shortCode> <newLimit> - Изменить лимит");
        System.out.println("delete <shortCode> - Удалить ссылку");
        System.out.println("notifications - Проверить уведомления");
        System.out.println("help - Справка");
        System.out.println("exit - Выход");
    }
}

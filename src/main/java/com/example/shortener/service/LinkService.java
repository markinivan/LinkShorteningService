package com.example.shortener.service;
import com.example.shortener.config.AppConfig;
import com.example.shortener.exception.AppException;
import com.example.shortener.model.ShortLink;
import com.example.shortener.repository.LinkRepository;
import com.example.shortener.util.GeneratorUtil;
import java.net.URI;
import java.net.URL;

public class LinkService {
    private final LinkRepository repository;
    private final AppConfig config;
    private final NotificationService notificationService;

    public LinkService(LinkRepository repository, AppConfig config, NotificationService notificationService) {
        this.repository = repository;
        this.config = config;
        this.notificationService = notificationService;
    }

    public ShortLink createLink(String originalUrl, String ownerId, Integer customLimit) {
        try {
            new URL(originalUrl).toURI();
        } catch (Exception e) {
            throw new AppException("Некорректный URL.");
        }

        int limit = (customLimit != null) ? customLimit : config.getDefaultMaxClicks();
        if (limit <= 0) throw new AppException("Лимит должен быть больше нуля.");

        String shortCode;
        do {
            shortCode = GeneratorUtil.generateShortCode(8);
        } while (repository.findByCode(shortCode) != null);

        long ttl = config.getDefaultTtlMinutes() * 60L * 1000L;
        ShortLink link = new ShortLink(shortCode, originalUrl, ownerId, limit, ttl);
        repository.save(link);
        return link;
    }

    public String resolveLink(String shortCode, String userId) {
        ShortLink link = repository.findByCode(shortCode);
        if (link == null) throw new AppException("Ссылка не найдена.");

        if (link.isExpired()) {
            repository.deleteByCode(shortCode);
            notificationService.notifyUser(link.getOwnerId(), "Ссылка " + shortCode + " удалена по истечении времени жизни.");
            throw new AppException("Время жизни ссылки истекло.");
        }

        if (link.isLimitReached()) {
            notificationService.notifyUser(link.getOwnerId(), "Исчерпан лимит переходов для ссылки " + shortCode + ".");
            throw new AppException("Лимит переходов исчерпан.");
        }

        link.setCurrentClicks(link.getCurrentClicks() + 1);
        repository.save(link);

        if (link.isLimitReached()) {
            notificationService.notifyUser(link.getOwnerId(), "Достигнут лимит переходов для ссылки " + shortCode + ".");
        }

        return link.getOriginalUrl();
    }

    public void editLimit(String shortCode, String userId, int newLimit) {
        ShortLink link = repository.findByCode(shortCode);
        if (link == null) throw new AppException("Ссылка не найдена.");
        if (!link.getOwnerId().equals(userId)) throw new AppException("Нет прав на редактирование этой ссылки.");
        if (newLimit <= 0) throw new AppException("Лимит должен быть больше нуля.");
        
        link.setMaxClicks(newLimit);
        repository.save(link);
    }

    public void deleteLink(String shortCode, String userId) {
        ShortLink link = repository.findByCode(shortCode);
        if (link == null) throw new AppException("Ссылка не найдена.");
        if (!link.getOwnerId().equals(userId)) throw new AppException("Нет прав на удаление этой ссылки.");
        
        repository.deleteByCode(shortCode);
    }
}

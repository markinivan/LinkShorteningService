package com.example.shortener.service;
import com.example.shortener.config.AppConfig;
import com.example.shortener.model.ShortLink;
import com.example.shortener.repository.LinkRepository;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CleanupService {
    private final LinkRepository repository;
    private final NotificationService notificationService;
    private final ScheduledExecutorService scheduler;

    public CleanupService(LinkRepository repository, NotificationService notificationService, AppConfig config) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        int interval = config.getCleanupIntervalSeconds();
        scheduler.scheduleAtFixedRate(this::cleanup, interval, interval, TimeUnit.SECONDS);
    }

    private void cleanup() {
        for (ShortLink link : repository.findAll()) {
            if (link.isExpired()) {
                repository.deleteByCode(link.getShortCode());
                notificationService.notifyUser(link.getOwnerId(), "Ссылка " + link.getShortCode() + " автоматически удалена по истечении TTL.");
            }
        }
    }

    public void stop() {
        scheduler.shutdown();
    }
}

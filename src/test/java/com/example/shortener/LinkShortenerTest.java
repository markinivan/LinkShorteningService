package com.example.shortener;

import com.example.shortener.config.AppConfig;
import com.example.shortener.exception.AppException;
import com.example.shortener.model.ShortLink;
import com.example.shortener.repository.InMemoryLinkRepository;
import com.example.shortener.repository.LinkRepository;
import com.example.shortener.service.LinkService;
import com.example.shortener.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LinkShortenerTest {
    private LinkService linkService;
    private LinkRepository repository;
    private NotificationService notificationService;
    private AppConfig config;
    private final String user1 = "user-1";
    private final String user2 = "user-2";

    @BeforeEach
    void setUp() {
        repository = new InMemoryLinkRepository();
        notificationService = new NotificationService();
        config = new AppConfig();
        linkService = new LinkService(repository, config, notificationService);
    }

    @Test
    void testCreateLinkSuccess() {
        ShortLink link = linkService.createLink("https://google.com", user1, 10);
        assertNotNull(link);
        assertEquals(user1, link.getOwnerId());
    }

    @Test
    void testUniqueLinksForDifferentUsers() {
        ShortLink link1 = linkService.createLink("https://google.com", user1, 10);
        ShortLink link2 = linkService.createLink("https://google.com", user2, 10);
        assertNotEquals(link1.getShortCode(), link2.getShortCode());
    }

    @Test
    void testUniqueLinksForSameUser() {
        ShortLink link1 = linkService.createLink("https://google.com", user1, 10);
        ShortLink link2 = linkService.createLink("https://google.com", user1, 10);
        assertNotEquals(link1.getShortCode(), link2.getShortCode());
    }

    @Test
    void testResolveAndClickCount() {
        ShortLink link = linkService.createLink("https://google.com", user1, 10);
        linkService.resolveLink(link.getShortCode(), user1);
        ShortLink updated = repository.findByCode(link.getShortCode());
        assertEquals(1, updated.getCurrentClicks());
    }

    @Test
    void testLimitReachedBlock() {
        ShortLink link = linkService.createLink("https://google.com", user1, 1);
        linkService.resolveLink(link.getShortCode(), user1);
        assertThrows(AppException.class, () -> linkService.resolveLink(link.getShortCode(), user1));
    }

    @Test
    void testLimitReachedNotification() {
        ShortLink link = linkService.createLink("https://google.com", user1, 1);
        linkService.resolveLink(link.getShortCode(), user1);
        List<String> notes = notificationService.getAndClearNotifications(user1);
        assertFalse(notes.isEmpty());
    }

    @Test
    void testEditLimitByOwner() {
        ShortLink link = linkService.createLink("https://google.com", user1, 10);
        linkService.editLimit(link.getShortCode(), user1, 5);
        assertEquals(5, repository.findByCode(link.getShortCode()).getMaxClicks());
    }

    @Test
    void testEditLimitByStrangerFails() {
        ShortLink link = linkService.createLink("https://google.com", user1, 10);
        assertThrows(AppException.class, () -> linkService.editLimit(link.getShortCode(), user2, 5));
    }

    @Test
    void testDeleteByOwner() {
        ShortLink link = linkService.createLink("https://google.com", user1, 10);
        linkService.deleteLink(link.getShortCode(), user1);
        assertNull(repository.findByCode(link.getShortCode()));
    }

    @Test
    void testDeleteByStrangerFails() {
        ShortLink link = linkService.createLink("https://google.com", user1, 10);
        assertThrows(AppException.class, () -> linkService.deleteLink(link.getShortCode(), user2));
    }

    @Test
    void testInvalidUrlThrowsError() {
        assertThrows(AppException.class, () -> linkService.createLink("not-a-url", user1, 10));
    }

    @Test
    void testInvalidLimitThrowsError() {
        assertThrows(AppException.class, () -> linkService.createLink("https://google.com", user1, -5));
    }

    @Test
    void testExpiredLinkResolution() throws InterruptedException {
        ShortLink link = linkService.createLink("https://google.com", user1, 10);
        link.setCreatedAtMillis(System.currentTimeMillis() - link.getTtlMillis() - 1000);
        repository.save(link);
        assertThrows(AppException.class, () -> linkService.resolveLink(link.getShortCode(), user1));
    }

    @Test
    void testListLinksByUser() {
        linkService.createLink("https://google.com", user1, 10);
        linkService.createLink("https://yahoo.com", user1, 10);
        linkService.createLink("https://bing.com", user2, 10);
        
        List<ShortLink> user1Links = repository.findByOwner(user1);
        assertEquals(2, user1Links.size());
    }

    @Test
    void testResolveMissingLink() {
        assertThrows(AppException.class, () -> linkService.resolveLink("noSuchCode", user1));
    }
}

package com.example.shortener.repository;
import com.example.shortener.model.ShortLink;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryLinkRepository implements LinkRepository {
    private final Map<String, ShortLink> links = new ConcurrentHashMap<>();

    @Override
    public void save(ShortLink link) { links.put(link.getShortCode(), link); }

    @Override
    public ShortLink findByCode(String shortCode) { return links.get(shortCode); }

    @Override
    public void deleteByCode(String shortCode) { links.remove(shortCode); }

    @Override
    public List<ShortLink> findByOwner(String ownerId) {
        return links.values().stream()
                .filter(l -> l.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ShortLink> findAll() { return new ArrayList<>(links.values()); }
}

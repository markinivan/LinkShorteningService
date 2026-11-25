package com.example.shortener.repository;
import com.example.shortener.model.ShortLink;
import java.util.List;

public interface LinkRepository {
    void save(ShortLink link);
    ShortLink findByCode(String shortCode);
    void deleteByCode(String shortCode);
    List<ShortLink> findByOwner(String ownerId);
    List<ShortLink> findAll();
}

package com.kutay.scraper.db.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kutay.scraper.db.entity.site.Site;

public interface SiteRepo extends JpaRepository<Site, Long> {
        Optional<Site> findByName(String name);
}

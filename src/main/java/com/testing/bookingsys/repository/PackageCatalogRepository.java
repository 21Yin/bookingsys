package com.testing.bookingsys.repository;

import com.testing.bookingsys.entity.PackageCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageCatalogRepository extends JpaRepository<PackageCatalog, Long> {
    List<PackageCatalog> findByCountryCodeIgnoreCaseAndActiveTrue(String countryCode);
}

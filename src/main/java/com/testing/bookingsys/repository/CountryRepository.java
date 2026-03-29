package com.testing.bookingsys.repository;

import com.testing.bookingsys.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByCodeIgnoreCase(String code);
}

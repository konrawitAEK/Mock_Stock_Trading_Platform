package com.mockstock.repository;

import com.mockstock.model.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, String> {}

package com.mockstock.repository;

import com.mockstock.model.UserState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStateRepository extends JpaRepository<UserState, Long> {}

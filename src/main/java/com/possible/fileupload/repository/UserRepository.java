package com.possible.fileupload.repository;

import com.possible.fileupload.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
}

package com.tuyenvp.spring_boot_app.Repository;

import com.tuyenvp.spring_boot_app.Model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpenAIRepository extends JpaRepository<ApiKey, Long> {
}

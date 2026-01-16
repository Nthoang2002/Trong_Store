package com.tuyenvp.spring_boot_app.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "api_key")
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApiKey {
    @Id
    private Long id;
    private String apiKey;
}

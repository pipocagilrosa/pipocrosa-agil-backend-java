package br.com.pipocarosa.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateDto(
        @NotBlank @Size(min = 6, max = 20) String password
) {
}

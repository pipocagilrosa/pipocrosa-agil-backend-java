package br.com.pipocarosa.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserUpdateDto(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$") String birthDate
) {
}

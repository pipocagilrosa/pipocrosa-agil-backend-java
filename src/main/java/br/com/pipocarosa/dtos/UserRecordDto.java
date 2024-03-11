package br.com.pipocarosa.dtos;

import jakarta.validation.constraints.*;

public record UserRecordDto(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$") String birthDate,
        @NotBlank @Size(min = 6, max = 20) String password
    ) {

}

package br.com.pipocarosa.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@Setter
@Getter
@Entity(name = "USERS")
public class UserModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true)
    private Long id;
    @Column(unique = true, nullable = false)
    private UUID uuid;
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String birthDate;
    @Column(nullable = false)
    private String password;

    public UserModel(Long id, String name, String email, String birthDate, String password){
        this.id = id;
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.birthDate = birthDate;
        this.password = password;
    }

    public UserModel() {
        this.uuid = UUID.randomUUID();
    }
}

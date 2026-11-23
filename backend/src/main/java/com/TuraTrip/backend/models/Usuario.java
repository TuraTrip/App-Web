package com.TuraTrip.backend.models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 255)
    private String correo;

    @Column(nullable = false, length = 255)
    private String clave;

    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil;

    /** Frase corta del perfil, p. ej. "Explorando Colombia y el mundo". */
    @Column(length = 160)
    private String biografia;

    /** Fin de la suscripción Premium; null o en el pasado = cuenta normal. */
    @Column(name = "premium_hasta")
    private LocalDateTime premiumHasta;

    /** Perfil verificado: tiene una suscripción Premium vigente. No es una columna. */
    public boolean isVerificado() {
        return premiumHasta != null && premiumHasta.isAfter(LocalDateTime.now());
    }

    @Builder.Default
    @Column(nullable = false)
    private Boolean habilitado = false;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    // Nueva Relación: Un usuario tiene muchas publicaciones
    @Builder.Default
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Publicacion> publicaciones = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creado;

    @UpdateTimestamp
    private LocalDateTime actualizado;
}

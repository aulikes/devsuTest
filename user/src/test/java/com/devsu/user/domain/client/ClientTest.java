package com.devsu.user.domain.client;

import com.devsu.user.domain.person.Gender;
import com.devsu.user.domain.person.IdentificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientTest {

    private static final LocalDate BIRTH = LocalDate.of(1990, 1, 15);

    // ---------- create(...) ----------
    @Test
    @DisplayName("create: construye un cliente nuevo activo, con clientId trim y password intacto")
    void create_builds_active_with_trimmed_clientId_and_kept_password() {
        var c = Client.create(
                "John", "Doe",
                Gender.MALE, BIRTH, IdentificationType.CC,
                "900123", "  Main St 123  ", "  3001112222  ",
                "  C-001  ", "  secret  "
        );

        // Ids de nuevo (dominio y persona) son null en create()
        assertThat(c.getId()).isNull();
        assertThat(c.getIdPersona()).isNull();

        // Valores básicos
        assertThat(c.getFirstName()).isEqualTo("John");
        assertThat(c.getLastName()).isEqualTo("Doe");
        assertThat(c.getGender()).isEqualTo(Gender.MALE);
        assertThat(c.getBirthDate()).isEqualTo(BIRTH);
        assertThat(c.getIdentificationType()).isEqualTo(IdentificationType.CC);
        assertThat(c.getIdentificationNumber()).isEqualTo("900123");

        // Person hace trim en address/phone
        assertThat(c.getAddress()).isEqualTo("Main St 123");
        assertThat(c.getPhone()).isEqualTo("3001112222");

        // Client hace trim en clientId, y NO trimea password (se guarda tal cual)
        assertThat(c.getClientId()).isEqualTo("C-001");
        assertThat(c.getPassword()).isEqualTo("  secret  ");

        // Nuevo cliente queda activo
        assertThat(c.isActive()).isTrue();
    }

    @Test
    @DisplayName("create: rechaza password null o en blanco")
    void create_rejects_null_or_blank_password() {
        assertThrows(IllegalArgumentException.class, () ->
                Client.create("A","B", Gender.MALE, BIRTH, IdentificationType.CC,
                        "1","Addr","300","C-1", null));

        assertThrows(IllegalArgumentException.class, () ->
                Client.create("A","B", Gender.MALE, BIRTH, IdentificationType.CC,
                        "1","Addr","300","C-1", "   "));
    }

    // ---------- fromPersistence(...) ----------
    @Test
    @DisplayName("fromPersistence: requiere id e idPersona; conserva valores y respeta trim en clientId")
    void fromPersistence_requires_ids_and_sets_fields() {
        var c = Client.fromPersistence(
                10L, 20L,
                "Ana", "Gomez",
                Gender.FEMALE, LocalDate.of(2000, 2, 29),
                IdentificationType.CE, "1010",
                "Calle 1", "3000000000",
                "  C-XYZ  ", "hashed", false
        );

        assertThat(c.getId()).isEqualTo(10L);
        assertThat(c.getIdPersona()).isEqualTo(20L);
        assertThat(c.getClientId()).isEqualTo("C-XYZ");
        assertThat(c.getPassword()).isEqualTo("hashed");
        assertThat(c.isActive()).isFalse();
    }

    @Test
    @DisplayName("fromPersistence: lanza IllegalArgumentException si id o idPersona son null")
    void fromPersistence_throws_when_ids_missing() {
        assertThrows(IllegalArgumentException.class, () ->
                Client.fromPersistence(null, 1L, "A","B", Gender.MALE, BIRTH,
                        IdentificationType.CC, "1", "Addr","300","C-1","x", true));

        assertThrows(IllegalArgumentException.class, () ->
                Client.fromPersistence(1L, null, "A","B", Gender.MALE, BIRTH,
                        IdentificationType.CC, "1", "Addr","300","C-1","x", true));
    }

    // ---------- Mutadores propios ----------
    @Nested
    @DisplayName("Password")
    class PasswordTests {

        @Test
        @DisplayName("setPassword: acepta no vacías y conserva el valor tal cual (sin trim)")
        void setPassword_accepts_non_empty_and_keeps_value() {
            var c = Client.create("J","D", Gender.MALE, BIRTH, IdentificationType.CC,
                    "1","Addr","300","C-1","x");

            c.setPassword("  newHash  ");
            assertThat(c.getPassword()).isEqualTo("  newHash  ");
        }

        @Test
        @DisplayName("setPassword: rechaza null y en blanco")
        void setPassword_rejects_null_and_blank() {
            var c = Client.create("J","D", Gender.MALE, BIRTH, IdentificationType.CC,
                    "1","Addr","300","C-1","x");

            assertThrows(IllegalArgumentException.class, () -> c.setPassword(null));
            assertThrows(IllegalArgumentException.class, () -> c.setPassword("   "));
        }
    }

    @Nested
    @DisplayName("Activación")
    class ActivationTests {
        @Test
        @DisplayName("activate/deactivate alternan el estado activo")
        void activation_toggles() {
            var c = Client.create("J","D", Gender.MALE, BIRTH, IdentificationType.CC,
                    "1","Addr","300","C-1","pwd");

            assertThat(c.isActive()).isTrue();
            c.deactivate();
            assertThat(c.isActive()).isFalse();
            c.activate();
            assertThat(c.isActive()).isTrue();
        }
    }

    // ---------- Inmutabilidad de id y clientId ----------
    @Test
    @DisplayName("id y clientId son inmutables")
    void id_and_clientId_are_immutable() {
        var c = Client.fromPersistence(99L, 77L, "A","B", Gender.MALE, BIRTH,
                IdentificationType.CC, "1", "Addr", "300", "C-IMM", "hash", true);

        c.deactivate();
        c.setPassword("other");
        c.activate();

        assertThat(c.getId()).isEqualTo(99L);
        assertThat(c.getClientId()).isEqualTo("C-IMM");
    }
}

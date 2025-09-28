package com.devsu.transaction.infrastructure.generator;

import com.devsu.transaction.application.port.AccountNumberGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Implementación que genera números de cuenta de 12 dígitos con check digit Luhn.
 * - Los primeros 11 dígitos son aleatorios (el primero no es cero).
 * - El último dígito es el verificador Luhn.
 * - No verifica unicidad; se confía en la restricción única de la base de datos.
 */
@Component
public class LuhnAccountNumberGenerator implements AccountNumberGenerator {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int BODY_LENGTH = 11; // 11 + 1 (check) = 12 dígitos

    @Override
    public String generate() {
        int[] body = new int[BODY_LENGTH];

        // Se asegura que el primer dígito no sea cero
        body[0] = 1 + RNG.nextInt(9);
        for (int i = 1; i < BODY_LENGTH; i++) {
            body[i] = RNG.nextInt(10);
        }

        int check = luhnCheckDigit(body);

        StringBuilder sb = new StringBuilder(BODY_LENGTH + 1);
        for (int d : body) sb.append(d);
        sb.append(check);
        return sb.toString();
    }

    /**
     * Calcula el dígito verificador Luhn para el arreglo de dígitos provisto.
     * Se aplica la regla de duplicar en posiciones alternas desde la izquierda,
     * dependiendo de la paridad de la longitud.
     */
    private static int luhnCheckDigit(int[] digits) {
        int sum = 0;
        int parity = digits.length % 2;
        for (int i = 0; i < digits.length; i++) {
            int d = digits[i];
            if (i % 2 == parity) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
        }
        return (10 - (sum % 10)) % 10;
    }
}

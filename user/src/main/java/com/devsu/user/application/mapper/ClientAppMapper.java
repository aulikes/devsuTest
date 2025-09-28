package com.devsu.user.application.mapper;

import com.devsu.user.application.command.UpdateClientCommand;
import com.devsu.user.application.result.ClientResult;
import com.devsu.user.domain.client.Client;
import com.devsu.user.domain.person.Gender;
import com.devsu.user.domain.person.IdentificationType;

/**
 * Mapper de la capa application que traduce entre command/result y el dominio.
 * Esta clase centraliza conversiones a enums del dominio y armado de resultados.
 */
public final class ClientAppMapper {

    // Se evita instanciación
    private ClientAppMapper() {}

    // --------- update ---------
    // Se aplica el UpdateClientCommand sobre una entidad de dominio existente
    public static void applyUpdate(Client target, UpdateClientCommand cmd, String maybeNewHashedPassword) {
        // Se actualizan campos de persona
        target.setFirstName(cmd.firstName());
        target.setLastName(cmd.lastName());
        target.setGender(Gender.valueOf(cmd.gender().toUpperCase()));
        target.setBirthDate(cmd.birthDate());
        target.setIdentificationType(IdentificationType.valueOf(cmd.identificationType().toUpperCase()));
        target.setIdentificationNumber(cmd.identificationNumber());
        target.setAddress(cmd.address());
        target.setPhone(cmd.phone());

        if (maybeNewHashedPassword != null && !maybeNewHashedPassword.isBlank()) {
            target.setPassword(maybeNewHashedPassword); // ya viene hasheado
        }
    }

    // --------- result ---------
    // Se convierte Client → ClientResult
    public static ClientResult toResult(Client client) {
        return new ClientResult(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getGender().name(),
                client.getBirthDate(),
                client.getIdentificationType().name(),
                client.getIdentificationNumber(),
                client.getAddress(),
                client.getPhone(),
                client.getClientId(),
                client.isActive()
        );
    }
}

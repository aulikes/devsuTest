package com.devsu.transaction.infrastructure.web.mappers;

import com.devsu.transaction.application.command.CreateAccountCommand;
import com.devsu.transaction.application.result.AccountResult;
import com.devsu.transaction.infrastructure.web.dto.AccountResponse;
import com.devsu.transaction.infrastructure.web.dto.CreateAccountRequest;
import org.mapstruct.Mapper;

/**
 * Mapper de la capa web. Interfaz de MapStruct.
 * Se mantiene sin lógica, solo proyecciones entre DTOs.
 */
@Mapper(componentModel = "spring")
public interface AccountWebMapper {

    CreateAccountCommand toCommand(CreateAccountRequest req);

    // Se mapea el result de aplicación al response web
    AccountResponse toResponse(AccountResult result);
}

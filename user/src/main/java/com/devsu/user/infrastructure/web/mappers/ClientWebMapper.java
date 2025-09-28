package com.devsu.user.infrastructure.web.mappers;

import com.devsu.user.application.command.CreateClientCommand;
import com.devsu.user.application.command.UpdateClientCommand;
import com.devsu.user.application.result.ClientResult;
import com.devsu.user.infrastructure.web.dto.ClientResponse;
import com.devsu.user.infrastructure.web.dto.CreateClientRequest;
import com.devsu.user.infrastructure.web.dto.UpdateClientRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ClientWebMapper {

    CreateClientCommand toCommand(CreateClientRequest request);

    UpdateClientCommand toCommand(Long id, UpdateClientRequest request);

    ClientResponse toResponse(ClientResult result);
}

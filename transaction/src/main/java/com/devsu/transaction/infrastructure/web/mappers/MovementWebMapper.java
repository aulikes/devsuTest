package com.devsu.transaction.infrastructure.web.mappers;

import com.devsu.transaction.application.command.CreateMovementCommand;
import com.devsu.transaction.application.result.MovementResult;
import com.devsu.transaction.infrastructure.web.dto.CreateMovementRequest;
import com.devsu.transaction.infrastructure.web.dto.MovementResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovementWebMapper {

    CreateMovementCommand toCommand(CreateMovementRequest request);

    MovementResponse toResponse(MovementResult result);
}

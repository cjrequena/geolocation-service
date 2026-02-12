package com.cjrequena.sample.service.command;


import com.cjrequena.sample.domain.model.Domain;
import com.cjrequena.sample.domain.model.command.Command;
import com.cjrequena.sample.domain.model.command.CreateGeoShapeCommand;
import jakarta.annotation.Nonnull;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
public class CreateGeoShapeCommandHandler extends CommandHandler<CreateGeoShapeCommand> {


  @Override
  public Domain handle(@Nonnull Command command) {
    log.trace("Handling command of type {}", command.getClass().getSimpleName());

    if (!(command instanceof CreateGeoShapeCommand)) {
      throw new IllegalArgumentException("Expected command of type CreateGeoShapeCommand but received " + command.getClass().getSimpleName());
    }

    log.info("Successfully handled command {} for domain with ID {}", command.getClass().getSimpleName(), command.getDomainId());
    return null;
  }

  @Nonnull
  @Override
  public Class<CreateGeoShapeCommand> getCommandType() {
    return CreateGeoShapeCommand.class;
  }

}

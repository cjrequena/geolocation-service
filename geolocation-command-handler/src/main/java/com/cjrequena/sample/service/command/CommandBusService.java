package com.cjrequena.sample.service.command;

import com.cjrequena.sample.domain.exception.CommandHandlerNotFoundException;
import com.cjrequena.sample.domain.model.Domain;
import com.cjrequena.sample.domain.model.command.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommandBusService {

  private final List<CommandHandler<? extends Command>> commandHandlers;


  public void handle(Command command) {
    log.info("Processing command {}", command);

    commandHandlers.stream()
      .filter(commandHandler -> commandHandler.getCommandType() == command.getClass())
      .findFirst()
      .ifPresentOrElse(commandHandler -> {
        log.info("Handling command {} with {}", command.getClass().getSimpleName(), commandHandler.getClass().getSimpleName());

        final Domain domain = commandHandler.handle(command);

      }, () -> {
        log.info("No specialized handler found with {}", command.getClass().getSimpleName());
        throw new CommandHandlerNotFoundException("No specialized handler found for command: " + command.getClass().getSimpleName());
      });
  }
}

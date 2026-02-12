package com.cjrequena.sample.service.command;

import com.cjrequena.sample.domain.exception.OptimisticConcurrencyException;
import com.cjrequena.sample.domain.model.Domain;
import com.cjrequena.sample.domain.model.command.Command;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Log4j2
public abstract class CommandHandler<T extends Command> {

  public abstract Domain handle(@Nonnull Command command) throws OptimisticConcurrencyException;

  @Nonnull
  public abstract Class<T> getCommandType();

}

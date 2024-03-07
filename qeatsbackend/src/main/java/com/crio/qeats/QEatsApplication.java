
/*
 *
 * <<<<<<< HEAD * Copyright (c) Crio.Do 2019. All rights reserved ======= * Copyright (c) Crio.Do
 * 2019. All rights reserved >>>>>>> dc8b0d3b93ef671e8dec85fa0588a5d535f28259
 *
 */

package com.crio.qeats;

import com.crio.qeats.globals.GlobalConstants;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
@Log4j2
@EnableCaching
public class QEatsApplication {

  public static void main(String[] args) {
    SpringApplication.run(QEatsApplication.class, args);

    // TIP:MODULE_RESTAPI: If your server starts successfully,
    // you can find the following message in the logs.
    log.info("Congrats! Your QEatsApplication server has started");
  }

  /**
   * Fetches a ModelMapper instance.
   *
   * @return ModelMapper
   */
  @Bean // Want a new obj every time
  @Scope("prototype")
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

}

package net.madz.download.service.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.madz.download.service.IService;
import net.madz.download.service.IServiceRequest;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface Services {

    Class<? extends IService<? extends IServiceRequest>>[] value();
}

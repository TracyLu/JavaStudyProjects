/**
 * 
 */
package net.madz.download.service.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.madz.download.service.IServiceRequest;

/**
 * @author tracy
 * 
 */
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Command {

    // Must be in order.
    Arg[] arguments();

    String commandName();

    String description() default "";

    Option[] options();

    Class<? extends IServiceRequest> request();
}

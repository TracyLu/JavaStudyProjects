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

	String name();

	Class<? extends IServiceRequest> request();

	//Must be in order.
	Arg[] arguments();

	Option[] options();

	String description() default "";

}

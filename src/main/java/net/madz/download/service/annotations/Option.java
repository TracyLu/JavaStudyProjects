package net.madz.download.service.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface Option {

	String shortName();

	String fullName();

	String description();
}

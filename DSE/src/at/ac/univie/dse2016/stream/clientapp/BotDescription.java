package at.ac.univie.dse2016.stream.clientapp;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BotDescription {
	String Description();
}


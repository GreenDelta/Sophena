package sophena.rcp.navigation.actions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sophena.rcp.navigation.NavigationElement;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Handler {

	Class<? extends NavigationElement> type();

	String title();

}

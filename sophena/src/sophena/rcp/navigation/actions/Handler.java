package sophena.rcp.navigation.actions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.SubFolderType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Handler {

	Class<? extends NavigationElement> type();

	/**
	 * When the titles should be translated we can use the message keys here:
	 * M.getMap().get([the message key]).
	 */
	String title();

	/**
	 * Only relevant if the type of the navigation element is a folder element.
	 */
	SubFolderType folderType() default SubFolderType.NONE;

}

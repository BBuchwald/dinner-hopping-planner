/**
 * Aufzählung von Zuständen des Routenberechnungsalgorithmus.
 * @author Björn Buchwald
 *
 */
public enum Modus{

	/**
	 * Finde Hauptspeiseort für Vorspeiseteam
	 */
	START_TO_MAIN, 
	/**
	 * Finde Nachspeiseort für Vorspeiseteam
	 */
	START_TO_DESSERT, 
	/**
	 * Finde Hauptspeiseort für Nachspeiseteam
	 */
	DESSERT_TO_MAIN, 
	/**
	 * Finde Vorspeiseort für Nachspeiseteam
	 */
	DESSERT_TO_STARTER

}
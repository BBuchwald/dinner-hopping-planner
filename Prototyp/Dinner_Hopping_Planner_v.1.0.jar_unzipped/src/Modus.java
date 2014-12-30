/**
 * Aufz�hlung von Zust�nden des Routenberechnungsalgorithmus.
 * @author Bj�rn Buchwald
 *
 */
public enum Modus{

	/**
	 * Finde Hauptspeiseort f�r Vorspeiseteam
	 */
	START_TO_MAIN, 
	/**
	 * Finde Nachspeiseort f�r Vorspeiseteam
	 */
	START_TO_DESSERT, 
	/**
	 * Finde Hauptspeiseort f�r Nachspeiseteam
	 */
	DESSERT_TO_MAIN, 
	/**
	 * Finde Vorspeiseort f�r Nachspeiseteam
	 */
	DESSERT_TO_STARTER

}
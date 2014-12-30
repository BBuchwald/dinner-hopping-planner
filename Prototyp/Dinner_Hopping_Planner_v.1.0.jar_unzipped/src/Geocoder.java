//Collections
import java.util.ArrayList;

/**
 * Interface des Geokodierers. Dient zum zuweisen von Geokoordinaten zu eingelesenen Adressen mit Hilfe eines Service,
 * außerdem können Adressen im Browser visualiert werden.
 * @author Björn Buchwald
 *
 */
public interface Geocoder {
	
	/**
	 * Abfrage des Servers des jeweiligen Services nach den Geokoordinaten aller Adressen die dem Konstruktor 
	 * übergeben werden. Werden mehrere mögliche Geokoordinaten gefunden, so kann die 
	 * passendste, durch Anzeige auf einer statischen Karte, ausgewählt werden.
	 * @return true Geokodierung erfolgreich, false sonst
	 */
	public boolean getGeocodes();
	
	/**
	 * Gibt alle Geoadressen mit Anschrift und Geokoordinaten zurück.
	 * @return Geoadressen
	 */
	public ArrayList<GeoAddress> getGeoAddrList();
	
	/**
	 * Gibt url zum Aufruf einer StaticMap mit allen Geocodierten Adressen zurück.
	 * @return CloudMade StaticMap Url
	 */
	public String getUrlGeoAddrStr();
	
	/**
	 * Erzeugt eine statische Karte aller kodierten Adressen der geoAddressList, um dem Nutzer die
	 * Möglichkeit zu geben diese zu evaluieren.
	 */
	public void showMapOfGeocodes();
	
	/**
	 * Label des aktuell verwendeten Geokodierers.
	 * @return String 
	 */
	public String getLabel();
}

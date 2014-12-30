//Collections
import java.util.ArrayList;

/**
 * Interface das eine Methode bereitstellt, um Distanzen von jeder geokodierten Adresse 
 * zu jeder anderen mit Hilfe verschiedener Dienste zu berechnen. 
 * @author Björn Buchwald
 *
 */
public interface DistanceCalculator{
	
	/**
	 * Berechnung von Distanzen von jeder geokodierten Adresse zu jeder anderen.
	 * @param geoAddressList
	 * @return Integer-Matrix mit Distanzen, null wenn Server (CloudMade) oder File (Osm2po) nicht erreichbar
	 */
	int[][] calculateDistances( ArrayList<GeoAddress> geoAddressList );
	
}
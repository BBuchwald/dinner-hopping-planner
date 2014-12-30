//Collections
import java.util.ArrayList;
//Exponent
import java.lang.Math;

/**
 * Berechnet die euklidische Distanz zwischen von jedem geokodierten Ort zu jedem anderen.
 * @author Björn Buchwald
 *
 */
class EuklidDistanceCalculator implements DistanceCalculator{
	
	/**
	 * Label dieses Distanzkalkulators.
	 */
	public String label = "Euklid Distance";

	/**
	 * Abstand zwischen zwei Breitengraden in Metern
	 */
	private static final int latLength = 111120; //in meter (60 NM = 60 * 1825 m)
	
	
	/**
	 * Nutzen der euklidischen Distanz (Luftlinie) als Entfernung zwischen zwei Veranstaltungsorten.
	 */
	public EuklidDistanceCalculator(){
		
	}
	
	/**
	 * Berechnung der Luftlinie zwischen zwei gegebenen Orten.
	 * @param lat1 Breitengrad des ersten Ortes
	 * @param lng1 Längengrad des ersten Ortes
	 * @param lat2 Breitengrad des zweiten Ortes
	 * @param lng2 Längengrad des zweiten Ortes
	 * @return double Distanz
	 */
	public static double getDistance(double lat1, double lng1, double lat2, double lng2){
		//double latMid = (lat1 + lat2) / 2 * (Math.PI /180); //choose a value that is in the middle of lat1 and lat2.  
															//convert to radiant.
															
		//use pythagoras (for small distances < 50 km there's no difference to spheric computation)
		//return Math.sqrt( Math.pow( latLength * (lat1 - lat2), 2.0 ) + Math.pow( latLength * Math.cos(latMid) * (lng1 - lng2), 2.0) );
		return Math.sqrt( Math.pow( latLength * (lat1 - lat2), 2.0 ) + Math.pow( 71500 * (lng1 - lng2), 2.0) );
	}
	
	/* (non-Javadoc)
	 * @see DistanceCalculator#calculateDistances(java.util.ArrayList)
	 */
	public int[][] calculateDistances( ArrayList<GeoAddress> geoAddressList ){
		
		System.out.println("-----------------Euklid distance calculation------------------------");
		System.out.println();
		
		int[][] distances = new int[geoAddressList.size()][geoAddressList.size()];
		int distance;
		
		for(int j = 0;j < geoAddressList.size(); j++){
		
			for(int i = 0; i < geoAddressList.size() ; i++){
				if(i <= j) continue;
			
				distance = (int) getDistance(geoAddressList.get(i).getLat(), geoAddressList.get(i).getLng(),geoAddressList.get(j).getLat(), geoAddressList.get(j).getLng());
				
				distances[j][i] = distance;
				distances[i][j] = distance;
				
			}
			
		}
		
		return distances;
	}

}
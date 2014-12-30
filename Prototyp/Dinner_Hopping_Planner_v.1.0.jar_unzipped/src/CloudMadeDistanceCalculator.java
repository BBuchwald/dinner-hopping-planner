//Collection
import java.util.ArrayList;
//Exception
import java.io.IOException;
//Lese Antwort
import java.io.InputStream;
//URL Handling
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
//CloudMade Routing
import com.cloudmade.api.CMClient;
import com.cloudmade.api.CMClient.MeasureUnit;
import com.cloudmade.api.CMClient.RouteType;
import com.cloudmade.api.geometry.Point;
import com.cloudmade.api.routing.Route;
import com.cloudmade.api.routing.RouteNotFoundException;

/**
 * Berechnet die Distanz von jedem geokodierten Ort zu jedem anderen mit Hilfe des CloudMade Dienstes.
 * Distanzen 
 * @author Björn Buchwald
 *
 */
class CloudMadeDistanceCalculator implements DistanceCalculator{
	
	/**
	 * routeType Bestimmt auf Grundlage welches Verkehrsmittels die Distanzbestimmung durchgeführt werden soll.
	 */
	private RouteType routeType;
	
	/**
	 * API-Key zur Nutzung der CloudMade Dienste notwendig.
	 */
	private String apiKey;
	
	/**
	 * Label dieses CloudMade-Dienstes.
	 */
	public String label = "CloudMade Distance";
	
	/**
	 * Berechnet die Distanz von jedem geokodierten Ort zu jedem anderen mit Hilfe des CloudMade-Dienstes.
	 * @param routeType Verkehrsmittel möglich sind CAR oder BICYCLE
	 * @param key API-Key zur Nutzung des CloudMade Dienstes.
	 */
	public CloudMadeDistanceCalculator( RouteType routeType, String key ){
		//this.routeType = RouteType.CAR;
		this.routeType = routeType;
		this.apiKey = key;
		
	}
	
	/**
	 * Bestimmt ob das Programm in einem Windows Betriebssystem ausgeführt wird.
	 * @return true wenn Windows vorliegt, false sonst.
	 */
	public static boolean isWindows() {
		String os = System.getProperty("os.name");
			if ( os != null && os.startsWith("Windows")) {
			return true;
		} else {
			return false;
		}
	}
	//http://routes.cloudmade.com:80/8ee2a50541944fb9bcedded5165f09d9/api/0.3/51.31988,12.3345,51.3344,12.33969/car.js?lang=de&units=km
	/* (non-Javadoc)
	 * @see DistanceCalculator#calculateDistances(java.util.ArrayList)
	 */
	public int[][] calculateDistances( ArrayList<GeoAddress> geoAddressList ){
		
		System.out.println("-------CloudMade distance calculation "+routeType.toString()+"-------");
		System.out.println();
		
		//Prüfe ob CloudMade Server erreichbar
		if( !proofCloudMadeDistanceServer() ){
			System.out.println("CloudMade Distance Server not available!");
			return null;
		}
		
		//CloudMade Cient Objekt mit API Key instanziieren
		CMClient client = new CMClient(this.apiKey);
		
		int[][] distances = new int[geoAddressList.size()][geoAddressList.size()];
		
		int distCounter = 0;
        int distSum = ( geoAddressList.size()* (geoAddressList.size()-1) ) / 2; //zu berechnende Distanzen
		
		//Distanz von jedem Ort zu jedem anderem
		for(int j = 0;j < geoAddressList.size(); j++){
		
		//Routing
		//test1: 100 Anfragen(mit selben Daten) in 47s, test2 : 27s, test3 : 27s
			for(int i = 0; i < geoAddressList.size() ; i++){
				
				if(i <= j) continue;
				try {
					distCounter++;
					
					Route route = client.route(
					new Point((double) geoAddressList.get(i).getLat(), (double) geoAddressList.get(i).getLng()),
					new Point((double) geoAddressList.get(j).getLat(), (double) geoAddressList.get(j).getLng()),
					//der Route Typ wird auf CAR bzw. BICYCLE gesetzt, um die Route per Auto bzw. per Fahrrad zu berechnen
					this.routeType,
					null,
					//RouteTypeModifier.SHORTEST,
					null,
					"de",
					MeasureUnit.KM
					);
					
					//Distanz der ermittelten Route in symmetrische Matrix schreiben
					distances[j][i] = (int) route.summary.totalDistance;
					distances[i][j] = (int) route.summary.totalDistance;
					//totalTime in sekunden (double)
					
				} catch (RouteNotFoundException e) {
					System.out.println("Route not found!");
					return null;
				}
				//Status
				System.out.println(distCounter+" of "+ distSum );
			}
		}
		
		System.out.println(distCounter+" of "+ distSum+". Finished!");
		
		return distances;
	}
	
	/**
	 * Erlaubt das ändern des Routentyps.
	 * @param routeType (CAR oder BICYCLE)
	 */
	public void setRouteType(RouteType routeType){
		this.routeType = routeType;
	}
	
	/**
	 * Prüft die Internetverbindung indem eine Distanz vom CloudMade Server 
	 * angefordert wird.
	 * @return true wenn eine positive Antwort zurückkam, false sonst
	 */
	public boolean proofCloudMadeDistanceServer(){
		
		URL url = null;
		InputStream inputStreamJson = null;
		int attempt = 0;
		//Request einer Distanz
		try{
			url = new URL( "http://routes.cloudmade.com:80/"+this.apiKey+"/api/0.3/51.31988,12.3345,51.3344,12.33969/car.js?lang=de&units=km");
		}catch(MalformedURLException mue){
			mue.printStackTrace();
		}
		//System.out.println(url.toString());
		//Verbindung herstellen (max. 3 Verbindungsversuche)
		while(attempt < 3){
			try{
				URLConnection con = url.openConnection();
				con.setDoInput(true);
				con.connect();
				inputStreamJson = con.getInputStream();
				break;
			}catch(IOException ioe){
				System.out.println("Connection Error "+(attempt+1)+"! ");
				
			}
			attempt++;
		}

		//Programm beenden, bei 3 fehlgeschlagenen Verbindungen
		if(attempt == 3) { 
			System.out.println("Internet connection error or CloudMade server not available!"); 
			return false; 
		}
		try{
			inputStreamJson.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
		
	}
	
}
//Collections
import java.util.ArrayList;
//Exception
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
//Lese Antwort
import java.io.InputStream;
//URL Handling
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
//JSON Handling (json-lib-2.4-jdk15.jar)
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
//Stream to String
import org.apache.commons.io.IOUtils;

/**
 * Geokodierer, nimmt die eingelesenen Adressen entgegen und nutzt die Google Geocoding APIv2, 
 * um die jeweiligen Geokoordinaten zu bestimmen. Implementiert das Geocoder Interface.
 * @author Björn Buchwald
 *
 */
public class GoogleGeocoder implements Geocoder {
	
	//google geocode api
	private static final String urlGoogle = "http://maps.googleapis.com/maps/api/geocode/json?address=";
	private static final String urlSensor = "&sensor=false";
	private static final String urlState = ",+DE";
	private static final String urlMarkers = "&markers=color:blue%7Clabel:";
	//static map url
	private static final String urlStaticMapsString = "http://maps.google.com/maps/api/staticmap?";
	private static final String urlProps = "center=Leipzig,DE&size=400x400";
	
	//private String urlAddressString = "";
	/**
	 * Liste mit Geokodierten Adressen aller Teams.
	 */
	private ArrayList<GeoAddress> geoAddressList = null;
	/**
	 * URL zum Darstellen der Geokodierten Orte mit Hilfe der Google StaticMaps API v2. 
	 */
	private String urlGeoAddressString = "";
	
	/**
	 * Label dieses Geocodierers.
	 */
	private String label = "Google";
	
	/**
	 * Entgegen nehmen der GeoAddresses, durchführen der Geokodierung.
	 * @param addressList Liste der Adressen
	 */
	public GoogleGeocoder( ArrayList<GeoAddress> addressList ){
		
		this.geoAddressList = addressList;
		//setze Bestandteile der URL zusammen
		this.urlGeoAddressString += urlStaticMapsString+urlProps;
		
		
		/*
		//create static map url
		urlAddressString = urlStaticMapsString+urlProps+this.urlAddressString;
		this.urlAddressString = this.urlAddressString.concat( "&sensor=false" );
		System.out.println(this.urlAddressString);
		*/
	}
	
	/* (non-Javadoc)
	 * @see Geocoder#getGeocodes()
	 */
	@Override
	public boolean getGeocodes() {
		
		System.out.println("-----------Google geocoding---------");
		System.out.println();
		
		String encAddrString = "";
		int addressId = 0;
		int attempt = 0;
		
		for(GeoAddress address : this.geoAddressList ){
			
			URL url = null;
			InputStream inputStreamJson = null;
			//Verbindungsaufbau
		
			try{
				encAddrString = URLEncoder.encode(address.getAddress().trim(), "UTF-8");
			}catch( UnsupportedEncodingException uee){
				uee.printStackTrace();
				return false;
			}
			//Erstelle URL
			try{
				url = new URL( urlGoogle+encAddrString+urlState+urlSensor);
			}catch(MalformedURLException mue){
				mue.printStackTrace();
				return false;
			}
			//System.out.println(url.toString());
			
			int results = 0;
			JSONArray resultsArray = null;
			URLConnection con = null;
			String jsonTxt = "";
			JSONObject json = null;
			
			//Prüfen fehlerhafter Rückgabe
			for( int noResultFoundCounter = 0; noResultFoundCounter < 100; noResultFoundCounter++){
				
				//Verbindung herstellen (max. 3 Verbindungsversuche)
				while(attempt < 3){
					try{
						con = url.openConnection();
						con.setDoInput(true);
						con.connect();
						inputStreamJson = con.getInputStream();
						break;
					}catch(IOException ioe){
						System.out.println("Connection Error "+(attempt+1)+"! ");
						//ioe.printStackTrace();
					}
					attempt++;
				}
				//Programm beenden, bei 3 fehlgeschlagenen Verbindungen
				if(attempt == 3) { 
					System.out.println("Internet connection error or server not available!"); 
					return false;
				}
				
				//Daten als String erhalten 
				jsonTxt = "";
				try {
					jsonTxt = IOUtils.toString( inputStreamJson );
				} catch (IOException e) {
					System.out.println("Can't convert source to string!");
					return false;
				}
				//Umwandlung in JSON
				json = (JSONObject) JSONSerializer.toJSON( jsonTxt );
				
				//hier werden oft Arrays ohne Inhalt erzeugt, obwohl resultate vorliegen und die obige URL korrekt ist (?)
				//wird ein array ohne inhalt zurückgegeben, so frage die Quelle erneut an (for Schleife)
				//die genaue Fehlerquelle lässt sich nicht ausmachen, wahrscheinlich Fehler in der JSON-API
				resultsArray = json.getJSONArray("results"); 
				//Prüfe Anzahl der Resultate
				results = resultsArray.size(); //Anzahl der resultate
				//werden resultate zurückgegeben wurde die geocodierung korrekt durchgeführt 
				if( results > 0 ) break;
				//nach 100 Anfragen kein erfolg => brich ab um endlosschleife zu vermeiden.
				//meist max. 20 Fehlversuche
				if( noResultFoundCounter == 99 ){ 
					System.out.println("Proof internet connection!"); 
					return false; 
				}
				
			};
			
			JSONObject firstResult = (JSONObject) resultsArray.get(0);
			String formAddress = firstResult.getString("formatted_address");
			
			//kein Resultat  gefunden, sondern nur Stadtzentrum
			if(results == 1 && (formAddress.equals("Leipzig, Deutschland") || formAddress.equals("Leipzig, Germany")) ){
				System.out.println("No result for: "+address.getAddress());
				System.out.println("To continue and choose city centre as geo-coord (51.33969550, 12.37307470) write \"y\"!");
				System.out.println("To break write \"n\"! Confirm with ENTER!");
				//Lese Nutzerantwort
				String answerStr;
				while(true){
					answerStr = Input.readString();
					if(Input.getFail()){ 
						System.out.println("Fail read! Try again: "); 
						continue; 
					}
					if( answerStr.equals("y") ){
						break;
					}else if(answerStr.equals("n")){ 
						System.out.println("Exit!!");
						return false;
					}else{
						System.out.println("Wrong input! Try again: ");
					}
				}
			}
			//Resultat gefunden
			if(results > 1){
				System.out.println();
				System.out.println("More than one result found!");
				System.out.println();
				
				String marker = "";
				String indexStr = "";
				int index = 0;
				
				GeoAddress[] geoAddresses = new GeoAddress[results];
				//gehe Resultate durch
				for(int i = 0; i < results; i++){
					
					JSONObject result = (JSONObject) resultsArray.get(i);
					//geometry->location
					JSONObject geometry = result.getJSONObject("geometry");
					JSONObject coords = geometry.getJSONObject("location");
					
					float lat = Float.valueOf(coords.getString("lat")).floatValue();
					float lng = Float.valueOf(coords.getString("lng")).floatValue();

					//merke die Geokoordinaten der Adressmöglichkeiten
					geoAddresses[i] = new GeoAddress(i, address.getAddress(),lat,lng);
					System.out.println(i+": "+address.getAddress()+"; "+lat+", "+lng);
					marker += urlMarkers+i+"%7C"+lat+","+lng;
					
				}
				//Rufe Karte mit Adressmöglichkeiten auf
				PlannerModel.browseUrl( urlStaticMapsString+urlProps+marker+urlSensor );
				//Frage Nutzer
				System.out.println();
				System.out.println("Choose location that is nearest to point of interest!");
				//Prüfe Input
				while(true){
					
					indexStr = Input.readString();
					if(Input.getFail()){ 
						System.out.println("Fail read! Try again: "); 
						continue; 
					}
					try{
						index = Integer.valueOf( indexStr ).intValue();
					}catch(Exception e){
						System.out.println("Wrong index! Please choose location: "); 
						continue;
					}
					if(index < 0 || index >= results){
						System.out.println("Wrong index! Please choose location: ");
						continue;
					}
					break;
				}
				//setze Geocode
				address.setGeoCode(geoAddresses[index].getLat(), geoAddresses[index].getLng());
				System.out.println("Your input: "+geoAddresses[index].getLat()+", "+geoAddresses[index].getLng());
				System.out.println();
				
				this.urlGeoAddressString += urlMarkers+Letter.getLetterOrNumericByNumber(addressId)+"%7C"+geoAddresses[index].getLat()+","+geoAddresses[index].getLng();
				//System.out.println(this.urlGeoAddressString);
				
				
			}else{
				
				//bestimme Koordinaten
				JSONObject geometry = firstResult.getJSONObject("geometry");
				JSONObject coords = geometry.getJSONObject("location");
				
				float lat = Float.valueOf(coords.getString("lat")).floatValue();
				float lng = Float.valueOf(coords.getString("lng")).floatValue();
				//Geocode in der GeoAddress setzen
				address.setGeoCode(lat, lng);
				
				System.out.println("Address: "+address.getAddress()+" successful geocoded! Latitude: "+lat+" Longitude: "+lng);
				System.out.println();
				
				this.urlGeoAddressString += urlMarkers+Letter.getLetterOrNumericByNumber(addressId)+"%7C"+lat+","+lng;
				//System.out.println(this.urlGeoAddressString);
				
			}
			addressId++;
		}
		//sensor-Parameter am Ende der URL
		this.urlGeoAddressString += urlSensor;
		
		return true;
	}

	/* (non-Javadoc)
	 * @see Geocoder#getGeoAddrList()
	 */
	@Override
	public ArrayList<GeoAddress> getGeoAddrList() {
		
		return this.geoAddressList;
	}

	/* (non-Javadoc)
	 * @see Geocoder#getUrlGeoAddrStr()
	 */
	@Override
	public String getUrlGeoAddrStr() {
		
		return this.urlGeoAddressString;
	}
	
	/* (non-Javadoc)
	 * @see Geocoder#showMapOfGeocodes()
	 */
	@Override
	public void showMapOfGeocodes(){
		
		//zeige URL im Browser
		PlannerModel.browseUrl(this.urlGeoAddressString);
		int count = 0;
		System.out.println();
		System.out.println("Evaluate geocoded addresses!");
		for(GeoAddress addr : this.geoAddressList){
			
			System.out.println(Letter.getLetterByNumber(count)+": "+addr.getAddress());
			count++;
		}
		System.out.println();
	}

	/* (non-Javadoc)
	 * @see Geocoder#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}
	
}

//Collection
import java.util.ArrayList;
//Exceptions
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
//InputStream to String
import org.apache.commons.io.IOUtils;

/**
 * Geokodierer, nimmt die eingelesenen Adressen entgegen und nutzt die CloudMade Geocoding API, 
 * um die jeweiligen Geokoordinaten zu bestimmen. API-Key wird benötigt, Implementiert das 
 * Geocoder Interface.
 * @author Björn Buchwald
 *
 */
public class CloudMadeGeocoder implements Geocoder{
	
	public static final String urlCloudMade = "http://geocoding.cloudmade.com/";
	public static final String urlGeocoding = "/geocoding/v2/find.js?query=";
	public static final String urlState = ",+Germany";
	
	public static final String urlStaticMaps = "http://staticmaps.cloudmade.com/";
	public static final String urlSize = "/staticmap?size=600x500";
	
	/**
	 * CloudMade API-Key zur Nutzung der CloudMade-Dienste.
	 */
	public String apiKey = null;
	
	/**
	 * Liste mit Geokodierten Adressen aller Teams.
	 */
	private ArrayList<GeoAddress> geoAddressList = null;
	
	/**
	 * URL zum Darstellen der Geokodierten Orte mit Hilfe der CloudMade StaticMaps API. 
	 */
	private String urlGeoAddressString = "";
	/**
	 * Label des Geokodierers.
	 */
	private String label = "CloudMade";
	
	/**
	 * Entgegen nehmen der GeoAddresses und des API-Keys, durchführen der Geokodierung.
	 * @param addressList Liste der Adressen
	 * @param apiKey CloudMade API-Key zur Nutzung der CloudMade-Dienste
	 */
	public CloudMadeGeocoder( ArrayList<GeoAddress> addressList, String apiKey ){
		
		this.geoAddressList = addressList;
		this.apiKey = apiKey;
		this.urlGeoAddressString += urlStaticMaps+this.apiKey+urlSize;
		
	}
	
	/* (non-Javadoc)
	 * @see Geocoder#getGeocodes()
	 */
	public boolean getGeocodes() {
		
		System.out.println("---------CloudMade geocoding----------");
		
		String prepAddress = "";
		String encAddrString = "";
		int addressId = 0;
		int attempt = 0;
		
		for(GeoAddress address : this.geoAddressList ){
			
			URL url = null;
			InputStream inputStreamJson = null;
			//Verbindungsaufbau
			//http://geocoding.cloudmade.com/8ee2a50541944fb9bcedded5165f09d9/geocoding/v2/find.html?query=house:5;street:Jahnallee;city:Leipzig;country:Germany
			//http://geocoding.cloudmade.com/8ee2a50541944fb9bcedded5165f09d9/geocoding/v2/find.html?query=5+Erich+Zeigner+Allee,+Leipzig,+Germany
			/* Schreibweise der Adressen:
			 * "Straße" : immer Ausschreiben, ß --> ss !!
			 * Hausnummer vor den Straßennamen schreiben z.B. 10 Karl-Liebknecht-Strasse
			*/
			//aufbereiten der Adresse
			prepAddress = prepareAddress(address.getAddress());
			try{
				encAddrString = URLEncoder.encode(prepAddress.trim(), "UTF-8");
			}catch( UnsupportedEncodingException uee){
				uee.printStackTrace();
				return false;
			}
			//Erstelle URL
			try{
				url = new URL( urlCloudMade+apiKey+urlGeocoding+encAddrString+urlState);
			}catch(MalformedURLException mue){
				mue.printStackTrace();
				return false;
			}
			
			//Verbindung herstellen (max. 3 Verbindungsversuche)
			while(attempt < 3){
				
				try{
					//Verbindungsobjekt erstellen
					URLConnection con = url.openConnection();
					//nutze das Objekt für den Input
					con.setDoInput(true);
					//Verbindung herstellen
					con.connect();
					//inputStream der von der Verbindung liest
					inputStreamJson = con.getInputStream();
					break;
					
				}catch(IOException ioe){
					System.out.println("Connection Error "+(attempt+1)+"! ");
				}
				attempt++;
				
			}
			//Programm beenden, bei 3 fehlgeschlagenen Verbindungen
			if(attempt == 3) { 
				System.out.println("Internet connection error or server not available!"); 
				return false; 
			}
			
			//String mit Antwort des Dienstes
			String jsonTxt = "";
			//Antwort als String übernehmen 
			try {
				jsonTxt = IOUtils.toString( inputStreamJson );
			} catch (IOException e) {
				System.out.println("Can't convert source to string!");
				return false;
			}
			
			//Umwandlung in JSON
			JSONObject json = (JSONObject) JSONSerializer.toJSON( jsonTxt );
			
			//erzeuge ein JSONArray features
			JSONArray features = json.getJSONArray("features");
			
			//Anzahl der Geokodierungsresultate 
			int results = features.size();
			
			//nimm das erste Resutat und erzeuge aus diesem ein JSONObject
			JSONObject obj = (JSONObject) features.get(0);
			
			//bestimme Ergebniseigenschaften
			JSONObject properties = obj.getJSONObject("properties");
			
			//Prüfe, ob Ergebnis gefunden wurde
			//enthält Ergebnis is_capital so wurde kein genaues Resultat gefunden
			if(results == 1 && properties.has("is_capital")){
				System.out.println("No result for: "+address.getAddress());
				System.out.println("To continue and choose city centre as geo-coord (51.34051, 12.37468) write \"y\"!");
				System.out.println("To break write \"n\"! Confirm with ENTER!");
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
			
			//prüfe, ob exaktes Ergebnis, dh. Hausnummer wurde gefunden, zurückgeliefert wurde.
			//if(results > 1){
			if( results > 1){
				
				System.out.println();
				System.out.println("More than one result found!");
				System.out.println();
				
				String marker = "";
				String indexStr = "";
				int index = 0;
				
				GeoAddress[] geoAddresses = new GeoAddress[results];
				
				for(int i = 0; i < results; i++){
					//Pfad: features->centroid->coordinates
					JSONObject result = (JSONObject) features.get(i);
					
					JSONObject coord = result.getJSONObject("centroid");
					JSONArray coordArray = coord.getJSONArray("coordinates");
					float lat = ((Double) coordArray.get(0)).floatValue();
					float lng = ((Double) coordArray.get(1)).floatValue();
//http://staticmaps.cloudmade.com/8ee2a50541944fb9bcedded5165f09d9/staticmap?size=600x500&center=51.477222,0&zoom=14&styleid=1&marker=url:http://cloudmade.com/images/layout/cloudmade-logo.png|51.477225,0.0
					//merke die Geokoordinaten der Adressmöglichkeiten
					geoAddresses[i] = new GeoAddress(i, address.getAddress(),lat,lng);
					System.out.println(i+": "+address.getAddress()+"; "+lat+", "+lng);
					marker += "&marker=label:"+i+"%7C"+lat+","+lng;
					
				}
				PlannerModel.browseUrl( urlStaticMaps+apiKey+urlSize+marker );
				
				System.out.println("Choose location that is nearest to point of interest!");
				//Prüfe Input
				while(true){
					
					indexStr = Input.readString();
					if(Input.getFail()){ 
						System.out.println("Fail read! Please choose location: "); 
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
				
				this.urlGeoAddressString += "&marker=label:"+Letter.getLetterByNumber(addressId)+"%7C"+geoAddresses[index].getLat()+","+geoAddresses[index].getLng();
				//System.out.println(this.urlGeoAddressString);
			}else{ //exaktes Ergebnis
			
				//bestimme Koordinaten Pfad: centroid->coordinates
				JSONObject coord = obj.getJSONObject("centroid");
				
				JSONArray coordArray = coord.getJSONArray("coordinates");
				float lat = ((Double) coordArray.get(0)).floatValue();
				float lng = ((Double) coordArray.get(1)).floatValue();
				
				//setze Geokoordinate in der aktuellen GeoAddress
				address.setGeoCode(lat, lng);
				
				//enthält das Resultat kein Attribut housenumber, so wurde kein Exaktes Ergebnis gefunden
				//sondern nur die Straße oder Stadt
				if(! properties.has("addr:housenumber")){
					System.out.print("Only one not exact result found ");
					System.out.println(lat+" "+lng+" for: "+address.getAddress()+"");
					System.out.println();
				}else{
					System.out.println("Address: "+address.getAddress()+" successful geocoded! Latitude: "+lat+" Longitude: "+lng);
					System.out.println();
				}
				
				this.urlGeoAddressString += "&marker=label:"+Letter.getLetterByNumber(addressId)+"%7C"+lat+","+lng;
				//System.out.println(this.urlGeoAddressString);
			}
			addressId++;
		}
		
		return true;
	}
	/**
	 * Bereitet die Adresse so vor, dass sie bestmöglich vom CloudMade Service geokodiert 
	 * werden kann.
	 * @param addr
	 * @return prepared Address
	 */
	private String prepareAddress(String addr){
		
		//Splitte Adresse auf (Straße, Hausnummer, Stadt)
		String[] addrArray = addr.split(",");
		int count = addrArray.length;
		//extrhiere Hausnummer
		String houseNumber = addrArray[1];
		houseNumber = houseNumber.trim();
		//extrahiere Straße; ersetzt ß oder Abkürzung von Straße
		String street = addrArray[0].replace("ß", "ss");
		street = street.replaceAll("Str[.]", "Strasse");
		street = street.replaceAll("str[.]", "strasse");
		street = street.trim();
		
		String city = "";
		//Das Feld Stadt kann in der CSV-Datei aus mehreren mit Komma getrennten
		//Einträgen bestehen, beispielsweise um den Stadtteil oder andere Adress-
		//angaben anzugeben
		//prüfe wie viele Einträge und konvertiere in geeignete Darstellung für den Dienst
		if( count > 2 ){
			for( int i = 2; i < count; i++ ){
				city += ",+"+addrArray[i];
			}
		}
		
		return houseNumber+" "+street+city;
	}
	
	
	/* (non-Javadoc)
	 * @see Geocoder#getGeoAddrList()
	 */
	public ArrayList<GeoAddress> getGeoAddrList() {
		
		return this.geoAddressList;
	}
	
	/* (non-Javadoc)
	 * @see Geocoder#getUrlGeoAddrStr()
	 */
	public String getUrlGeoAddrStr(){
		
		return this.urlGeoAddressString;
	}
	
	
	/* (non-Javadoc)
	 * @see Geocoder#showMapOfGeocodes()
	 */
	public void showMapOfGeocodes(){
		
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

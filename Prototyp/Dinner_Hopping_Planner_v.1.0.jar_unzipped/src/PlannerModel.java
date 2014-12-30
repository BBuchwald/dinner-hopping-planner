//Collections
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
//Observer-Pattern
import java.util.Observable;
import java.util.Observer;
//Exception
import java.lang.Exception;
//AWT
import java.awt.Dimension;
import java.awt.Toolkit;
//IO
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
//URL Handling
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
//Datei Handling
import java.io.File;
//Java Properties Handling
import java.util.Properties;
//CloudMade RouteType CAR, BICYCLE
import com.cloudmade.api.CMClient.RouteType;

/**
 * Model des Planners nach MVC, Aufruf der einzelnen Komponenten des Prototyps und Initialisieren der Programm Properties. Erweitert Observable.
 * @author Björn Buchwald
 *
 */
public class PlannerModel extends Observable{
	
	/**
	 * Geokodierer
	 */
	private Geocoder geocoder;
	
	/**
	 * Distanzkalkulator
	 */
	private DistanceCalculator distCalculator;
	
	/**
	 * JXMapViewer Kartenansicht in JFrame mit Minikarte und Zoom
	 */
	private MapViewer viewer;
	
	/**
	 * Routenkberechner
	 */
	private TeamRoutConstructor routeConstructor;

	/**
	 * Liste von GeoAddresses
	 */
	private ArrayList<GeoAddress> geoAddressList;
	
	/**
	 * Liste der Teams
	 */
	private ArrayList<Team> teamList;

	/**
	 * Bestimmte Distanzen von jedem Ort zu jedem anderen Ort der geoAddressList
	 */
	private int[][] distances;
	
	/**
	 * Standard Programm Properties. Properies sind: Geocoder, Distance, Route, APIKEY, Input File, Output File, Override.
	 */
	public static final HashMap<String, String> defaultProps = new HashMap<String, String>();
	
	/**
	 * Standard Konfigurationen für Routenplanungsalgorithmus.
	 */
	public static final HashMap<String, String> defaultConfigurations = new HashMap<String, String>();
	
	/**
	 * aktuell gesetzte Programm Eigenschaften.
	 */
	private HashMap<String, String> actuellProps;
	
	/**
	 * aktuell gesetzte Konfigurationen.
	 */
	private HashMap<String, String> actuellConfigurations;
	
	/**
	 * Aktueller Dienst zur Distanzbestimmung.
	 */
	private String distanceLabel = "";
	
	/**
	 * aktuell gesetztes Resultat, welches im Viewer angezeigt wird.
	 */
	private int actuellResult;
	
	/**
	 * Instance-ID des aktuellen Models
	 */
	private int instance;

	/**
	 * Instanz-ID des letzten erstellten Models
	 */
	private ArrayList<Integer> instanceArray;

	/**
	 * Setzen der Default Properties und Konfigurationen, 
	 * Einlesen der Properties- und RouteConfigurations-Datei falls vorhanden.
	 */
	public PlannerModel(){
		
		instanceArray = new ArrayList<Integer>();
		//Standard Eigenschaften
		defaultProps.put( "Geocoder", "Google" ); 
		defaultProps.put( "Distance", "Osm2po" ); 
		defaultProps.put( "Route", "middle low" );
		defaultProps.put( "APIKEY", "8ee2a50541944fb9bcedded5165f09d9" );
		defaultProps.put( "Input File", "addr.csv");
		defaultProps.put( "Output File", "new.csv");
		defaultProps.put( "Override", "false");
		
		//Standard Konfigurationen
		defaultConfigurations.put("very fast low", "1,10,10");
		defaultConfigurations.put("fast low", "10,10,10");
		defaultConfigurations.put("middle low", "1,100,100");
		defaultConfigurations.put("time-consuming low", "5,100,100");
		defaultConfigurations.put("very time-consuming low", "10,100,100");
		defaultConfigurations.put("very fast high", "1,1000,1000");
		defaultConfigurations.put("fast high", "20,1000,1000");
		defaultConfigurations.put("middle high", "1,1000,20000");
		defaultConfigurations.put("time-consuming high", "10,20000,5000");
		defaultConfigurations.put("very time-consuming high", "10,10000,10000");
		
		//Setze die Programmeigenschaften
		actuellProps = new HashMap<String, String>();
		if( !initializeProperties() ) this.actuellProps = readProps();
		else this.actuellProps = defaultProps; //Lesefehler: setze Defaults
		
		//Setze die Konfigurationen
		if( !initializeConfigurations() ){
			try{
				this.actuellConfigurations = readRouteConfigurations();
			}catch(IllegalArgumentException e){ //falls Fehler in RouteConfigurations-Datei
				System.out.println("Error in RouteConfiguration file! Default Configurations have set.");
				this.actuellConfigurations = defaultConfigurations;
			}
		}
		else this.actuellConfigurations = defaultConfigurations; //Lesefehler: setze Defaults
			
	}
	
	/**
	 * Main Methode Initialisieren des Models und des Views (MVC), Setzen der Eigenschaften der View, initialisiere Instanz-ID.
	 * @param argv Kommandozeilenparameter
	 */
	public static void main(String[] argv  ){
		
		PlannerModel meinModel = new PlannerModel();
		meinModel.instanceArray.add(1);
		meinModel.instance = 1;
		PlannerView frame = new PlannerView( meinModel );
		meinModel.setPropertiesPlannerView( frame );
		
	}
	
	/**
	 * Setzen der Eigenschaften der View (JFrame) des Planners.
	 * @param frame PlannerView
	 */
	public void setPropertiesPlannerView( PlannerView frame ){
		
		frame.setSize(530,630);
		//frame.setSize(620,720);
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		frame.setResizable(false);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation( (d.width- frame.getSize().width ) / 2, (d.height- frame.getSize().height) / 2 );
		
	}
	
	/**
	 * Aufruf der grundlegenden Funktioen des Planners: Einlesen der Datei, Distanzkalkulator, Routenberechner, MapViewer
	 */
	public void initPlanner(){
		
		this.geoAddressList = new ArrayList<GeoAddress>();
		this.teamList = new ArrayList<Team>();
		
		//wenn Input-File ohne Fehler gelesen werden konnte
		if( !readDataAndGeocode() ) return;
			//getLocationMap();
		System.out.println(this.distanceLabel);
		
		if( !calculateDistances() ) return;
			
		constructRoutes();
			
		this.viewer = new MapViewer(this.instance);
			
	}
	
	/**
	 * Kopiere zweidimensionales Integer Array (Distanzen).
	 * @param distances zu kopierendes Array
	 * @return kopiertes Array
	 */
	public static int[][] copy2DIntArray( int[][] distances ){
		
		int[][] newDist = new int[distances.length][distances.length];
		for(int i = 0; i < distances.length; i++ ){
			for( int j = 0; j < distances.length; j++ ){
				newDist[i][j] = distances[i][j];
			}
		}
		
		return newDist;
	}
	
	/**
	 * Kopieren der GeoAddress ArrayList
	 * @param list zu kopierende Liste
	 * @return kopierte GeoAddressList
	 */
	public static ArrayList<GeoAddress> copyGeoAddressList( ArrayList<GeoAddress> list ){
		ArrayList<GeoAddress> newList = new ArrayList<GeoAddress>();
		for( GeoAddress addr : list ){
			newList.add( new GeoAddress(addr) );
		}
		return newList;
	}
	
	/**
	 * Kopieren der Team ArrayList
	 * @param list zu kopierende Liste
	 * @return kopierte TeamListe
	 */
	public static ArrayList<Team> copyTeamList( ArrayList<Team> list ){
		ArrayList<Team> newList = new ArrayList<Team>();
		for( Team team : list ){
			newList.add( new Team(team) );
		}
		return newList;
	}
	
	/**
	 * Einlesen der Properties Datei, Erstellen Properties-Datei und Setzen der Default-Properties falls Datei nicht existiert.
	 * @return true wenn Datei bereits existiert, false sonst.
	 */
	public boolean initializeProperties(){
		
		File file = new File( "./", "Planner.properties");
		//prüfe ob Propertie Datei existiert
		if ( !file.exists() ) {
			//schreibe Default Properties in Propertie Datei
			writeProps( defaultProps );
			return true;
		}
		return false;
		
	}
	
	/**
	 * Einlesen der Konfigurationen für den Routenplanungsalgorithmus, Erstellen Konfigurations-Datei und Setzen der Default-Konfigurationen falls Datei nicht existiert.
	 * @return true wenn Datei bereits existiert, false sonst.
	 */
	public boolean initializeConfigurations(){
		
		File file = new File( "./", "RouteConfiguration.properties");
		//prüfe ob Configurations Datei existiert
		if ( !file.exists() ) {
			//schreibe Default Configurations in Propertie Datei
			writeRouteConfigurations( defaultConfigurations );
			return true;
		}
		return false;
		
	}
	
	
	/**
	 * Schreiben der übergebenen Properies in die Planner.properties Datei.
	 * @param map Properties
	 */
	public void writeProps( HashMap<String, String> map ){
		
		//Schreiben eines Charakter-Streams (java.io.Writer)
		Writer writer = null;
		
		try{  
			
			//Erzeugend er Property-Datei
			writer = new FileWriter( "Planner.properties" ); 
			//(Key, Value) Liste der Programmeigenschaften (java.util.Properties)
			Properties prop1 = new Properties();  
			prop1.setProperty( "Geocoder", map.get("Geocoder") ); 
			prop1.setProperty( "Distance", map.get("Distance") ); 
			prop1.setProperty( "Route", map.get("Route") );
			prop1.setProperty( "APIKEY", map.get("APIKEY") );
			prop1.setProperty( "Input File", map.get("Input File") );
			prop1.setProperty( "Output File", map.get("Output File") );
			prop1.setProperty("Override", map.get("Override"));
			//schreiben der Eigenschaften in die erzeugte Datei
			prop1.store( writer, "-----Dinner Hopping Planner Properties-----\nDo not change this file!" );
			
		}catch ( IOException e ){  
			e.printStackTrace();
		}finally{  
			try { 
				writer.close(); 
			}catch ( Exception e ) {
			}
		}
		
	}
	
	/**
	 * Lesen der Programm Properties aus Planner.properties Datei.
	 * @return Properties
	 */
	public HashMap<String, String> readProps(){
		
		HashMap<String,String> map = new HashMap<String,String>();
		Reader reader = null;
		
		try{
			reader = new FileReader( "Planner.properties" );  
			Properties prop2 = new Properties();  
			prop2.load( reader );  
			map.put( "APIKEY", prop2.getProperty("APIKEY") );
			map.put( "Route", prop2.getProperty("Route") );
			map.put( "Input File", prop2.getProperty("Input File") );
			map.put( "Output File", prop2.getProperty("Output File") );
			map.put( "Geocoder", prop2.getProperty("Geocoder") );
			map.put( "Distance", prop2.getProperty("Distance") );
			map.put( "Override", prop2.getProperty("Override") );
		}catch( IOException e ){
			e.printStackTrace();
		}finally{
			try{
				reader.close();
			}catch( Exception e ){
			}
		}
		return map;
	}
	
	
	
	/**
	 * Prüfe ob ein Windows Betriebssystem vorliegt.
	 * @return boolean true wenn Windows vorliegt, false sonst
	 */
	public static boolean isWindows() {
		String os = System.getProperty("os.name");
			if ( os != null && os.startsWith("Windows")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Erstellen einer Google geocoding URL aus den Geokodierten Adressen. 
	 * @param geoAddressList Liste der Veranstaltungsorte
	 * @return URL 
	 */
	public static String createUrlAddressStringByGeoCodes(ArrayList<GeoAddress> geoAddressList){
		int addrNumber = 0;
		String urlMarkers = "&markers=color:blue%7Clabel:";
		String urlLeipzig = ",+Leipzig,+DE";
		String addrEnc = "";
		String urlAddressString = "";
		
		for(GeoAddress geoAddress : geoAddressList){
			
			try{
				//Adresse kodieren
				addrEnc = URLEncoder.encode(geoAddress.getAddress().trim(), "UTF-8");
			}catch( UnsupportedEncodingException uee){
				uee.printStackTrace();
			}
			urlAddressString = urlAddressString + urlMarkers + Letter.getLetterByNumber(addrNumber) + "%7C" + addrEnc + urlLeipzig;
			addrNumber++;
		}
		return urlAddressString;
	}
	
	/**
	 * Formatierte Ausgabe eines zweidimensionalen Arrays (Array mit Distanzen), Hilfsmethode.
	 * @param array
	 */
	public static void displayArray(int[][] array){
		
		String arrayString;
		int length;
		for(int i= 0;i < array.length; i++ ){
		
			for(int j = 0; j < array.length; j++ ){
				arrayString = Integer.toString(array[i][j]);
				length = arrayString.length();
				switch(length){
				case 1 : System.out.print("000"+array[i][j]+"|"); break;
				case 2 : System.out.print("00"+array[i][j]+"|"); break;
				case 3 : System.out.print("0"+array[i][j]+"|"); break;
				default : System.out.print(array[i][j]+"|");
				}
				
			}
			System.out.println();
		}
	
	}
	
	/**
	 * Lesen der CSV Input Datei, Aufruf des Geokodierers, wenn keine Geokoordinaten in der Datei vorhanden.
	 */
	public boolean readDataAndGeocode(){
		
		System.out.println("--------------Input---------------");
		
		//lese CSV Datei, Fehlermeldung sonst
		System.out.println("Read CSV-File "+actuellProps.get("Input File")+"!");
		System.out.println();
		
		if( CSVHandler.readCSV( actuellProps.get("Input File"), this.geoAddressList, this.teamList) ){
			int i = 0;
			for(Team team : this.teamList){	
				
				if( CSVHandler.geoCoordinatesFound ){
					System.out.print("Team: "+geoAddressList.get(i).getId() + " Address: "+geoAddressList.get(i).getAddress() );
					System.out.println( " Geocode: "+ geoAddressList.get(i).getLat()+ " "+geoAddressList.get(i).getLng() );
				}else{
					System.out.println("Team: "+geoAddressList.get(i).getId() + " Address: "+geoAddressList.get(i).getAddress() );
				}
				System.out.println("      Members: "+team.members[0]+", "+team.members[1] );
				System.out.println();
				i++;
				
			}
			//wenn keine Geokoordinaten gefunden => initilisiere Geokodierer
			if( !CSVHandler.geoCoordinatesFound ){
				System.out.println("No geocoords found! Geocoding necessary!");
				System.out.println();
				switch( actuellProps.get("Geocoder") ){
					case "Google": this.geocoder = new GoogleGeocoder(geoAddressList); break;
					case "CloudMade": this.geocoder = new CloudMadeGeocoder( geoAddressList, actuellProps.get("APIKEY") );break;
				}
				
				//Geokodierung und prüfe ob Geokodierung erfolgreich
				if(!this.geocoder.getGeocodes()) return false;
				
				//Schreibe CSV-Datei mit Geokoordinaten, Fehlermeldung sonst
				if(! CSVHandler.writeCSV(actuellProps.get("Input File"), geoAddressList) ){
					System.out.println( CSVHandler.errorStringWriteGeocodes );
				}
			}else{
				System.out.println("Geocoords found! No geocoding necessary!");
				System.out.println();
			}
			
		}else{
			System.out.println( CSVHandler.errorStringRead );
			return false;
		}
		return true;
	}
	
	/**
	 * Schreiben der Resultate in eine CSV-Datei. Aufruf des CSV-Handlers.
	 */
	public void writeResult(){
		
		if( !CSVHandler.writeCSVResults( actuellProps.get("Output File"), actuellProps.get("Override"), routeConstructor.getRoutes().get( this.actuellResult ), this.geoAddressList ) ){
			System.out.println(CSVHandler.errorStringWrite);
		}else{
			System.out.println("Writing file "+actuellProps.get("Output File")+" successful!");
		}
			
	}
	
	/**
	 * Ausgeben einer Google Karte mit allen geokodierten Addressen (Provisorisch).
	 */
	public void getLocationMap(){
		/*
		InputStream geoCodeStream = null;
		String urlGoogle = "http://maps.googleapis.com/maps/api/geocode/xml?address=";
		String urlSensor = "&sensor=false";
		String urlLeipzig = ",+Leipzig,+DE";
		String encAddrString = "";
		String urlMarkers = "&markers=color:blue%7Clabel:";
		String addrEnc = "";
		int addrNumber = 0;
		URL url = null;
		*/
		String urlAddressString = "";
		//Google Static Maps API v2 url
		String urlStaticMapsString = "http://maps.google.com/maps/api/staticmap?";
		String urlProps = "center=Leipzig,DE&size=400x400";
		
		//caracter "|" will not be encoded by encode method (?), so replace it now
		//urlAddressString = urlAddressString.replaceAll("[|]","%7C");
		urlAddressString = createUrlAddressStringByGeoCodes(this.geoAddressList);
		urlAddressString = urlStaticMapsString+urlProps+urlAddressString;
		urlAddressString = urlAddressString.concat( "&sensor=false" );
		//System.out.println(urlAddressString);
		
		try {
			if (isWindows()) {
				Runtime.getRuntime().exec( "rundll32 url.dll,FileProtocolHandler "
				+urlAddressString );
			} else {
				Runtime.getRuntime().exec("firefox " + urlAddressString);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		System.out.print("Evaluate geocoded addresses!");
		
	}
	
	/**
	 * Aufrufen der Distanzbestimmung. Auswählen der Dienstes und setzen des API-Keys.
	 */
	public boolean calculateDistances(){
		//System.out.println("API: "+this.getActuellProps().get("APIKEY"));
		//wähle Distanzdienst je nach Propertie (Distance)
		switch(this.actuellProps.get("Distance")){
			case "Osm2po": this.distCalculator = new Osm2poDistanceCalculator(); this.distanceLabel = "Osm2po"; break;
			case "CloudMade Car": this.distCalculator = new CloudMadeDistanceCalculator( RouteType.CAR , this.getActuellProps().get("APIKEY") ); this.distanceLabel = "CloudMade Car"; break;
			case "CloudMade Bicycle": this.distCalculator = new CloudMadeDistanceCalculator(  RouteType.BICYCLE , this.getActuellProps().get("APIKEY") ); this.distanceLabel = "CloudMade Bicycle"; break;
			case "Linear Distance": this.distCalculator = new EuklidDistanceCalculator(); this.distanceLabel = "Linear Distance"; break;
		
		}
		
		this.distances = new int[geoAddressList.size()][geoAddressList.size()];
		
		long zstVorher;
		long zstNachher;
		//Zeitanzeige
		zstVorher = System.nanoTime();

		//Berechne Distanzen
		this.distances = this.distCalculator.calculateDistances(geoAddressList);
		//Prüfe, ob Berechnung erfolgreich
		if( this.distances == null ){
			return false;
		}
			
		zstNachher = System.nanoTime();
		
		//Zeige Distanzmatrix in der Konsole
		System.out.println();
		System.out.println("Distance matrix:");
		PlannerModel.displayArray(this.distances);
		//Anzeige Dauer
		System.out.println();
		System.out.println("Time required: " + ((double) (zstNachher - zstVorher) /1000000000) + " sec");
		System.out.println();
		
		return true;
	}
	
	/**
	 * Schreiben der Default-Konfigurationen in RouteConfiguration.properties Datei.
	 * @param map Konfigurationen
	 */
	public void writeRouteConfigurations( HashMap<String, String> map ){
		
		//Schreiben eines Character-Streams (java.io.Writer)
		Writer writer = null;
		
		try{  
			
			//Erzeugen der Route Configurations Property-Datei
			writer = new FileWriter( "RouteConfiguration.properties" ); 
			//(Key, Value) Liste der Konfigurationen für die Routenplanung (java.util.Properties)
			Properties prop1 = new Properties();  
			prop1.setProperty( "very fast low", map.get("very fast low") ); 
			prop1.setProperty( "fast low", map.get("fast low") ); 
			prop1.setProperty( "middle low", map.get("middle low") );
			prop1.setProperty( "time-consuming low", map.get("time-consuming low") );
			prop1.setProperty( "very time-consuming low", map.get("very time-consuming low") );
			prop1.setProperty( "very fast high", map.get("very fast high") );
			prop1.setProperty("fast high", map.get("fast high"));
			prop1.setProperty( "middle high", map.get("middle high") );
			prop1.setProperty("time-consuming high", map.get("time-consuming high"));
			prop1.setProperty("very time-consuming high", map.get("very time-consuming high"));
			//schreiben der Eigenschaften in die erzeugte Datei
			prop1.store( writer, "-----Dinner Hopping Planner Route Configuration Properties-----\nConfiguration = A, B, C all values have to be higher than 1\nChange settings at one's own option!\nReturn to defaults by deleting this file." );
			
		}catch ( IOException e ){  
			e.printStackTrace();
		}finally{  
			try { 
				writer.close(); 
			}catch ( Exception e ) {
			}
		}
		
	}
	
	/**
	 * Lesen der Konfigurationen aus RouteConfigurations.properties. 
	 * @return
	 * @throws IllegalArgumentException, wenn ein Fehler in einer Konfiguration entdeckt wird (jeweils 3 Werte > 1).
	 */
	public HashMap<String, String> readRouteConfigurations() throws IllegalArgumentException{
		
		HashMap<String,String> map = new HashMap<String,String>();
		Reader reader = null;
		
		try{
			reader = new FileReader( "RouteConfiguration.properties" );  
			Properties prop2 = new Properties();  
			prop2.load( reader );
			map.put( "very fast low", prop2.getProperty("very fast low") );
			map.put( "fast low", prop2.getProperty("fast low") );
			map.put( "middle low", prop2.getProperty("middle low") );
			map.put( "time-consuming low", prop2.getProperty("time-consuming low") );
			map.put( "very time-consuming low", prop2.getProperty("very time-consuming low") );
			map.put( "very fast high", prop2.getProperty("very fast high") );
			map.put( "fast high", prop2.getProperty("fast high") );
			map.put( "middle high", prop2.getProperty("middle high") );
			map.put( "time-consuming high", prop2.getProperty("time-consuming high") );
			map.put( "very time-consuming high", prop2.getProperty("very time-consuming high") );
			//Prüfe Konfigurationen
			if( !checkConfiguration( map.values() ) ) throw new IllegalArgumentException ("Error while parsing RouteConfiguration.properties file!");	
		}catch( IOException e ){
			e.printStackTrace();
		}finally{
			try{
				reader.close();
			}catch( Exception e ){
			}
		}
		return map;
	}
	
	/**
	 * Überprüfen aller Konfigurationen (3 Werte jeweils größer 1).
	 * @param configurations Collection<String> mit Konfigurationen.
	 * @return true, wenn Konfigurationen konsistent, false sonst.
	 */
	public boolean checkConfiguration( Collection<String> configurations){
		
		String[] config;
		//Durchlauf aller Konfigurationen
		for( String value : configurations ){
			
			//einzelne Werte durch Komma getrennt
			config = value.split(",");
			
			//3 Werte
			if( config.length != 3 ) return false;
			//jeweils größer 1
			if( Integer.valueOf( config[0] ) < 1 || Integer.valueOf( config[1] ) < 1 || Integer.valueOf( config[2] ) < 1 ) return false;
			
		}
		
		return true;
		
	}
	
	/**
	 * Ausführen des Routenberechnung. Setzen der Parameter des Algorithmus.
	 */
	public void constructRoutes(){
		
		this.routeConstructor = new TeamRoutConstructor(this.distances, this.geoAddressList, this.teamList);
		
		//setze Rekursionsparameter je nach ausgewählten Properties (Route)
		switch( this.actuellProps.get("Route") ){
			//setRecursionProperties(solutions, recursionDepth, changeMainCoursesForRecursion);
			case "very fast low": setConfigurations( this.actuellConfigurations.get("very fast low") ); break;//15
			case "fast low": setConfigurations( this.actuellConfigurations.get("fast low") ); break;
			case "middle low": setConfigurations( this.actuellConfigurations.get("middle low") ); break;
			case "time-consuming low": setConfigurations( this.actuellConfigurations.get("time-consuming low") ); break;
			case "very time-consuming low": setConfigurations( this.actuellConfigurations.get("very time-consuming low") ); break;
			case "very fast high": setConfigurations( this.actuellConfigurations.get("very fast high") ); break;//15
			case "fast high": setConfigurations( this.actuellConfigurations.get("fast high") ); break;
			case "middle high": setConfigurations( this.actuellConfigurations.get("middle high") ); break;
			case "time-consuming high": setConfigurations( this.actuellConfigurations.get("time-consuming high") ); break;
			case "very time-consuming high": setConfigurations( this.actuellConfigurations.get("very time-consuming high") ); break;
		
		}
		//Zeimessung
		long zstVorher2;
		long zstNachher2;

		
		zstVorher2 = System.nanoTime();
		
		//Ausführen Routenplanungsalgorithmus
		this.routeConstructor.constructRoutes();
		
		zstNachher2 = System.nanoTime();
		
		System.out.println();
		System.out.println("Time required: " + ((double) (zstNachher2 - zstVorher2) /1000000000) + " sec");
			
	}

	/**
	 * setze aktuelle Konfiguration im RouteConstructor
	 * @param configuration
	 */
	private void setConfigurations( String configuration ){
		
		String[] config = configuration.split(",");
		this.routeConstructor.setRecursionProperties( Integer.valueOf( config[0] ), Integer.valueOf( config[1] ), Integer.valueOf( config[2] ) );
	
	}
	
	
	/**
	 * Programm zum warten der übergebenen Zeit (ms) veranlassen.
	 * @param mseconds 
	 */
	public static void sleep(int mseconds){
		try {
			Thread.currentThread();
			Thread.sleep(mseconds);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 * Aufrufen der übergegbenen URL im Browser.
	 * @param urlAddressString
	 */
	public static void browseUrl( String urlAddressString ){
	
		
		try {
			if (isWindows()) {
				Runtime.getRuntime().exec( "rundll32 url.dll,FileProtocolHandler "
				+urlAddressString );
			} else {
				Runtime.getRuntime().exec("firefox " + urlAddressString);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	
	}
	
	/**
	 * Diese Methode entfernt einen Observer aus der Observerliste dieses Modells. 
	 * Es wird also  ab jetzt ein View weniger benachrichtig, falls sich im Modell
	 * etwas aendert.
	 * Ausserdem wird das gesamte Programm beendet, falls kein Observer mehr 
	 * angemeldet ist (es liefe sonst unsichtbar weiter).
	 * @param obs Observer
	 */
	public void deleteObserver( Observer obs)
	{
		super.deleteObserver( obs);
		if( countObservers() == 0) release();
	}
	
	/**
	 * Die Methode release() gibt es im Model, im View und im Controller. Sie 
	 * wird gerufen, falls das Programm beendet werden soll und dem entsprechenden
	 * Objekt die Moeglichkeit zu abschliessenden Massnahmen gegeben werden soll.
	 * In diesem Fall kann der Aufruf der Methode durch die Methode deleteObserver(...)
	 * oder durch den Controller geschehen. Diese Methode beendet das Programm dann.
	 */
	private void release()
	{
		System.exit( 0);
	}
	
	/**
	 * Aktueller Routenkalkulator.
	 * @return the routeConstructor
	 */
	public TeamRoutConstructor getRouteConstructor() {
		return routeConstructor;
	}

	/**
	 * Setze aktuellen Routenkalkulator.
	 * @param routeConstructor the routeConstructor to set
	 */
	public void setRouteConstructor(TeamRoutConstructor routeConstructor) {
		this.routeConstructor = routeConstructor;
	}
	
	/**
	 * Aktuelle Instanz des JXMapViewers.
	 * @return the viewer
	 */
	public MapViewer getViewer() {
		return viewer;
	}

	/**
	 * Setze aktuelle Instanz des JXMapViewers.
	 * @param viewer the viewer to set
	 */
	public void setViewer(MapViewer viewer) {
		this.viewer = viewer;
	}
	
	/**
	 * Liste mit allen Teams.
	 * @return the teamList
	 */
	public ArrayList<Team> getTeamList() {
		return teamList;
	}

	/**
	 * Setze Liste mit allen Teams.
	 * @param teamList the teamList to set
	 */
	public void setTeamList(ArrayList<Team> teamList) {
		this.teamList = teamList;
	}
	
	/**
	 * Liste der Veranstaltungsorte mit Geokoordinaten.
	 * @return the geoAddressList
	 */
	public ArrayList<GeoAddress> getGeoAddressList() {
		return geoAddressList;
	}

	/**
	 * Setze Liste der Veranstaltungsorte mit Geokoordinaten.
	 * @param geoAddressList the geoAddressList to set
	 */
	public void setGeoAddressList(ArrayList<GeoAddress> geoAddressList) {
		this.geoAddressList = geoAddressList;
	}
	
	/**
	 * Aktuell geltende Properies des Programms.
	 * @return the actuellProps 
	 */
	public HashMap<String, String> getActuellProps() {
		return actuellProps;
	}

	/**
	 * Setze aktuell geltende Properies des Programms. In Datei Planner.properties gespeichert.
	 * @param actuellProps the actuellProps to set
	 */
	public void setActuellProps(HashMap<String, String> actuellProps) {
		this.actuellProps = actuellProps;
	}
	
	/**
	 * Aktuell geltende Konfigurationen.
	 * @return the actuellConfigurations
	 */
	public HashMap<String, String> getActuellConfigurations() {
		return actuellConfigurations;
	}

	/**
	 * Setze aktuell geltende Konfigurationen.
	 * @param actuellConfigurations
	 */
	public void setActuellConfigurations(
			HashMap<String, String> actuellConfigurations) {
		this.actuellConfigurations = actuellConfigurations;
	}
	
	/**
	 * Resultat, welches aktuell angezeigt wird
	 * @return the actuellResult
	 */
	public int getActuellResult() {
		return actuellResult;
	}

	/**
	 * Setze Resultat, das angezeigt werden soll.
	 * @param actuellResult the actuellResult to set
	 */
	public void setActuellResult(int actuellResult) {
		this.actuellResult = actuellResult;
	}
	
	/**
	 * Distanzen des Distanzkalkulators
	 * @return berechnete Distanzen
	 */
	public int[][] getDistances() {
		return distances;
	}
	
	/**
	 * @param distances
	 */
	public void setDistances(int[][] distances) {
		this.distances = distances;
	}
	
	/**
	 * @return Instanz-ID der letzten erzeugten Instanz als Liste
	 */
	public ArrayList<Integer> getInstanceArray() {
		return instanceArray;
	}

	/**
	 * @param instanceArray
	 */
	public void setInstanceArray(ArrayList<Integer> instanceArray) {
		this.instanceArray = instanceArray;
	}
	
	/**
	 * @return Instanz-ID des aktuellen Models
	 */
	public int getInstance() {
		return instance;
	}

	/**
	 * Setze Instance-ID des aktuellen Models
	 * @param instance
	 */
	public void setInstance(int instance) {
		this.instance = instance;
	}
	
	
}
/**
 * Stellt eine geokodierte Adresse dar.
 * @author Björn Buchwald
 *
 */
class GeoAddress{
	
	/**
	 * ID der Geoadresse
	 */
	private int id;
	/**
	 * Adressname dieser Geoadresse
	 */
	private String address;
	/**
	 * Längengrad der Geokoordinate
	 */
	private float lng = 0.0f;
	/**
	 * Breitengrad der Geokoordinate
	 */
	private float lat = 0.0f;
	
	/**
	 * Erzeugen einer GeoAddress ohne Geokoordinaten.
	 * @param id id des Ortes stimmt mit der Team ID überein.
	 * @param address im input File übergebener Adressname
	 */
	public GeoAddress( int id, String address ){
		this.address = address;
		this.id = id;
	}
	
	/**
	 * Erzeugen einer vollständigen GeoAddress.
	 * @param id ID des GeoAddress
	 * @param address im input File übergebener Adressname
	 * @param lat Breitengrad der Geokoordinate.
	 * @param lng Längengrad der Geokoordinate.
	 */
	public GeoAddress(int id, String address, float lat, float lng ){
		this.id = id;
		this.address = address;
		this.lng = lng;
		this.lat = lat;
	}
	
	/**
	 * Kopierkonstruktor
	 * @param addr Geoaddress
	 */
	public GeoAddress( GeoAddress addr ){
		
		this.address = addr.address;
		this.id = addr.id;
		this.lng = addr.lng;
		this.lat = addr.lat;
	
	}
	
	/**
	 * Zurückgeben des Adressnamens
	 * @return String Adressname
	 */
	public String getAddress(){
		return this.address;
	}
	
	/**
	 * Zurückgeben des Längengrades.
	 * @return float Längengrad
	 */
	public float getLng(){
		return this.lng;
	}
	
	/**
	 * Zurückgeben des Breitengrades.
	 * @return float Breitengrad
	 */
	public float getLat(){
		return this.lat;
	}
	
	/**
	 * Setzen der Adresse.
	 * @param address Adresse
	 */
	public void setAddress(String address){
		this.address = address;
	}
	
	/**
	 * Setzt die Geokoordinaten.
	 * @param lat Breitengrad
	 * @param lng Längengrad
	 */
	public void setGeoCode(float lat, float lng){
		this.lat = lat;
		this.lng = lng;
	}
	
	/**
	 * Gibt an, ob diese GeoAddress Geokoordinaten enthält.
	 * @return true wenn Geokoordinaten vorhanden, false sonst.
	 */
	public boolean isSetGeoCode(){
		if( lat != 0.0f && lng != 0.0f ){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Zurückgeben der ID dieser GeoAddress.
	 * @return ID
	 */
	public int getId(){
		return this.id;
	}
	
}
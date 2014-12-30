/**
 * Stellt eine geokodierte Adresse dar.
 * @author Bj�rn Buchwald
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
	 * L�ngengrad der Geokoordinate
	 */
	private float lng = 0.0f;
	/**
	 * Breitengrad der Geokoordinate
	 */
	private float lat = 0.0f;
	
	/**
	 * Erzeugen einer GeoAddress ohne Geokoordinaten.
	 * @param id id des Ortes stimmt mit der Team ID �berein.
	 * @param address im input File �bergebener Adressname
	 */
	public GeoAddress( int id, String address ){
		this.address = address;
		this.id = id;
	}
	
	/**
	 * Erzeugen einer vollst�ndigen GeoAddress.
	 * @param id ID des GeoAddress
	 * @param address im input File �bergebener Adressname
	 * @param lat Breitengrad der Geokoordinate.
	 * @param lng L�ngengrad der Geokoordinate.
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
	 * Zur�ckgeben des Adressnamens
	 * @return String Adressname
	 */
	public String getAddress(){
		return this.address;
	}
	
	/**
	 * Zur�ckgeben des L�ngengrades.
	 * @return float L�ngengrad
	 */
	public float getLng(){
		return this.lng;
	}
	
	/**
	 * Zur�ckgeben des Breitengrades.
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
	 * @param lng L�ngengrad
	 */
	public void setGeoCode(float lat, float lng){
		this.lat = lat;
		this.lng = lng;
	}
	
	/**
	 * Gibt an, ob diese GeoAddress Geokoordinaten enth�lt.
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
	 * Zur�ckgeben der ID dieser GeoAddress.
	 * @return ID
	 */
	public int getId(){
		return this.id;
	}
	
}
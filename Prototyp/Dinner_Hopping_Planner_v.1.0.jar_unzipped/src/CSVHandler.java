//Collection
import java.util.ArrayList;
//Timestamp
import java.util.Date;
import java.text.SimpleDateFormat;
//Datei Handling
import java.io.File;
import java.io.FileWriter;
//Exceptions
import java.io.IOException;
import java.io.FileNotFoundException;
//javacsv2.1
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

/**
 * Klasse zum Schreiben und Lesen von CSV-Dateien mit Teamdaten.
 * @author Björn Buchwald
 *
 */
class CSVHandler{

	/**
	 * Header der CSV-Input-Datei.
	 */
	public static final String[] CSV_HEADER = new String[]{"Zeitstempel","Teilnehemer 1","Teilnehmer 2","Straße","Hausnummer", "Stadt", "Telefon","Email","Sonstiges"};
	
	/**
	 * Header der CSV-Input-Datei mit Geokoordinaten.
	 */
	public static final String[] CSV_HEADER_GEOCODES = new String[]{"Zeitstempel","Teilnehmer 1","Teilnehmer 2","Straße","Hausnummer","Stadt","Telefon","Email","Sonstiges","lat","lng"};
	
	/**
	 * Header der CSV-Output-Datei.
	 */
	public static final String[] CSV_HEADER_OUTPUT = new String[]{"Id","Teilnehmer 1","Teilnehmer 2", "lat", "lng", "Vorspeise", "Hauptspeise", "Nachspeise"};
	
	/**
	 * Fehlermeldung beim Lesen einer CSV-Datei
	 */
	public static String errorStringRead = "";
	
	/**
	 * Fehlermeldung beim Lesen einer CSV-Datei mit Geokoordinaten.
	 */
	public static String errorStringWriteGeocodes = "";
	
	/**
	 * Fehlermeldung beim Schreiben von Ergebnissen.
	 */
	public static String errorStringWrite = "";
	
	/**
	 * Gibt an, ob Geokoordinaten im eingelesenen File gefunden wurden.
	 */
	public static boolean geoCoordinatesFound = false;
	
	
	/**
	 * Dient zum einlesen der Input CSV-Datei. Der Header muss folgendes Format besitzen:
	 * Zeitstempel,Teilnehmer 1,Teilnehmer 2,Straße,Hausnummer,Telefon,Email,Sonstiges;
	 * Sind bereits Geokoordinaten vorhanden so werden diese ebenfalls eingelesen, der 
	 * muss dafür folgendes Format besitzen:
	 * Zeitstempel,Teilnehmer 1,Teilnehmer 2,Straße,Hausnummer,Telefon,Email,Sonstiges,lat,lng
	 * Bei fehlerhaftem Einlesen kann ein Error-String mit Fehlerursache durch Abfragen von 
	 * der Variable errorStringRead ausgegeben werden.
	 * @param filename
	 * @param addressList
	 * @param teams
	 * @return boolean true bei erfolgreichem einlesen, false sonst
	 */
	public static boolean readCSV( String filename, ArrayList<GeoAddress> addressList, ArrayList<Team> teams ){
		
		CSVHandler.geoCoordinatesFound = false;
		errorStringRead = "";
		addressList.clear(); 
		teams.clear();
		ArrayList<String[]> members = new ArrayList<String[]>();
		
		try{
			
			CsvReader products = new CsvReader(filename);
			
			products.setDelimiter(',');
			products.setSkipEmptyRecords(true);
			//Lese CSV-Header
			products.readHeaders();
			
			//wenn Header = 9 Spalten => kein Geokoordinaten entdeckt
			if( products.getHeaderCount() == 9){
				
				//Prüfe, ob korrekte Anzahl von Teams
				if( !getNumberOfRecords(filename) ){
					errorStringRead += "Uncorrect number of Teams n. (n >= 9 and n must be divisible by three)!";
					return false;
				}
				
				int i = 0; 
				//Lese einzelne Zeilen
				while (products.readRecord())
				{
					//prüfe jeweils die Anzahl der Spalten
					if( products.getColumnCount() != 9 ){
						errorStringRead += "Uncorrect column count in line "+i+" in file "+filename+"! \n";
						return false;
					}
					
					//Lese einzelne CSV Spalten
					//String zeitstempel = products.get("Zeitstempel"); 
					//System.out.print(zeitstempel);
					members.add(new String[]{ products.get("Teilnehmer 1"), products.get("Teilnehmer 2")});
					/*
					System.out.print(members.get(i)[0]+" "+members.get(i)[1]);
					String tel = products.get("Telefon");
					System.out.print(tel);
					String mail = products.get("Email");
					System.out.print(mail);
					String others = products.get("Sonstiges");
					System.out.print(others);
					*/
					//Erzeuge GeoAddress aus eingelesenen Daten
					addressList.add( new GeoAddress(i, products.get("Straße")+","+products.get("Hausnummer")+",+"+products.get("Stadt") ) );
					
					//System.out.print(products.get("Straße")+" "+products.get("Hausnummer"));
					
					i++;
				}
		
				products.close();
			}else if(products.getHeaderCount() == 11 && products.getHeader(9).equals("lat") && products.getHeader(10).equals("lng") ){
				
				CSVHandler.geoCoordinatesFound = true;
				
				//Prüfe, ob korrekte Anzahl von Teams
				if( !getNumberOfRecords(filename) ){
					errorStringRead += "Uncorrect number of Teams n. (n >= 9 and n must be divisible by three)!";
					return false;
				}
				
				int i = 0;
				while (products.readRecord())
				{
					if( products.getColumnCount() != 11 ){
						errorStringRead += "Uncorrect column count in line "+i+" in file "+filename+"! \n";
						return false;
					}
					//String zeitstempel = products.get("Zeitstempel");
					members.add( new String[]{ products.get("Teilnehmer 1"), products.get("Teilnehmer 2") } );
					//String tel = products.get("Telefon");
					//String mail = products.get("Email");
					//String others = products.get("Sonstiges");
					addressList.add( new GeoAddress( i, products.get("Straße")+","+products.get("Hausnummer")+","+products.get("Stadt"), Float.valueOf( products.get("lat") ), Float.valueOf( products.get("lng") ) ) );
					i++;
				}
				
			}else{
				errorStringRead += "Uncorrect header column count in file "+filename+"! \n";
				return false;
			}
			
			//Erzeuge Teams mit id und Namen der Mitglieder
			for( int l = 0; l < members.size(); l++ ){
				teams.add(new Team(l, members.get(l), members.size()));
				
			}
			
			products.close();
		
		} catch (FileNotFoundException e) {
			errorStringRead += "File "+filename+" not found!\n";
			return false;
			
		} catch (IOException e) {
			errorStringRead += "Error while reading file "+filename+"!\n";
			return false;
		}
			
		return true;
		
	}
	
	/**
	 * Prüft, ob die Anzahl der Teams in der Input-Datei korrekt ist.
	 * @param filename zu lesendes File
	 * @return true, wenn Anzahl Team durch 3 teilbar und größer gleich 9, sonst false
	 * @throws IOException
	 */
	public static boolean getNumberOfRecords(String filename) throws IOException{ 
		
		CsvReader products = new CsvReader(filename);
		
		products.setDelimiter(',');
		//products.setSkipEmptyRecords(true);
		
		//Lese CSV-Header
		products.readHeaders();
		
		int recordCount = 0;
		//Zähle einzelne Zeilen
		while ( products.readRecord() ){
			//System.out.println( products.getRawRecord() );
			recordCount++;
			
		}
		products.close();
		//Anzahl der Teams muss größer gleich 9 und durch 3 teilbar sein
		if( recordCount % 3 == 0 && recordCount >= 9){
			return true;
		}else{
			return false;
		}
		
	}
	
	/**
	 * Nach dem Geokodieren erzeugt diese Funktion eine CSV-Datei mit Geokoordinaten nach
	 * dem in readCSV() genannten Format, Vorsicht: Gleichnamige Dateien werden überschrieben!
	 * Bei fehlerhaftem Schreibne kann ein Error-String mit Fehlerursache durch Abfragen von 
	 * der Variable errorStringWriteGeocodes ausgegeben werden.
	 * @param filename
	 * @param addressList
	 * @return
	 */
	public static boolean writeCSV( String filename, ArrayList<GeoAddress> addressList ){
		
		
		
		errorStringWriteGeocodes = "";
		//write File
		String[] fileString = filename.split("[.]");
		String newName = "";
		
		for( int j = 0; j < fileString.length -1; j++ ){
			
			newName = newName+fileString[j];
			
		}
		
		String outputFile = newName+"WithGeocodes.csv";
		
		System.out.println("Writing file with geocoordinates "+outputFile+"!");
		
		// before we open the file check to see if it already exists
		new File(outputFile);
		/*
		try{
			if( file.exists() ){
				if( !file.delete() ){
					errorStringWriteGeocodes += "Error while delete existing file "+outputFile+"! \n";
					return false;
				}
				if( !file.createNewFile() ){
					errorStringWriteGeocodes += "Error while create file "+outputFile+" with geocodes! \n";
					return false;
				}
			}else{
				if( !file.createNewFile() ){
					errorStringWriteGeocodes += "Error while create file "+outputFile+" with geocodes! \n";
					return false;
				}
			}
		}catch(IOException ioe){
			errorStringWriteGeocodes += "Error while delete existing file "+outputFile+" or creating the new file with geocodes! \n";
			return false;
		}*/
		
		try {
			
			//lese Input File
			CsvReader products = new CsvReader( filename );
			products.setDelimiter(',');
			products.readHeaders();
			// use FileWriter constructor that specifies open for appending
			CsvWriter csvOutput = new CsvWriter( new FileWriter( outputFile, false ), ',' );
			
			//schreibe Zeitstempel als Kommentar
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy H:mm:ss");
			csvOutput.writeComment( sdf.format( date ) );
			
			// if the file didn't already exist then we need to write out the header line
			csvOutput.writeRecord(CSV_HEADER_GEOCODES);
			
			int i = 0;
			//schreibe einzelne Zeilen
			while ( products.readRecord() ){
				
				//hänge an jeder Zeile des Input-Files die Geocodes an
				csvOutput.writeRecord( new String[]{ products.get("Zeitstempel"),products.get("Teilnehmer 1"),products.get("Teilnehmer 2"),products.get("Straße"),products.get("Hausnummer"),products.get("Stadt"), products.get("Telefon"),products.get("Email"),products.get("Sonstiges"),String.valueOf( addressList.get(i).getLat() ),String.valueOf( addressList.get(i).getLng() ) } );
				i++;
			}
		
			
			products.close();
			csvOutput.close();
		
		} catch ( IOException e ) {
			errorStringWriteGeocodes += "Error while write file "+outputFile+" with geocodes!\n";
			return false;
					
		}
		return true;
   
	}
	
	
	/**
	 * Schreiben des übergebenen Resultats in die Output-Datei. Diese hat folgendes Foramt:
	 * Id,Teilnehmer 1,Teilnehmer 2,Vorspeise,Hauptspeise,Nachspeise;
	 * Bei fehlerhaftem Schreibne kann ein Error-String mit Fehlerursache durch Abfragen von 
	 * der Variable errorStringWrite ausgegeben werden.
	 * @param outputFile Name des Output Files
	 * @param override die Propertie override entscheidet, ob gleichnamige Dateien überschriebne werden sollen.
	 * @param result Resultat, welches ausgegeben werden soll.
	 * @return boolean true bei erfolgreichem Erstellen des Output Files, false sonst.
	 */
	public static boolean writeCSVResults( String outputFile, String override, ArrayList<Team> result, ArrayList<GeoAddress> geoAddressList ){
		
		System.out.println("-----------------Output--------------");
		System.out.println();
		
		errorStringWrite = ""; 
		File file = new File( outputFile );
		if( override.equals("false") ){
			int i = 0;
			
			String[] outputString = outputFile.split("[.]");
			if( outputString.length == 1 ) outputString = new String[]{ outputString[0], ".csv" };
			while( file.exists() ){
				String newName = "";
				for( int j = 0; j < outputString.length -1; j++ ){
					newName = newName+outputString[j];
				}
				file = new File(newName+"("+i+")"+".csv");
				i++;
				if( i > 20 ){
					errorStringWrite += "To many files with same name while writing file "+outputFile+"!\n";
					return false;
				}
			}
		}
		
		System.out.println("Writing output file "+file.getName()+"!");
		System.out.println();
		
		try{
			// use FileWriter constructor that specifies open for appending
			CsvWriter csvOutput = new CsvWriter( new FileWriter( file.getName(), false ), ',' );
			
			//schreibe Zeitstempel als Kommentar
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy H:mm:ss");
			csvOutput.writeComment( sdf.format( date ) );
			
			// if the file didn't already exist then we need to write out the header line
			csvOutput.writeRecord(CSV_HEADER_OUTPUT);
			
			//schreibe Ergebnissätze
			for( Team team : result ){
				System.out.println( "Team: " + team.home + " Members: " + team.members[0] + ", " + team.members[1] );
				System.out.println( "       Route: start " + team.starter + " -> main " + team.mainCourse + " -> dessert " + team.dessert );
				csvOutput.writeRecord( new String[]{ String.valueOf( team.home ), team.members[0], team.members[1], String.valueOf( geoAddressList.get( team.home ).getLat() ), String.valueOf( geoAddressList.get( team.home ).getLng() ), String.valueOf( team.starter ), String.valueOf( team.mainCourse), String.valueOf( team.dessert ) } );
			
			}
		
			csvOutput.close();
		}catch( IOException ioe ){
			errorStringWrite += "Error while writing results to file "+file.getName()+"!\n";
			return false;
		}
		
		return true;
	}
	
}
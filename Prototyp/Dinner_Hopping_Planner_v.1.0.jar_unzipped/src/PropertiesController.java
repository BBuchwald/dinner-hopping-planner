//Collections
import java.util.HashMap;
//Datei Handling
import java.io.File;
//Lese Antwort (Prüfe API-Key)
import java.io.InputStream;
//Exception
import java.io.IOException;
import java.net.MalformedURLException;
//URL Handling
import java.net.URL;
import java.net.URLConnection;
//AWT
import java.awt.Color;
import java.awt.event.*; // KeyEvent
//SWING
import javax.swing.JCheckBox;

/**
 * Controller des Planners nach MVC, nimmt Benutzereingaben des Properties-Fensters entgegen
 * @author Björn Buchwald
 *
 */
public class PropertiesController extends WindowAdapter implements ActionListener{
	
	/** Modell des Programmes */
	private PlannerModel plannerModel;

	/** zum Controller gehoeriger View */
	private PropertiesView propView;
	
	/**
	 * actuell gesetzte Eigenschaften des Programms
	 */
	private HashMap<String, String> actuellProps;
	
	/**
	 * Der Konstruktor nimmt das Model des Programmes und den zum Controller
	 * gehoerigen View entgegen und weist sie seinen Klassenvariablen zu.
	 * @param neuerView der zum Controller gehoerige View
	 * @param neuesModel das Model des Programmes
	 */
	public PropertiesController( PropertiesView neuerView, PlannerModel neuesModel)
	{
		this.propView = neuerView;
		this.plannerModel = neuesModel;
		//this.actuellProps = (HashMap<String,String>) neuesModel.getActuellProps().clone();
		this.actuellProps = new HashMap<String, String>();
		this.actuellProps.putAll(neuesModel.getActuellProps());
		
	}

	/**
	 * Diese Methode wird automatisch gerufen, wenn eine "Action" im PropertiesView 
	 * festgestellt wird. Diese Eigenschaft bekommt der Controller daher, dass er
	 * als "ActionListener" fuer diverse Komponenten des Views angemeldet wurde.
	 * Die Methode wertet nun die Aktionen des Users im View aus und unternimmt
	 * die notwendigen Schritte zur Verarbeitung der Eingaben.
	 * Setze neue Programm-Eigenschaften. 
	 */
	public void actionPerformed(ActionEvent ae) {
		
		// Annahme der vom System uebergebenen Action
		String command = ae.getActionCommand();
		
		//Üernehme Input-Datei
		if( command.equals("Apply File") ){
			
			String inputFile = this.propView.getFileTextField().getText();
			
			File file = new File("./", inputFile);
			//Wenn File existiert 
			if (file.exists()){
				//File kann gelesen werden
				if( file.canRead() ){
					//setze Status Label 
					try{
						if( CSVHandler.getNumberOfRecords(inputFile) ){
							
							this.propView.getStateLabel().setForeground(Color.green);
							this.propView.getStateLabel().setText("State: File applied successful!");
							this.actuellProps.put("Input File", inputFile );
							
						}else{
							//setze Status Label: Fehler!
							this.propView.getStateLabel().setForeground(Color.red);
							this.propView.getStateLabel().setText("State: Uncorrect number of Teams n. (n >= 9 and n must be divisible by three)!");
							
						}
					}catch(IOException e){
						//setze Status Label: Fehler!
						this.propView.getStateLabel().setForeground(Color.red);
						this.propView.getStateLabel().setText("State: File found, but can't read!");
						
					}
					
				}else{
					//setze Status Label: Fehler!
					this.propView.getStateLabel().setForeground(Color.red);
					this.propView.getStateLabel().setText("State: File found, but can't read!");
					
				}
				
			}else{
				//setze Status Label: Fehler!
				this.propView.getStateLabel().setForeground(Color.red);
				this.propView.getStateLabel().setText("State: File not found!");
				
			}
			
		}
		
		//Geocoder
		if( command.equals("Google") ){
			this.actuellProps.put("Geocoder", "Google");
		}
		if( command.equals("CloudMade") ){
			this.actuellProps.put("Geocoder", "CloudMade");
		}
	    if( command.equals("Apply Key") ){
	    	
	    	String key = this.propView.getTxtApiKey().getText();
	    	confirmAPIKey(key);
	    }
	    
	    //Distanzbestimmung
	    if( command.equals("Osm2po")){
	    	this.actuellProps.put("Distance", "Osm2po");
	    }
	    if( command.equals("CloudMade Car")){
	    	this.actuellProps.put("Distance", "CloudMade Car");
	    }
	    if( command.equals("CloudMade Bicycle")){
	    	this.actuellProps.put("Distance", "CloudMade Bicycle");
	    }
	    if( command.equals("Linear Distance")){
	    	this.actuellProps.put("Distance", "Linear Distance");
	    }
	    
	    //Routenberechnung
	    if( command.equals("very fast low")){
	    	this.actuellProps.put("Route", "very fast low");
	    }
	    if( command.equals("fast low")){
	    	this.actuellProps.put("Route", "fast low");
	    }
	    if( command.equals("middle low")){
	    	this.actuellProps.put("Route", "middle low");
	    }
	    if( command.equals("time-consuming low")){
	    	this.actuellProps.put("Route", "time-consuming low");
	    }
	    if( command.equals("very time-consuming low")){
	    	this.actuellProps.put("Route", "very time-consuming low");
	    }
	    
	    if( command.equals("very fast high")){
	    	this.actuellProps.put("Route", "very fast high");
	    }
	    if( command.equals("fast high")){
	    	this.actuellProps.put("Route", "fast high");
	    }
	    if( command.equals("middle high")){
	    	this.actuellProps.put("Route", "middle high");
	    }
	    if( command.equals("time-consuming high")){
	    	this.actuellProps.put("Route", "time-consuming high");
	    }
	    if( command.equals("very time-consuming high")){
	    	this.actuellProps.put("Route", "very time-consuming high");
	    }
		
	    //Übernehme Output-File
	    if( command.equals("Apply Output") ){
	    	String output = this.propView.getOutputTextField().getText();
	    	
	    	File file = new File( "./", output+".tmp" );
	    	try {
	    		//setze StateLabel
				file.createNewFile();
				this.propView.getOutputStateLabel().setForeground(Color.green);
				this.propView.getOutputStateLabel().setText("State: Output applied successful!");
				file.delete();
				this.actuellProps.put("Output File", output);
				
			} catch (IOException e) {
				//setze State Label bei Fehler
				this.propView.getOutputStateLabel().setForeground(Color.red);
				this.propView.getOutputStateLabel().setText("State: Can't write to output location!");
				
			}
	    	
	    	
	    }
	    //Überschreibe existierendes Output-File
	    if( command.equals("override existing file") ){
	    	JCheckBox override = (JCheckBox) ae.getSource();
	    	if( override.isSelected() ) this.actuellProps.put("Override", "true");
	    	else this.actuellProps.put("Override", "false");
	    }
	    //übernehme aktuell gesetzte Eigenschaften ins Model und schreibe Propertie Datei neu
	    if( command.equals("OK") ){
	    	
	    	this.plannerModel.writeProps(this.actuellProps);
	    	this.plannerModel.setActuellProps(this.actuellProps);
	    	
	    	//schließe Properties Fenster
	    	this.propView.release();
	    }
	    
	    //Setze auf die alten Eigenschaften zurück
	    if( command.equals("Cancel") ){
	    	this.propView.setPropertieFields( this.plannerModel.getActuellProps() );
	    	this.propView.release();
	    }
	    
	    //Setze Default Eigenschaften
	    if( command.equals("Choose Defaults") ){
	    	this.actuellProps = PlannerModel.defaultProps;
	    	this.propView.setPropertieFields( this.actuellProps );
	    	
	    }
		
	}
	
	/**
	 * Überprüfen der Korrektheit des API-Keys, indem versucht wird ein Adresse zu geokodieren.
	 * @param key
	 */
	public void confirmAPIKey(String key){
		URL url = null;
		int attempt = 0;
		InputStream inputStreamJson = null;
		
		String addr = "133+Fleet+street,+London,+UK";
		
		//Erstelle URL
		try{
			url = new URL( CloudMadeGeocoder.urlCloudMade+key+CloudMadeGeocoder.urlGeocoding+addr);
		}catch(MalformedURLException mue){
			mue.printStackTrace();
		}
		//System.out.println(url.toString());
		//Verbindung herstellen (max. 3 Verbindungsversuche)
		while(attempt < 3){
			System.out.println(attempt);
			try{
				URLConnection con = url.openConnection();
				con.setDoInput(true);
				con.connect();
				inputStreamJson = con.getInputStream();
				break;
			}catch(IOException ioe){
				this.propView.getKeyStateLabel().setForeground(Color.red);
				this.propView.getKeyStateLabel().setText("State: Connection Error "+(attempt+1)+"! ");
				//ioe.printStackTrace();
			}
			attempt++;
		}
		//Programm beenden, bei 3 fehlgeschlagenen Verbindungen
		if(attempt == 3){
			this.propView.getKeyStateLabel().setForeground(Color.red);
			this.propView.getKeyStateLabel().setText("State: Error while using Key!" );
		}
		else{
			this.propView.getKeyStateLabel().setForeground(Color.green);
			this.propView.getKeyStateLabel().setText("State: Key applied successful!" );
			this.actuellProps.put("APIKEY", key);
			System.out.println("success");
			try{
				inputStreamJson.close();
			}catch( IOException ioe ){
				ioe.printStackTrace();
			}
		}
		
		
	}
	
	/**
	* Diese Methode wird automatisch gerufen, wenn das Fenster des Views geschlossen
	* wird. Diese Eigenschaft hat der Controller daher, dass er von WindowAdapter
	* abgeleitet wurde. 
	* Diese Methode veranlasst in diesem Bsp nur den View, sich selbst zu schliessen.
	* @param e Das System uebergibt beim Aufruf der Methode ein WindowEvent.
	*/
	public void windowClosing( WindowEvent e)
	{
		this.propView.release();
	}

	/**
	* Diese Methode gibt dem Controller die Moeglichkeit, abschliessende Massnahmen zu
	* unternehmen, bevor er vom View zerstoert wird. In diesem Fall werden View und
	* Model mit in die ewigen Jagdgruende gerissen.
	*/
	public void release()
	{
		this.plannerModel = null;
		this.propView = null;
	}
	
}

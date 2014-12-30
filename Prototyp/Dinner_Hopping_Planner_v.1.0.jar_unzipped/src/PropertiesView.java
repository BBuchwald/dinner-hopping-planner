//Collections
import java.util.HashMap;
//Observer Pattern
import java.util.Observable;
import java.util.Observer;
//SWING
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;

/**
 * Fenster der Programm-Eigenschaften, Einstellen von Input, Geocoding, Distanzbestimmung,
 * Routenberechnung und Output.
 * @author Björn Buchwald
 *
 */
public class PropertiesView extends JFrame implements Observer{
	
	private static final long serialVersionUID = 655277140319527389L;

	/**
	 * Textfeld API-Key.
	 */
	private JTextField txtApiKey;
	
	/**
	 * Textfeld für das Input-File
	 */
	private JTextField fileTextField;
	
	/**
	 * Textfeld des output-File
	 */
	private JTextField outputTextField;

	/**
	 * Status Anzeige Input File
	 */
	private JLabel stateLabel;
	
	/**
	 * Status Anzeige des API-Key
	 */
	private JLabel keyStateLabel;
	
	/**
	 * Status Anzeige des Output Files.
	 */
	private JLabel outputStateLabel;
	
	/** Das Modell des Programms liegt als Klassenvariable im View. */
	private PlannerModel model;

	/** Der zu diesem View gehoerige Controller ist ebenfalls im View enthalten. */
	private PropertiesController controller;
	
	/**
	 * Radio Button Gruppe Geokoder Tab
	 */
	private final ButtonGroup geocoderButtonGroup = new ButtonGroup();
	
	/**
	 * Radio Button Gruppe Distanzberechnung Tab
	 */
	private final ButtonGroup distanceButtonGroup = new ButtonGroup();

	/**
	 * RadioButton Google Geokoder
	 */
	private JRadioButton rdbtnGoogle;

	/**
	 * RadioButton CloudMade Geokoder
	 */
	private JRadioButton rdbtnCloudmade;

	/**
	 * RadioButton Osm2po Distanz
	 */
	private JRadioButton rdbtnOsmpo;

	/**
	 * RadioButton CloudMade Distanz Auto
	 */
	private JRadioButton rdbtnCloudmadeCar;

	/**
	 * RadioButton CloudMade Distanz Fahrrad
	 */
	private JRadioButton rdbtnCloudmadeBicycle;

	/**
	 * RadioButton Luftlinienberechnung
	 */
	private JRadioButton rdbtnLinearButton;

	/**
	 * CheckBox Output File überschreiben
	 */
	private JCheckBox overrideCheckBox;

	/**
	 * RadioButton Routenberechnung
	 */
	private JRadioButton rdbtnVeryFastLow;

	/**
	 * RadioButton Routenberechnung
	 */
	private JRadioButton rdbtnFastLow;

	/**
	 * RadioButton Routenberechnung
	 */
	private JRadioButton rdbtnMiddleLow;

	/**
	 * RadioButton Routenberechnung
	 */
	private JRadioButton rdbtnTimeconsumingLow;

	/**
	 * RadioButton Routenberechnung
	 */
	private JRadioButton rdbtnVeryTimeconsumingLow;
	
	/**
	 * Radio Button Gruppe Distanzbestimmung
	 */
	private final ButtonGroup buttonGroup = new ButtonGroup();
	
	/**
	 * RadioButton Routenberechnung
	 */
	private JRadioButton rdbtnVeryFastHigh;
	
	/**
	 * RadioButton Routenberechnung
	 */
	private JRadioButton rdbtnFastHigh;
	
	/**
	 * RadioButton Routenberechnung
	 */
	private JRadioButton rdbtnMiddleHigh;
	
	/**
	 * RadioButton Routenberechnung
	 */
	private JRadioButton rdbtnTimeconsumingHigh;

	/**
	 * RadioButton Routenberechnung
	 */
	private JRadioButton rdbtnVeryTimeconsumingHigh;

	/**
	 * Erstellt das View des Programmeigenschaften-Fensters nach MVC.
	 * @param neuesModel das Model des Programmes
	 */
	public PropertiesView(PlannerModel neuesModel) {
		// Model uebernehmen
		this.model = neuesModel;
				
		initializeMVC();
		
		//setze GUI
		initialize();
		
		//setze Felder entsprechend den aktuellen Eigenschaften im Model
		setPropertieFields(this.model.getActuellProps());
		
	}
	
	/**
	 * Setzt die actuellen Eigenschaften im Properties-Fenster.
	 * @param map
	 */
	public void setPropertieFields( HashMap<String, String> map){
		
		//setze Text Felder
		this.fileTextField.setText( map.get("Input File") );
		this.txtApiKey.setText( map.get("APIKEY") );
		this.outputTextField.setText( map.get("Output File"));
		//setze CheckBox Override
		if( map.get("Override").equals("false") ) 
			this.overrideCheckBox.setSelected(false);
		else this.overrideCheckBox.setSelected(true);
		
		//setze einzelne RadioButton
		switch( map.get("Geocoder") ){
			case "Google": rdbtnGoogle.setSelected(true); break; //geocoderButtonGroup.setSelected( rdbtnGoogle.getModel() , true); break;
			case "CloudMade": rdbtnCloudmade.setSelected(true); break;
		}
		
		switch( map.get("Distance") ){
			case "Osm2po": rdbtnOsmpo.setSelected(true); break;
			case "CloudMade Car": rdbtnCloudmadeCar.setSelected(true); break;
			case "CloudMade Bicycle": rdbtnCloudmadeBicycle.setSelected(true); break;
			case "Linear Distance": rdbtnLinearButton.setSelected(true); break;
		}
		
		switch( map.get("Route") ){
			case "very fast low": rdbtnVeryFastLow.setSelected(true); break;
			case "fast low": rdbtnFastLow.setSelected(true); break;
			case "middle low": rdbtnMiddleLow.setSelected(true); break;
			case "time-consuming low": rdbtnTimeconsumingLow.setSelected(true); break;
			case "very time-consuming low": rdbtnVeryTimeconsumingLow.setSelected(true); break;
			case "very fast high": rdbtnVeryFastHigh.setSelected(true); break;
			case "fast high": rdbtnFastHigh.setSelected(true); break;
			case "middle high": rdbtnMiddleHigh.setSelected(true); break;
			case "time-consuming high": rdbtnTimeconsumingHigh.setSelected(true); break;
			case "very time-consuming high": rdbtnVeryTimeconsumingHigh.setSelected(true); break;
		}
		
	}
	

	/**
	 * Initialsiert den Inhalt des Properties-Fensters.
	 */
	private void initialize() {
		
		//Frame Eigenschaften
		new JFrame();
		setType(Type.UTILITY);
		setTitle("Properties");
		setAlwaysOnTop(true);
		this.setResizable(false);
		
		setBounds(100, 100, 690, 377);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//setze GUI
		
		getContentPane().setLayout(null);
		//Tabs (pro Tab ein JPanel)
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, 674, 303);
		getContentPane().add(tabbedPane);
		
		//Input-File Tab
		//Panel das Eigenschaften für Input-Datei enthält
		JPanel panel = new JPanel();
		tabbedPane.addTab("File", null, panel, null);
		panel.setLayout(null);
		
		fileTextField = new JTextField();
		fileTextField.setBounds(10, 39, 189, 20);
		panel.add(fileTextField);
		fileTextField.setColumns(10);
		
		JLabel lblInputFileName = new JLabel("Input File Location");
		lblInputFileName.setBounds(10, 14, 136, 14);
		panel.add(lblInputFileName);
		
		JButton btnApplyFile = new JButton("Apply File");
		btnApplyFile.setBounds(243, 38, 89, 23);
		panel.add(btnApplyFile);
		btnApplyFile.addActionListener(this.controller);
		
		this.stateLabel = new JLabel("State: ");
		stateLabel.setBounds(10, 70, 615, 14);
		panel.add(stateLabel);
		
		//Geocoder Tab
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("Geocoder", null, panel_1, null);
		panel_1.setLayout(null);
		
		rdbtnGoogle = new JRadioButton("Google");
		geocoderButtonGroup.add(rdbtnGoogle);
		rdbtnGoogle.setBounds(6, 42, 109, 23);
		panel_1.add(rdbtnGoogle);
		rdbtnGoogle.addActionListener(this.controller);
		geocoderButtonGroup.setSelected( rdbtnGoogle.getModel() , true);
		
		rdbtnCloudmade = new JRadioButton("CloudMade");
		geocoderButtonGroup.add(rdbtnCloudmade);
		rdbtnCloudmade.setBounds(113, 42, 109, 23);
		panel_1.add(rdbtnCloudmade);
		rdbtnCloudmade.addActionListener(this.controller);
		
		JLabel lblCooseAGeocoder = new JLabel("Choose a Geocoder");
		lblCooseAGeocoder.setBounds(6, 11, 184, 24);
		panel_1.add(lblCooseAGeocoder);
		
		txtApiKey = new JTextField();
		
		txtApiKey.setBounds(6, 112, 184, 20);
		panel_1.add(txtApiKey);
		txtApiKey.setColumns(10);
		
		JLabel lblCloudmadeApiKey = new JLabel("CloudMade API Key");
		lblCloudmadeApiKey.setBounds(6, 87, 148, 14);
		panel_1.add(lblCloudmadeApiKey);
		
		JButton btnApplyKey = new JButton("Apply Key");
		btnApplyKey.setBounds(221, 111, 89, 23);
		panel_1.add(btnApplyKey);
		btnApplyKey.addActionListener(this.controller);
		
		this.keyStateLabel = new JLabel("State: ");
		keyStateLabel.setBounds(6, 143, 216, 14);
		panel_1.add(keyStateLabel);
		btnApplyKey.addActionListener(this.controller);
		
		//Distance Service Tab
		JPanel panel_2 = new JPanel();
		tabbedPane.addTab("Distance Service", null, panel_2, null);
		panel_2.setLayout(null);
		
		JLabel lblChooseADistance = new JLabel("Choose a Distance Service");
		lblChooseADistance.setBounds(10, 11, 183, 14);
		panel_2.add(lblChooseADistance);
		
		this.rdbtnOsmpo = new JRadioButton("Osm2po");
		distanceButtonGroup.add(rdbtnOsmpo);
		rdbtnOsmpo.setBounds(6, 32, 90, 23);
		panel_2.add(rdbtnOsmpo);
		rdbtnOsmpo.addActionListener(this.controller);
		
		this.rdbtnCloudmadeCar = new JRadioButton("CloudMade Car");
		distanceButtonGroup.add(rdbtnCloudmadeCar);
		rdbtnCloudmadeCar.setBounds(98, 32, 125, 23);
		panel_2.add(rdbtnCloudmadeCar);
		rdbtnCloudmadeCar.addActionListener(this.controller);
		
		this.rdbtnCloudmadeBicycle = new JRadioButton("CloudMade Bicycle");
		distanceButtonGroup.add(rdbtnCloudmadeBicycle);
		rdbtnCloudmadeBicycle.setBounds(225, 32, 142, 23);
		panel_2.add(rdbtnCloudmadeBicycle);
		rdbtnCloudmadeBicycle.addActionListener(this.controller);
		
		this.rdbtnLinearButton = new JRadioButton("Linear Distance");
		distanceButtonGroup.add(rdbtnLinearButton);
		rdbtnLinearButton.setBounds(369, 32, 127, 23);
		panel_2.add(rdbtnLinearButton);
		rdbtnLinearButton.addActionListener(this.controller);
		
		//Routenberechnung Tab
		JPanel panel_3 = new JPanel();
		tabbedPane.addTab("Route Calculation", null, panel_3, null);
		panel_3.setLayout(null);
		
		rdbtnVeryFastLow = new JRadioButton("very fast");
		rdbtnVeryFastLow.setActionCommand("very fast low");
		buttonGroup.add(rdbtnVeryFastLow);
		rdbtnVeryFastLow.setBounds(10, 32, 97, 23);
		panel_3.add(rdbtnVeryFastLow);
		rdbtnVeryFastLow.addActionListener(this.controller);
		
		JLabel lblNewLabel_1 = new JLabel("Level of search depth recommended for 9 up to 15 teams (slower speed often comes to better results)");
		lblNewLabel_1.setBounds(10, 11, 626, 14);
		panel_3.add(lblNewLabel_1);
		
		rdbtnFastLow = new JRadioButton("fast");
		rdbtnFastLow.setActionCommand("fast low");
		buttonGroup.add(rdbtnFastLow);
		rdbtnFastLow.setBounds(132, 32, 84, 23);
		panel_3.add(rdbtnFastLow);
		rdbtnFastLow.addActionListener(this.controller);
		
		rdbtnMiddleLow = new JRadioButton("middle");
		rdbtnMiddleLow.setActionCommand("middle low");
		buttonGroup.add(rdbtnMiddleLow);
		rdbtnMiddleLow.setBounds(246, 32, 109, 23);
		panel_3.add(rdbtnMiddleLow);
		rdbtnMiddleLow.addActionListener(this.controller);
		
		rdbtnTimeconsumingLow = new JRadioButton("time-consuming");
		rdbtnTimeconsumingLow.setActionCommand("time-consuming low");
		buttonGroup.add(rdbtnTimeconsumingLow);
		rdbtnTimeconsumingLow.setBounds(357, 32, 126, 23);
		panel_3.add(rdbtnTimeconsumingLow);
		rdbtnTimeconsumingLow.addActionListener(this.controller);
		
		rdbtnVeryTimeconsumingLow = new JRadioButton("very time-consuming");
		rdbtnVeryTimeconsumingLow.setActionCommand("very time-consuming low");
		buttonGroup.add(rdbtnVeryTimeconsumingLow);
		rdbtnVeryTimeconsumingLow.setBounds(492, 32, 144, 23);
		panel_3.add(rdbtnVeryTimeconsumingLow);
		rdbtnVeryTimeconsumingLow.addActionListener(this.controller);
		
		rdbtnVeryFastHigh = new JRadioButton("very fast");
		rdbtnVeryFastHigh.setActionCommand("very fast high");
		buttonGroup.add(rdbtnVeryFastHigh);
		rdbtnVeryFastHigh.setBounds(10, 112, 109, 23);
		panel_3.add(rdbtnVeryFastHigh);
		rdbtnVeryFastHigh.addActionListener(this.controller);
		
		rdbtnFastHigh = new JRadioButton("fast");
		rdbtnFastHigh.setActionCommand("fast high");
		buttonGroup.add(rdbtnFastHigh);
		rdbtnFastHigh.setBounds(132, 112, 109, 23);
		panel_3.add(rdbtnFastHigh);
		rdbtnFastHigh.addActionListener(this.controller);
		
		rdbtnMiddleHigh = new JRadioButton("middle");
		rdbtnMiddleHigh.setActionCommand("middle high");
		buttonGroup.add(rdbtnMiddleHigh);
		rdbtnMiddleHigh.setBounds(246, 112, 109, 23);
		panel_3.add(rdbtnMiddleHigh);
		rdbtnMiddleHigh.addActionListener(this.controller);
		
		rdbtnTimeconsumingHigh = new JRadioButton("time-consuming");
		rdbtnTimeconsumingHigh.setActionCommand("time-consuming high");
		buttonGroup.add(rdbtnTimeconsumingHigh);
		rdbtnTimeconsumingHigh.setBounds(357, 112, 126, 23);
		panel_3.add(rdbtnTimeconsumingHigh);
		rdbtnTimeconsumingHigh.addActionListener(this.controller);
		
		rdbtnVeryTimeconsumingHigh = new JRadioButton("very time-consuming");
		rdbtnVeryTimeconsumingHigh.setActionCommand("very time-consuming high");
		buttonGroup.add(rdbtnVeryTimeconsumingHigh);
		rdbtnVeryTimeconsumingHigh.setBounds(492, 112, 144, 23);
		panel_3.add(rdbtnVeryTimeconsumingHigh);
		rdbtnVeryTimeconsumingHigh.addActionListener(this.controller);
		
		JLabel lblLevelOfSearch = new JLabel("Level of search depth recommended for equals to or greater than 15 teams");
		lblLevelOfSearch.setBounds(10, 91, 601, 14);
		panel_3.add(lblLevelOfSearch);
		
		JLabel lblToChangeSettings = new JLabel("To change settings see file RouteConfigurations.properties!");
		lblToChangeSettings.setBounds(10, 253, 601, 14);
		panel_3.add(lblToChangeSettings);
		
		//Output-File Tab
		JPanel panel_4 = new JPanel();
		tabbedPane.addTab("Output", null, panel_4, null);
		panel_4.setLayout(null);
		
		outputTextField = new JTextField();
		outputTextField.setBounds(10, 39, 175, 20);
		panel_4.add(outputTextField);
		outputTextField.setColumns(10);
		
		JLabel lblOutputFileLocation = new JLabel("Output File Location");
		lblOutputFileLocation.setBounds(10, 14, 147, 14);
		panel_4.add(lblOutputFileLocation);
		
		JButton btnApplyOutput = new JButton("Apply Output");
		btnApplyOutput.setBounds(222, 38, 115, 23);
		panel_4.add(btnApplyOutput);
		btnApplyOutput.addActionListener(this.controller);
		
		this.outputStateLabel = new JLabel("State: ");
		outputStateLabel.setBounds(10, 70, 215, 14);
		panel_4.add(outputStateLabel);
		
		this.overrideCheckBox = new JCheckBox("override existing file");
		overrideCheckBox.setBounds(10, 108, 175, 23);
		panel_4.add(overrideCheckBox);
		overrideCheckBox.addActionListener(this.controller);
		
		//unteres Panel mit Kontrollelementen 
		JPanel panel_5 = new JPanel();
		panel_5.setBounds(0, 302, 674, 37);
		getContentPane().add(panel_5);
		panel_5.setLayout(null);
		
		JButton btnOk = new JButton("OK");
		btnOk.setBounds(476, 11, 89, 23);
		panel_5.add(btnOk);
		btnOk.addActionListener(this.controller);
		
		JButton btnAbort = new JButton("Cancel");
		btnAbort.setBounds(575, 11, 89, 23);
		panel_5.add(btnAbort);
		btnAbort.addActionListener(this.controller);
		
		JButton btnChooseDefaults = new JButton("Choose Defaults");
		btnChooseDefaults.setBounds(10, 11, 135, 23);
		panel_5.add(btnChooseDefaults);
		btnChooseDefaults.addActionListener(this.controller);
	}
	
	/**
	 * @return the textField
	 */
	public JTextField getFileTextField() {
		return fileTextField;
	}

	/**
	 * @param textField the textField to set
	 */
	public void setFileTextField(JTextField textField) {
		this.fileTextField = textField;
	}
	
	/**
	 * @return the lblNewLabel
	 */
	public JLabel getStateLabel() {
		return stateLabel;
	}

	/**
	 * @param lblNewLabel the lblNewLabel to set
	 */
	public void setStateLabel(JLabel lblNewLabel) {
		this.stateLabel = lblNewLabel;
	}
	
	/**
	 * @return the txtApiKey
	 */
	public JTextField getTxtApiKey() {
		return txtApiKey;
	}

	/**
	 * @param txtApiKey the txtApiKey to set
	 */
	public void setTxtApiKey(JTextField txtApiKey) {
		this.txtApiKey = txtApiKey;
	}
	
	/**
	 * @return the keyStateLabel
	 */
	public JLabel getKeyStateLabel() {
		return keyStateLabel;
	}

	/**
	 * @return the outputStateLabel
	 */
	public JLabel getOutputStateLabel() {
		return outputStateLabel;
	}

	/**
	 * @param outputStateLabel the outputStateLabel to set
	 */
	public void setOutputStateLabel(JLabel outputStateLabel) {
		this.outputStateLabel = outputStateLabel;
	}

	/**
	 * @return the outputTextField 
	 */
	public JTextField getOutputTextField() {
		return outputTextField;
	}

	/**
	 * @param outputTextField the outputTextField to set
	 */
	public void setOutputTextField(JTextField outputTextField) {
		this.outputTextField = outputTextField;
	}
	
	/**
	 * Diese Methode fuehrt die zum Starten des MVC wichtigen Schritte aus. Dabei
	 * wird diesem View ein Controller erstellt und der Controller als WindowListener
	 * angemeldet. Ausserdem wird dieser View beim Modell als Observer angemeldet.
	 * Diese Methode kann fuer die meisten MVC-Anwendungen sicher unveraendert bleiben.
	 */
	private void initializeMVC()
	{
		// Der View meldet beim Model an, ueber evtl. Aenderungen im Model
		// informiert werden zu wollen.
		this.model.addObserver(this);

		// diesem View einen eigenen Controller erstellen
		this.controller = new PropertiesController(this, this.model);
		
		// dem Controller erlauben, auf Fensteraktionen dieses Fensters 
		// (z.B. Schliessen des Fensters) reagieren zu duerfen
		addWindowListener( this.controller);
		
        addWindowFocusListener(controller);
        addWindowStateListener(controller);
		
	}
	
	/**
	 * Dies ist eine Kernmethode des MVC: Sollte sich im PlannerModel
	 * etwas aendern, so bekommt der View das dadurch mit, dass DIESE Methode
	 * des Views aufgerufen wird. Dies geschieht automatisch dadurch, dass der View
	 * beim Modell als Observer angemeldet ist.
	 * Der View passt sich mit dieser Methode selbst an, indem er die neuen Daten
	 * aus dem Modell holt und sie in seine View-Komponenten steckt.
	 * @param neuesModel Es wird ein ganz neues Modell mitgeliefert. Dies ist in der Klasse
	 * Observable so vorgesehen, waere aber nicht notwendig: Dieser View enthaelt das
	 * Modell, kann also jeder Zeit auf ein aktuelles Modell zugreifen.
	 * @param arg ein Objekt, welches das Modell ggf der Methode notifyObservers(Object arg) 
	 * mitgegeben hat.
	 */
	
	public void update( Observable neuesModel, Object arg)
	{
		// neues Model annehmen
	//PlannerModel m = (PlannerModel) neuesModel;
		/*
		// neue Werte aus dem Model holen
		this.figur = m.getFigur();
		
		//neue Werte darstellen
		panel = new KurvenPanel(figur,900,500);
		win.remove(box);
		setGUI();
		*/
		initialize();
	}
	
	/**
	 * Diese Methode beendet diesen View. Ausserdem werden der Controller des Views
	 * zerstoert und der View am Model des Programmes abgemeldet. Das Model anschliessend
	 * zu zerstoeren ist nicht notwendig, das Model beendet sich selbstaendig, wenn
	 * es keinen Observer mehr gibt.
	 */
	
	public void release()
	{
		// dem Controller die Chance zu abschliessenden Massnahmen geben
		this.controller.release();
		// Controller zerstoeren
		this.controller = null;
		// diesen View (also das Fenster) mit allen Teilen und Kindern schliessen
		dispose();
		
		// diesen View vom Model abmelden
		this.model.deleteObserver( this);
	}
}

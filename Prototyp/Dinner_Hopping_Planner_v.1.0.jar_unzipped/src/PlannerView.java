//Observer Pattern
import java.util.Observable;
import java.util.Observer;
//SWING
import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.border.TitledBorder;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;
//AWT 
import java.awt.Color;

/**
 * View des Planners nach MVC, Erstellt die GUI des Programms in einem JFrame. Implementiert Observer Interface.
 * @author Björn Buchwald
 *
 */
public class PlannerView extends JFrame implements Observer{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2505030174111126891L;
	
	/**
	 * RadioButton Gruppe Auswahl im Viewer anzuzeigender Teams
	 */
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	
	/**
	 * CheckBox Abfrage, ob ausschließlich Orte angezeigt werden sollen.
	 */
	private JCheckBox chckbxLocationsOnly;
	
	/**
	 * ComboBox Auswahl einer Routenplanung.
	 */
	private JComboBox<String> resultsComboBox;

	/**
	 * ComboBox Auswahl, ob Anzeige auf Teams oder Orten beruhen soll.
	 */
	private JComboBox<String> depOnComboBox;
	
	/**
	 * ComboBox Auswahl nach welchem Kriterium die Routenplanungen gefiltert werden sollen.
	 */
	private JComboBox<String> sortComboBox;
	

	/**
	 * Liste zeigt Teams oder Orte die zur Anzeige im Viewer ausgewählt werden können.
	 */
	private JList<String> list;

	/** Das Modell des Programms liegt als Klassenvariable im View. */
	private PlannerModel model;

	/** Der zu diesem View gehoerige Controller ist ebenfalls im View enthalten. */
	private PlannerController controller;
	
	/**
	 * Launch the application.
	 *//*
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PlannerView frame = new PlannerView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/
	
	/**
	 * Erstellen des Frames und aller Komponenten der GUI.
	 * @param neuesModel das Model des Programmes
	 */
	public PlannerView(PlannerModel neuesModel) {
		
		// Model uebernehmen
		this.model = neuesModel;
		
		initializeMVC();
		
		//Frame
		setTitle("Dinner Hopping Planner v1.0 ( Instance "+this.model.getInstanceArray().get(0)+" )");
		getContentPane().setLayout(null);
		
		//Label Auswahlbox Routenplanungen
		JLabel lblChooseResult = new JLabel("Choose Result");
		lblChooseResult.setToolTipText("Choose one of the result calculated by the planner. \r\nEach entry shows the id and the distance \r\ndepending on the \"Results sorted by\" field.");
		lblChooseResult.setBounds(10, 77, 97, 20);
		getContentPane().add(lblChooseResult);
		
		//Auswahlbox für Routenplanungen
		this.resultsComboBox = new JComboBox<String>();
		resultsComboBox.setToolTipText("Choose one of the result calculated by the planner. \r\nEach entry shows the id and the distance \r\ndepending on the \"Results sorted by\" field.");
		this.resultsComboBox.setModel(new DefaultComboBoxModel<String>());
		this.resultsComboBox.setBounds(143, 77, 204, 20);
		getContentPane().add(this.resultsComboBox);
		resultsComboBox.setActionCommand("ResultsComboBox");
		resultsComboBox.addActionListener(this.controller);
		
		JLabel lblViewDependingOn = new JLabel("Items depending on");
		lblViewDependingOn.setToolTipText("Select routes of teams or \r\nroutes over locations in the list on the left. ");
		lblViewDependingOn.setBounds(312, 234, 125, 14);
		getContentPane().add(lblViewDependingOn);
		
		//CheckBox: Nur Orte anzeigen?
		this.chckbxLocationsOnly = new JCheckBox("Locations only");
		chckbxLocationsOnly.setToolTipText("See only location markers without routes \r\non the dinner hopping viewer.");
		this.chckbxLocationsOnly.setBounds(10, 117, 132, 23);
		getContentPane().add(this.chckbxLocationsOnly);
		this.chckbxLocationsOnly.addActionListener(this.controller);
		
		//Abtrennbalken horizontal
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 108, 482, 2);
		getContentPane().add(separator_1);
		
		//Liste beruhend auf: Orten oder Teams?
		depOnComboBox = new JComboBox<String>();
		depOnComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {}));
		depOnComboBox.setBounds(312, 259, 180, 20);
		depOnComboBox.setActionCommand("DependingComboBox");
		getContentPane().add(depOnComboBox);
		depOnComboBox.setToolTipText("Select routes of teams or \r\nroutes over locations in the list on the left. ");
		depOnComboBox.addActionListener(this.controller);
		
		//scrollen der Liste
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 234, 279, 308);
		getContentPane().add(scrollPane);
		//Liste mit Teams bzw. Orten
		this.list = new JList<String>();
		scrollPane.setViewportView(list);
		list.setToolTipText("Select one or more items in this list. \r\nThe routes will be displaying in the dinner hopping viewer.");
		list.setModel(new AbstractListModel<String>() {
			
			private static final long serialVersionUID = -3051196958535232811L;
			
			String[] values = new String[] {};
			public int getSize() {
				return values.length;
			}
			public String getElementAt(int index) {
				return values[index];
			}
		});
		
		//Label Liste
		list.setBorder(new TitledBorder(null, "Select items to view", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		list.addListSelectionListener(this.controller);
		
		//RadioButton für vorgefertigte Routenauswahlen
		JRadioButton rdbtnAllRoutes = new JRadioButton("All Routes");
		rdbtnAllRoutes.setToolTipText("Select all routes of this computed result to see on the viewer.");
		rdbtnAllRoutes.setActionCommand("All Routes");
		buttonGroup_1.add(rdbtnAllRoutes);
		rdbtnAllRoutes.setBounds(10, 174, 89, 23);
		getContentPane().add(rdbtnAllRoutes);
		rdbtnAllRoutes.addActionListener(this.controller);
		
		JRadioButton rdbtnStarterTeams = new JRadioButton("Starter Teams");
		rdbtnStarterTeams.setToolTipText("Select only routes of teams which are responsible \r\nfor the first course (starter).");
		rdbtnStarterTeams.setActionCommand("Starter Teams");
		buttonGroup_1.add(rdbtnStarterTeams);
		rdbtnStarterTeams.setBounds(101, 174, 109, 23);
		getContentPane().add(rdbtnStarterTeams);
		rdbtnStarterTeams.addActionListener(this.controller);
		
		JRadioButton rdbtnMainCourseTeams = new JRadioButton("Main Course Teams");
		rdbtnMainCourseTeams.setToolTipText("Select only routes of teams which are responsible \r\nfor the second course (main).");
		rdbtnMainCourseTeams.setActionCommand("Main Course Teams");
		buttonGroup_1.add(rdbtnMainCourseTeams);
		rdbtnMainCourseTeams.setBounds(212, 174, 144, 23);
		getContentPane().add(rdbtnMainCourseTeams);
		rdbtnMainCourseTeams.addActionListener(this.controller);
		
		JRadioButton rdbtnDessertTeams = new JRadioButton("Dessert Teams");
		rdbtnDessertTeams.setToolTipText("Select only routes of teams which are responsible \r\nfor the third course (dessert).");
		rdbtnDessertTeams.setActionCommand("Dessert Teams");
		buttonGroup_1.add(rdbtnDessertTeams);
		rdbtnDessertTeams.setBounds(358, 174, 134, 23);
		getContentPane().add(rdbtnDessertTeams);
		rdbtnDessertTeams.addActionListener(this.controller);
		
		//Label vorgefertigte Routenauswahl
		JLabel lblSelectPreselectedItems = new JLabel("Select preselected items");
		lblSelectPreselectedItems.setBounds(10, 147, 153, 14);
		getContentPane().add(lblSelectPreselectedItems);
		
		//Label Liste
		JLabel lblOrSelectSingle = new JLabel("or select separately");
		lblOrSelectSingle.setBounds(10, 204, 119, 14);
		getContentPane().add(lblOrSelectSingle);
		
		//Komplett neue Routenberechnung
		JButton btnCalculatAll = new JButton("New Calculation");
		btnCalculatAll.setToolTipText("Press this button to begin a new calculation with the actuell programm properties. See actuell properties by press Properties button. \r\nNote: The actuell results will have overwritten.");
		btnCalculatAll.setBackground(new Color(255, 165, 0));
		btnCalculatAll.setForeground(Color.BLACK);
		btnCalculatAll.setBounds(10, 11, 132, 23);
		getContentPane().add(btnCalculatAll);
		btnCalculatAll.addActionListener(this.controller);
		
		//Aufruf Eigenschaften Fenster 
		JButton btnProperties = new JButton("Properties");
		btnProperties.setToolTipText("Manipulate input, geocoding, distance calculation, \r\nroute planning and output settings.");
		btnProperties.setBounds(10, 553, 97, 23);
		getContentPane().add(btnProperties);
		btnProperties.addActionListener(this.controller);
		
		//Schließe Hauptfenster
		JButton btnClose = new JButton("Close");
		btnClose.setBounds(403, 553, 89, 23);
		getContentPane().add(btnClose);
		btnClose.addActionListener(this.controller);
		
		//Schreibe Output-File
		JButton btnOutput = new JButton("Output");
		btnOutput.setToolTipText("Write the selected actuell viewing result to the output file.");
		btnOutput.setBackground(new Color(154, 205, 50));
		btnOutput.setBounds(290, 553, 89, 23);
		getContentPane().add(btnOutput);
		btnOutput.addActionListener(this.controller);
		
		//Label für Filter der Routenplanungen
		JLabel lblResults = new JLabel("Results sorted by");
		lblResults.setToolTipText("Sort result for example depending on the total distance \r\nwhich will be travelling by starter teams or total distance travelling by all team.");
		lblResults.setBounds(10, 52, 119, 14);
		getContentPane().add(lblResults);
		
		//Filter für Routenplanungen
		this.sortComboBox = new JComboBox<String>();
		sortComboBox.setToolTipText("Sort result for example depending on the total distance \r\nwhich will be travelling by starter teams or total distance travelling by all team.");
		sortComboBox.setBounds(143, 45, 349, 20);
		getContentPane().add(sortComboBox);
		sortComboBox.setActionCommand("SortBy");
		sortComboBox.addActionListener(this.controller);
		
		//Erzeuge neue Instanz (Hauptfenster+Viewer) mit aktuellem Datensatz
		JButton btnNewInstance = new JButton("New Instance");
		btnNewInstance.setToolTipText("Open a new instance of the programm with \r\nthe actuell calculation results.");
		btnNewInstance.setBackground(new Color(0, 206, 209));
		btnNewInstance.setBounds(373, 11, 119, 23);
		getContentPane().add(btnNewInstance);
		btnNewInstance.addActionListener(this.controller);
		
		
		
	}
	
	/**
	 * @return the buttonGroup_1
	 */
	public ButtonGroup getButtonGroup_1() {
		return buttonGroup_1;
	}
	
	/**
	 * @return the chckbxLovationsOnly
	 */
	public JCheckBox getChckbxLocationsOnly() {
		return chckbxLocationsOnly;
	}

	/**
	 * @param chckbxLovationsOnly the chckbxLovationsOnly to set
	 */
	public void setChckbxLocationsOnly(JCheckBox chckbxLocationsOnly) {
		this.chckbxLocationsOnly = chckbxLocationsOnly;
	}
	

	/**
	 * @return the comboBox
	 */
	public JComboBox<String> getComboBox() {
		return resultsComboBox;
	}

	/**
	 * @param comboBox the comboBox to set
	 */
	public void setComboBox(JComboBox<String> comboBox) {
		this.resultsComboBox = comboBox;
	}
	
	/**
	 * @return the list
	 */
	public JList<String> getList() {
		return list;
	}

	/**
	 * @param list the list to set
	 */
	public void setList(JList<String> list) {
		this.list = list;
	}
	
	/**
	 * @return the depOnComboBox
	 */
	public JComboBox<String> getDepOnComboBox() {
		return depOnComboBox;
	}

	/**
	 * @param depOnComboBox the depOnComboBox to set
	 */
	public void setDepOnComboBox(JComboBox<String> depOnComboBox) {
		this.depOnComboBox = depOnComboBox;
	}
	
	/**
	 * @return the resultsComboBox
	 */
	public JComboBox<String> getResultsComboBox() {
		return resultsComboBox;
	}

	/**
	 * @param resultsComboBox the resultsComboBox to set
	 */
	public void setResultsComboBox(JComboBox<String> resultsComboBox) {
		this.resultsComboBox = resultsComboBox;
	}
	
	public JComboBox<String> getSortComboBox() {
		return sortComboBox;
	}

	public void setSortComboBox(JComboBox<String> sortComboBox) {
		this.sortComboBox = sortComboBox;
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
		this.controller = new PlannerController(this, this.model);
		
		// dem Controller erlauben, auf Fensteraktionen dieses Fensters 
		// (z.B. Schliessen des Fensters) reagieren zu duerfen
		addWindowListener( this.controller);
		
        addWindowFocusListener(controller);
        addWindowStateListener(controller);
		
	}
	
	/**
	 * Dies ist eine Kernmethode des MVC: Sollte sich im Model des Programmes
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

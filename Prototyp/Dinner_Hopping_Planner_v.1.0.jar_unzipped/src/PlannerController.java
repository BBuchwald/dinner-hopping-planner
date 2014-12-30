//Collections
import java.util.ArrayList;
import java.util.HashMap;
//AWT
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
//SWING
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Controller des PlannerViews nach dem MVC Prinzip. Nimmt die Eingaben des Nutzerinterfaces entgegen.
 * und führt entsprechende Operationen aus.
 * @author Björn Buchwald
 * 
 */
public class PlannerController extends WindowAdapter implements ActionListener, ListSelectionListener{

	/** Modell des Programmes */
	private PlannerModel model;

	/** zum Controller gehoeriger View */
	private PlannerView view;
	
	/**
	 * Zeigt an, ob die mögliche Auswahl zur anzeige auf dem Viewer auf
	 * Teams oder Orten beruhen soll.
	 */
	private String dependingOn;
	
	/**
	 * Zeigt an, ob im Viewer Orte mit oder ohne Routen angezeigt werden sollen.
	 */
	private boolean locationsOnly;
	
	/**
	 * Der Konstruktor nimmt das Model des Programmes und den zum Controller
	 * gehoerigen View entgegen und weist sie seinen Klassenvariablen zu.
	 * @param neuerView der zum Controller gehoerige View
	 * @param neuesModel das Model des Programmes
	 */
	public PlannerController( PlannerView plannerView, PlannerModel neuesModel){
		
		this.view = plannerView;
		this.model = neuesModel;
		
		this.dependingOn = "Teams";
		
	}
	
	/**
	 * Diese Methode wird automatisch gerufen, wenn eine "Action" im View 
	 * festgestellt wird. Diese Eigenschaft bekommt der Controller daher, dass er
	 * als "ActionListener" fuer diverse Komponenten des Views angemeldet wurde.
	 * Die Methode wertet nun die Aktionen des Users im View aus und unternimmt
	 * die notwendigen Schritte zur Verarbeitung der Eingaben.
	 * @param ActionEvent Aktion des Nutzers
	 */
	public void actionPerformed(ActionEvent ae){
		
		boolean  waypointOnly = false;
		// Annahme der vom System uebergebenen Action
		String command = ae.getActionCommand();
	    
		if( this.view.getChckbxLocationsOnly().isSelected() ) waypointOnly = true;
		
		// wenn auf Beenden geklickt wurde: View zum eigenstaendigen
		// Schliessen veranlassen
		if( command.equals("Close") ){
			this.view.dispose();
			if( this.model.getViewer() != null ) this.model.getViewer().dispose();
		}
		//Wenn New Calculation und Viewer offen => schließe alten Viewer
		if( command.equals("New Calculation") && this.model.getViewer() != null ){
			this.model.getViewer().dispose();
		}
		if( command.equals("New Calculation") ){
			
			model.initPlanner();
			//wenn Input-File nicht gelesen werden konnte breche ab
			if( this.model.getRouteConstructor() == null ) 
				return;
			//Setze Auswahlboxen (DepOn und SortBy JComboBox)
			this.view.getDepOnComboBox().setModel( new DefaultComboBoxModel<String>( new String[] {"Teams", "Locations"} ) );
			this.view.getDepOnComboBox().setSelectedItem( "Teams" );
			
			this.view.getSortComboBox().setModel( new DefaultComboBoxModel<String>( new String[] { "Total distance", "Starter teams total distance", "Main Course teams total distance", "Dessert teams total distance", "(Shortest) Longest distance, shortest total distance", "Longest distance main course, shortest total distance", "Longest distance starter, shortest starter total distance", "Longest distance main course, shortest main course total distance", "Longest distance dessert, shortest dessert total distance" } ) );
			this.view.getSortComboBox().setSelectedItem("Total distance");
					
			//initialisiere SortBy ComboBox mit "TotalDistance"
			//Funktion zur Bestimmung der Gesamtstrecken
			DefaultComboBoxModel<String> combModel = new DefaultComboBoxModel<String>();
			//Prüfe, ob Ergebnis vorhanden
			if( !this.model.getRouteConstructor().routeFound() ){
				combModel.addElement("No results found!");
				this.view.getComboBox().setModel(combModel);
				return;
			}
			
			ArrayList<Integer> sums = new ArrayList<Integer>();
			int min = model.getRouteConstructor().getDistanceSumMinimalRoutes(sums, null);
			//setze Elemente der Routenplanungsauswahlbox
			for( int i = 0; i < sums.size(); i++ ){
				//markiere Routenplanung mit minimaler Distanzsumme
				if(i == min){
					combModel.addElement( i+" - "+String.valueOf(sums.get(i))+" m"+" (minimum)" ); continue;
				}
				combModel.addElement( i+" - "+String.valueOf(sums.get(i))+" m" );
			}
			
			this.view.getComboBox().setModel(combModel);
			//selektiertes Element = Element mit minimaler Distanzsumme
			this.view.getComboBox().setSelectedIndex(min);
			//aktuell Betrachtete Routenplanung im Model setzen
			this.model.setActuellResult(min);
			
		}
		//Filter der Routenplanungen setzen
		if( command.equals("SortBy") ){
			
			setSortByComboBoxValues( this.model, this.view );
			
		}
		
		if( command.equals("ResultsComboBox") ){
			
			//lese index aktuell selektiertes Item
			JComboBox<String> rcb = this.view.getResultsComboBox();
			String result = (String) rcb.getSelectedItem();
			//System.out.println(result);
			if( result.equals("No results found!") ) return;
			int res = Integer.valueOf( result.split("-")[0].trim() );
			//initialisiere Viewer mit neuer Routenplanung
			this.model.getViewer().init(this.model.getRouteConstructor().getRoutes().get(res), this.model.getGeoAddressList() );
			
			this.model.setActuellResult(res);
			//command = "locationsOnly" bewirkt das der Viewer den Inhalt mit dem neuen result darstellt
			command = "Locations only";
			
		}
		
		if( this.model.getViewer() != null ){
			if( command.equals("Locations only") ){
				if( this.view.getChckbxLocationsOnly().isSelected() ) this.locationsOnly = true;
				else this.locationsOnly = false;
				
			}
			//wird locationsOnly CheckBox betätigt zeige nur Orte im Viewer oder umgekehrt
			if( command.equals("Locations only") && this.view.getButtonGroup_1().getSelection() != null ){
				//setze Routen im Viewer
				switch( this.view.getButtonGroup_1().getSelection().getActionCommand() ){
					case "All Routes": this.model.getViewer().setAllRoutes( this.view.getChckbxLocationsOnly().isSelected() ); break;
					case "Starter Teams": this.model.getViewer().setStarterTeamRoutes( this.view.getChckbxLocationsOnly().isSelected() ); break;
					case "Main Course Teams": this.model.getViewer().setMainTeamRoutes( this.view.getChckbxLocationsOnly().isSelected() ); break;
					case "Dessert Teams": this.model.getViewer().setDessertTeamRoutes( this.view.getChckbxLocationsOnly().isSelected() ); break;
				}
				
			}
			//setze einzelne Routen von Teams
			if( command.equals("Locations only") && !this.view.getList().isSelectionEmpty() && this.model.getRouteConstructor().routeFound() ){
			
				JList<String> list = this.view.getList();
				int[] indices = list.getSelectedIndices();
				ArrayList<Integer> idList = new ArrayList<Integer>();
				
				for(int id : indices) idList.add(id);
				//setze Teamrouten
				if(this.dependingOn.equals("Teams")){
					this.model.getViewer().setTeamRoute(idList, this.locationsOnly);
				//setze Routen die über einen Ort gehen
				}else{
					this.model.getViewer().setLocationRoute(idList, this.locationsOnly);
					
				}
			}
			
			if( command.equals("All Routes") ){
				
				this.model.getViewer().setAllRoutes( waypointOnly );
				//leere Auswahl der Liste, wenn vorgefertigte Auswahl gewählt wird
				this.view.getList().clearSelection();
				
			}
			if( command.equals("Starter Teams") ){
				
				this.model.getViewer().setStarterTeamRoutes( waypointOnly );
				this.view.getList().clearSelection();
			}
			if( command.equals("Main Course Teams") ){
				
				this.model.getViewer().setMainTeamRoutes( waypointOnly );
				this.view.getList().clearSelection();
			}
			if( command.equals("Dessert Teams") ){
		
				this.model.getViewer().setDessertTeamRoutes( waypointOnly );
				this.view.getList().clearSelection();
			}
			
			
			
		}
		//setze Listenelemente
		if( command.equals("DependingComboBox") || command.equals("New Calculation") ){
			JComboBox<String> cb = this.view.getDepOnComboBox();
			this.dependingOn = (String) cb.getSelectedItem();
			DefaultListModel<String> listModel = new DefaultListModel<String>();
			//setze Teams bzw Orte in der Liste
			for( Team team : this.model.getTeamList() ) 
				//if(dependingOn.equals("Teams")) listModel.addElement("Team: "+Letter.getLetterByNumber( team.home ) );
				//else if(dependingOn.equals("Locations")) listModel.addElement("Location: "+Letter.getLetterByNumber( team.home ) );
				if(dependingOn.equals("Teams")) listModel.addElement("Team: "+ team.home );
				else if(dependingOn.equals("Locations")) listModel.addElement("Location: "+ team.home );
	        //setze Listenelemente
	        this.view.getList().setModel(listModel);
		}
		
		if( command.equals("Properties") ){
			//zeige Properties Fenster
			PropertiesView window = new PropertiesView(this.model);
			window.setVisible(true);
			
		}
		
		if( command.equals("Output") && this.model.getRouteConstructor() != null && this.model.getRouteConstructor().routeFound() ){
			//schreibe Output-File
			this.model.writeResult();
			
		}
		//Kopie des aktuellen Datensatzes in einer neuen Instanz des Planners öffnen
		if( command.equals("New Instance") && this.model.getRouteConstructor() != null && this.model.getRouteConstructor().routeFound() ){
			
			PlannerModel meinModel = new PlannerModel();
			
			//setze das neue Model auf den berechneten Datensatz dieses Models
			
			//setze Instanz 
			meinModel.setInstanceArray(this.model.getInstanceArray()); 
			int temp = this.model.getInstanceArray().get(0); 
			meinModel.getInstanceArray().clear();
			meinModel.getInstanceArray().add(temp+1);
			meinModel.setInstance(temp+1);
			
			meinModel.setGeoAddressList( PlannerModel.copyGeoAddressList( this.model.getGeoAddressList() ) );
			meinModel.setTeamList( PlannerModel.copyTeamList( this.model.getTeamList() ) );
			TeamRoutConstructor routeConstructor = new TeamRoutConstructor( this.model.getRouteConstructor() );
			meinModel.setRouteConstructor( routeConstructor );
			//setze Eigenschaften  und Konfigurationen
			HashMap<String, String> actuellProps = new HashMap<String, String>();
			actuellProps.putAll( this.model.getActuellProps() );
			HashMap<String, String> actuellConfigurations = new HashMap<String, String>();
			actuellConfigurations.putAll( this.model.getActuellConfigurations() );
			meinModel.setActuellProps( actuellProps );
			//setze Routenplanung und Instanz
			meinModel.setActuellResult( this.model.getActuellResult() );
			meinModel.setViewer( new MapViewer( meinModel.getInstance() ) );
			//meinModel.getRouteConstructor().displayArray(meinModel.getDistances());
			
			//neues PlannerView
			PlannerView frame = new PlannerView( meinModel );
			meinModel.setPropertiesPlannerView( frame );
			
			//initialisiere Anzeige des neuen PlannerViewers (DepOn und SortBy ComboBox initialisieren)
			frame.getDepOnComboBox().setModel( new DefaultComboBoxModel<String>( new String[] {"Teams", "Locations"} ) );
			frame.getDepOnComboBox().setSelectedItem( this.view.getDepOnComboBox().getSelectedItem() );
			
			frame.getSortComboBox().setModel( new DefaultComboBoxModel<String>( new String[] { "Total distance", "Starter teams total distance", "Main Course teams total distance", "Dessert teams total distance", "(Shortest) Longest distance, shortest total distance", "Longest distance main course, shortest total distance", "Longest distance starter, shortest starter total distance", "Longest distance main course, shortest main course total distance", "Longest distance dessert, shortest dessert total distance" } ) );
			frame.getSortComboBox().setSelectedItem( this.view.getSortComboBox().getSelectedItem() );
			
			//setze Werte des Views der neuen Instanz auf die des alten Views in der SortBy ComboBox
			setSortByComboBoxValues( meinModel, frame );
			
		}
		
	}
	
	/**
	 * Setze die Werte der ComboBox zur Filterung der Routenplanungen (ComboBox), 
	 * beim neu setzen des Filters (SortComboBox) wird hier das jeweilige Minimum
	 * berechnet und die Routenplanungen in der ComboBox mit dem Minimum als 
	 * gesetztem Wert angezeigt.
	 * @param model Model aus welchem die Routenplanungen entnommen werden sollen.
	 * @param view View in dem sich die ComboBox befindet
	 */
	private void setSortByComboBoxValues(PlannerModel model, PlannerView view){
		
		JComboBox<String> sbc = this.view.getSortComboBox();
		//aktuell selektierter Modus (Filter)
		String mode = (String) sbc.getSelectedItem();
		int min = 0;
		//Array mit Distanzsummen
		ArrayList<Integer> sums = new ArrayList<Integer>();
		//Array mit den längsten Strecken der Routenplanungen
		ArrayList<Integer> longestRoutes = null;
		DefaultComboBoxModel<String> combModel = new DefaultComboBoxModel<String>();
		switch(mode){
			case "Total distance": min = model.getRouteConstructor().getDistanceSumMinimalRoutes(sums, null); break;
			case "Starter teams total distance": min = model.getRouteConstructor().getDistanceSumMinimalRoutes(sums, Course.STARTER); break;
			case "Main Course teams total distance": min = model.getRouteConstructor().getDistanceSumMinimalRoutes(sums, Course.MAIN_COURSE); break;
			case "Dessert teams total distance": min = model.getRouteConstructor().getDistanceSumMinimalRoutes(sums, Course.DESSERT); break;
			case "(Shortest) Longest distance, shortest total distance": longestRoutes = new ArrayList<Integer>();
											//min = Routenplanung mit kürzester längster Strecke, bei Gleichheit wird die Planung mit der kürzesten Gesamtstrecke gewählt
											min = model.getRouteConstructor().getShortestLongestRouteWithMinimalSum(longestRoutes, sums, null, null); 
											break;
			case "Longest distance starter, shortest starter total distance": longestRoutes = new ArrayList<Integer>();
											//min = Routenplanung mit kürzester längster Strecke der Vorspeiseteams, bei Gleichheit wird die Planung mit der kürzesten Gesamtstrecke der Vorspeiseteams gewählt
											min = model.getRouteConstructor().getShortestLongestRouteWithMinimalSum(longestRoutes, sums, Course.STARTER, Course.STARTER); 
											break;
			case "Longest distance main course, shortest main course total distance": longestRoutes = new ArrayList<Integer>();
											min = model.getRouteConstructor().getShortestLongestRouteWithMinimalSum(longestRoutes, sums, Course.MAIN_COURSE, Course.MAIN_COURSE); 
											break;
			case "Longest distance dessert, shortest dessert total distance": longestRoutes = new ArrayList<Integer>();
											min = model.getRouteConstructor().getShortestLongestRouteWithMinimalSum(longestRoutes, sums, Course.DESSERT, Course.DESSERT); 
											break;
			case "Longest distance main course, shortest total distance": longestRoutes = new ArrayList<Integer>();
											min = model.getRouteConstructor().getShortestLongestRouteWithMinimalSum(longestRoutes, sums, null, Course.MAIN_COURSE); 
											break;
		}
		
		//Prüfe, ob Ergebnis vorhanden
		if(sums.size() == 0){
			combModel.addElement("No results found!");
			this.view.getComboBox().setModel(combModel);
		}
			
		//setze minimum
		//Fall: Gesamtdistanz
		if( longestRoutes == null ){
			for( int i = 0; i < sums.size(); i++ ){
				if(i == min){
					combModel.addElement( i+" - "+String.valueOf(sums.get(i))+" m (minimum)" ); continue;
				}
				combModel.addElement( i+" - "+String.valueOf(sums.get(i))+" m" );
			}
		//Fall: longest distance
		}else{
			for( int i = 0; i < sums.size(); i++ ){
				if(i == min){
					combModel.addElement( i+"- "+String.valueOf(longestRoutes.get(i))+" m (min) total: "+sums.get(i)+" m" ); continue;
				}
				combModel.addElement( i+"- "+String.valueOf(longestRoutes.get(i))+" m total: "+sums.get(i)+" m" );
			}
		}
		
		this.view.getComboBox().setModel(combModel);
		this.view.getComboBox().setSelectedIndex(min);
		model.setActuellResult(min);
		
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
		//this.view.release();
		this.view.dispose();
		if( this.model.getViewer() != null ) this.model.getViewer().dispose();
	
	}

	/**
	* Diese Methode gibt dem Controller die Moeglichkeit, abschliessende Massnahmen zu
	* unternehmen, bevor er vom View zerstoert wird. In diesem Fall werden View und
	* Model mit in die ewigen Jagdgruende gerissen.
	*/
	public void release()
	{
		this.model = null;
		this.view = null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		//wenn Auswahl der Liste geändert wird
		if (e.getValueIsAdjusting() == true ) {
			this.view.getButtonGroup_1().clearSelection();
			
			if( !this.model.getRouteConstructor().routeFound() ) return;
			
			int[] indices = this.view.getList().getSelectedIndices();
			ArrayList<Integer> idList = new ArrayList<Integer>();
			
			for(int id : indices) idList.add(id);
			//setze anzuzeigende Teams bzw. Orte im Viewer
			if(this.dependingOn.equals("Teams")){
				this.model.getViewer().setTeamRoute(idList, this.locationsOnly);
				
			}else{
				this.model.getViewer().setLocationRoute(idList, this.locationsOnly);
				
			}
		}
		
	}
	
}
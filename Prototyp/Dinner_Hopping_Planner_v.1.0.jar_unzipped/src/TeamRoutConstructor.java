//Collections
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;

/**
 * Berechnung von Routenplanungen durch den Routenplanungsalgorithmus.
 * @author Björn Buchwald
 *
 */
class TeamRoutConstructor{

	/**
	 * Liste aller Veranstaltungsorte.
	 */
	private ArrayList<GeoAddress> geoAddressList;
	/**
	 * Liste aller Teams.
	 */
	private ArrayList<Team> teamList;
	/**
	 * Array aller Distanzen zwischen Orten.
	 */
	private int[][] distances;
	/**
	 * Liste von IDs der Nachspeiseteams.
	 */
	private List<Integer> dessertTeamList;
	/**
	 * Liste von IDs der Vorspeiseteams.
	 */
	private List<Integer> starterTeamList;
	/**
	 * Liste von IDs der Hauptspeiseteams.
	 */
	private List<Integer> mainTeamList;
	/**
	 * Liste aller berechneten Routenplanungen.
	 */
	private ArrayList<ArrayList<Team>> routeList;
	
	/**
	 * Wurden Routenplanungen gefunden.
	 */
	private boolean routeFound;
	/**
	 * Anzahl gefundener Routenplanungen (ohne Duplikate) pro aktueller Anordung der Hauptspeiseteams (Hilfsvariable),
	 * kann maximal den Wert von solutions annhemen.
	 */
	private int routeCounterPerMainCourse;
	
	/**
	 * Anzahl gefundener Routenplanungen ohne Duplikate.
	 */
	private int routeCounter;

	/**
	 * Maximale Anzahl von Lösungen pro Anordung von Hauptspeiseteams.
	 */
	private int solutions;
	/**
	 * Anzahl von Durchläufen pro Teilschritt in setTeams().
	 */
	private int recursionDepth;
	/**
	 * Gibt an, wie oft die Anordnung der Hauptspeiseteams geändert werden soll (jeweils zufällige Permutation).
	 */
	private int changeMainCoursesForRecursion;
	
	/**
	 * Initialisieren der Attribute und Algorithmen-Parameter.
	 * @param distances Berechnete Entferungen zwischen den Veranstaltungsorten
	 * @param geoAddressList Liste der Veranstaltungsorte
	 * @param teamList Liste der Teams
	 */
	public TeamRoutConstructor(int[][] distances, ArrayList<GeoAddress> geoAddressList, ArrayList<Team> teamList){
	
		this.distances = new int[geoAddressList.size()][geoAddressList.size()];
		this.distances = distances;
		this.geoAddressList = geoAddressList;
		this.teamList = teamList;
		
		dessertTeamList = new ArrayList<Integer>();
		starterTeamList = new ArrayList<Integer>();
		mainTeamList = new ArrayList<Integer>();
		this.routeCounterPerMainCourse = 0;
		this.routeCounter = 0;
		this.routeFound = false;
		routeList = new ArrayList<ArrayList<Team>>();
		this.solutions = 5;
		this.recursionDepth = 10;
		this.changeMainCoursesForRecursion = 10;
		
	}
	
	/**
	 * Kopierkonstruktor
	 * @param constructor
	 */
	public TeamRoutConstructor( TeamRoutConstructor constructor ){
		
		this.dessertTeamList = new ArrayList<Integer>();
		this.starterTeamList = new ArrayList<Integer>();
		this.mainTeamList = new ArrayList<Integer>();
		this.geoAddressList = PlannerModel.copyGeoAddressList( constructor.geoAddressList );
		this.teamList = PlannerModel.copyTeamList( constructor.teamList );
		this.distances = new int[geoAddressList.size()][geoAddressList.size()];
		this.distances = PlannerModel.copy2DIntArray( constructor.distances );
		this.dessertTeamList.addAll(constructor.dessertTeamList);
		this.starterTeamList.addAll(constructor.starterTeamList);
		this.mainTeamList.addAll(constructor.mainTeamList);
		this.routeFound = constructor.routeFound;
		this.routeCounterPerMainCourse = constructor.routeCounterPerMainCourse;
		this.solutions = constructor.solutions;
		this.routeList = new ArrayList<ArrayList<Team>>();
		for(ArrayList<Team> teamListOld : constructor.routeList){
		 
			this.routeList.add( PlannerModel.copyTeamList( teamListOld ) );
			
		}
		
	}
	
	/**
	 * Setzen der Parameter des Routenplanungsalgorithmus.
	 * @param solutions Anzahl max Lösungen pro Hauptspeiseteam-Anordnung
	 * @param recursionDepth
	 * @param changeMainCoursesForRecursion
	 */
	public void setRecursionProperties(int solutions, int recursionDepth, int changeMainCoursesForRecursion){
		
		this.solutions = solutions;
		this.recursionDepth = recursionDepth;
		this.changeMainCoursesForRecursion = changeMainCoursesForRecursion;
		System.out.println("Configuration: "+solutions+", "+recursionDepth+", "+changeMainCoursesForRecursion);
		System.out.println();
	
	}
	
	/**
	 * Aufruf der einzelnen Teile (Methoden) des Routenplanungsalgorihtmus,
	 * Setzen wie oft Hauptspeiseteam-Anordnung geändert wird.
	 * Aufruf Methode: Berechnen Routen für Hauptspeiseteams, setzen der restlichen
	 * Gänge, Berechnen der restlichen Routen (rekursive Methode setTeams.
	 * 
	 */
	public void constructRoutes(){
		
		System.out.println("------------Route constructor-----------");
		System.out.println();
		
		//mark main courses in diagonal, nearest neighboor of main course => i for course proof j (column) for nearest courses
		boolean[][] mainCourses = new boolean[ (this.geoAddressList.size() ) ][(this.geoAddressList.size() )];
		ArrayList<Team> teamListTemp = new ArrayList<Team>();
		//displayArray(distances);
		setMinimalSumCourses( mainCourses );
		
		//Berechne neue Routenplanungen
		for( int i = 0; i < changeMainCoursesForRecursion; i++ ){
			this.routeCounterPerMainCourse = 0;
			//reset mainCourse array, without diagonal, cause diagonal is set by setMinimalSumCourses() method
			for( int j = 0; j < mainCourses.length; j++ )
				for(int k = 0; k < mainCourses.length; k++ )
					if( j != k ) mainCourses[j][k] = false;
			//setze Listen neu, die während des Algorithmus berechnet werden
			teamListTemp.clear();
			this.starterTeamList.clear();
			this.dessertTeamList.clear();
			this.mainTeamList.clear();
			//Kopiere Liste mit Teams
			copyTeamList( this.teamList, teamListTemp );
			//Berechne für jeden Hauptspeiseort, die zwei am nahesten zu ihm gelegenen Orte
			computeNearestNeighborMain( mainCourses, true);
			computeNearestNeighborMain( mainCourses, true); //jeweils zweiter Ort
			setCourses( mainCourses, teamListTemp ); //setze Routen der Hauptspeiseteams
			//Berechne restliche Routen
			setTeams( teamListTemp, Modus.START_TO_MAIN ); 
			
		}
		
		System.out.println("Solution: "+this.routeCounter+" Finished!");
		
	}
	/**
	 * Haupspeiseteams sollen nach Möglichkeit die Routen mit kleinstem zurückgelegten
	 * Weg erhalten. Deshalb erfolgt hier eine Vorauswahl von Haupspeiseorten.
	 * Vorgehen: Berechnung der Summe aller Distanzen eines Ortes zu allen anderen Orten.
	 * Sei n die Anzahl der Veranstaltungsorte bzw. -teams.
	 * Auswahl der n/3 Orte mit den kleinsten Summen. Diese Orte werden für eine
	 * Vorauswahl der Hauptspeiseorte (provisorisch) ausgewählt. 
	 * Annahme: Da diese Orte die kleinsten Entfernungen zu allen allen anderen 
	 * Orten besitzen, entstehen für sie die potenziell kleinsten Routen. 
	 * @param mainCourses zweidimensionales array, Orte mit kleinsten Summen 
	 * sind in der Diagonale true gesetzt. Der Index entspricht der home ID des 
	 * jeweiligen Teams.
	 */
	public void setMinimalSumCourses( boolean[][] mainCourses ){
		
		//Array zum Speichern von Distanzsummen
		int[] sumDistances = new int[this.distances.length];
		
		//Aufsummieren der Einzeldistanzen pro Veranstaltungsort
		for( int i= 0; i < sumDistances.length; i++ ){
		
			for( int j = 0; j < sumDistances.length; j++ ){
				
				sumDistances[i] += this.distances[i][j];
				
			}
		}
		
		//Anzahl auszuwählender Hauptspeiseorte = 1/3 der Gesamtanzahl von Orten
		int countMainCourse = sumDistances.length / 3;
		
		//kopieren des Summen-Arrays
		int[] minSum = Arrays.copyOf( sumDistances, sumDistances.length );
		
		//Auswählen der n/3 Orte mit den kleinsten Distanzsummen.
		for( int k = 0; k < countMainCourse; k++ ){
			
			//temp = aktuell kleinste Summe
			int temp = minSum[0];
			//min = Index von temp
			int min = 0;
			
			//Durchlauf aller Orte
			for( int l = 1; l < minSum.length; l++ ){
				
				//Prüfe ob kleinste Summe größer als aktuelle Summe
				if( temp > minSum[l] ){
					
					//aktualisiere kleinste Summe und Index
					min = l;
					temp = minSum[l];
					
				}
				
			}
			//setzen der Distanzsumme des ausgewählten Ortes auf maximal Wert.
			//=> dieser wird nicht erneut ausgewählt.
			minSum[min] = Integer.MAX_VALUE;
			
			//Länge entspricht Anzahl der Orte in beiden Dimensionen
			//ausgewählter Ort wird in der Diagonale auf true gesetzt. 
			mainCourses[min][min] = true;
		}
		
	}
	
	/**
	 * Bestimmen der Routen für die Hauptspeiseteams. Diese sollen bevorzugt kurze 
	 * Routen erhalten, deshalb wird deren Berechnung seperat durchgeführt.
	 * Vorgehen: Nach der Nearest Neighbor Heuristik werden die zwei Orte mit den 
	 * kürzesten Entferung zum jeweiligen provisorischen Hauptspeiseort ausgewählt.
	 * Die Reihenfolge in welcher die Orte abgearbeitet werden, kann zufällig oder 
	 * sequentiell nach ihrer Anordnung im mainCourse Array erfolgen.
	 * @param mainCourses zweidimensionales boolean Array, der erste Index gibt die home id
	 * der orte wieder, Orte die am nahesten an einem Hauptspeiseort liegen sind in 
	 * den Zeilen auf true gesetzt.
	 * @param random boolean, legt fest ob provisorische Hauptspeiseorte zufällig oder 
	 * in fester Reihenfolge abgearbeitet werden sollen.
	 */
	public void computeNearestNeighborMain( boolean[][] mainCourses, boolean random){
		
		//mainList = indices der Hauptspeiseorte
		List<Integer> mainList = new ArrayList<Integer>();
		
		for(int j = 0; j < mainCourses.length; j++){
			if( mainCourses[j][j] ) mainList.add(j);
		}
		
		//choose random permutation of mainCourses if random is set
		if(random) Collections.shuffle(mainList);
		
		//choose nearest neighboor, gehe alle Hauptspeiseorte durch finde für jeden nearest neighbor 
		//for(int i = 0; i < this.mainCourses.length; i++){
		for(int main : mainList){
			
			//temp = aktuell kleinste Distanz vom Hauptspeiseort zu einem anderen noch nicht besetzten Ort
			int temp;
			//min= index der kleinsten Distanz
			int min = 0;
			//suche noch nicht belegten Ort 
			for( ; min < this.distances.length; min++ ){
				//wenn in spalte min kein ort gesetzt ist => dieser Ort ist noch frei
				if( locationIsSet( mainCourses, min ) == false ) 
					break;
			
			}
			
			temp = this.distances[main][min];
			
			for( int l = min +1; l < this.distances.length; l++ ){
				//wenn in Spalte l (Ort l) ein Wert belegt => dieser Ort ist bereits nearest Neighbor eines anderen Hauptspeiseortes
				if(locationIsSet(mainCourses, l) == true) 
					continue; //setze Suche fort (aktueller Ort schon belegt)
				
				//aktuelle Distanz kleiner als bisher kleinste Distanz (temp)
				if(temp > this.distances[main][l]){
					min = l;
					temp = this.distances[main][l];
				}
				
			}
			//setze nearest Neighbor (min) in der Zeile des Hauptspeiseortes (main)
			mainCourses[main][min] = true;
		}
		//displayArray( mainCourses );
	}
	
	/**
	 * Hier werden die eigentlichen Vor-, Haupt- und Nachspeiseorte bzw. -teams 
	 * fetsgelegt und die Abfolge ihrer entgültigen Route nach Vor-, Haupt- und 
	 * Nachspeise festgelegt.
	 * Vorgehen: Die in computeNearestNeighborMain festgelegten Tripel von Orten, 
	 * werden so zu einer Route verbunden, dass die zurückgelegte Distanz minimal ist. 
	 * @param mainCourses zweidimensionales boolean Array, enthält Tripel von nah 
	 * beieinander liegenden Orten.
	 * Für die beteiligten Teams werden entsprechende Parameter, wie beispielsweise
	 * welches Team hat welches andere Team bereits gesehen, gesetzt.
	 * @param teamList ArrayList von Team Obekten
	 */
	public void setCourses( boolean[][] mainCourses, ArrayList<Team> teamList ){
		
		List<Integer> actuellCourses = new ArrayList<Integer>();
		int min, distance0, distance1, distance2, mainCourseId;
		
		for(int i = 0; i < mainCourses.length; i++){
			
			//wähle ausschließlich Hauptspeiseorte (-teams)
			if( mainCourses[i][i] == false) continue;
			actuellCourses.clear();
			
			//wähle die beiden Orte, die im vorherigen Schritt als naheste Orte markiert wurden
			for(int j = 0; j < mainCourses.length; j++){
				if(mainCourses[i][j] == false) continue;
				actuellCourses.add(j);
			}
			//=> 3 Orte
			//mögliche Distanzen zwischen den 3 Orten 
			distance0 = this.distances[actuellCourses.get(0)][actuellCourses.get(1)] + this.distances[actuellCourses.get(0)][actuellCourses.get(2)];
			distance1 = this.distances[actuellCourses.get(1)][actuellCourses.get(0)] + this.distances[actuellCourses.get(1)][actuellCourses.get(2)];
			distance2 = this.distances[actuellCourses.get(2)][actuellCourses.get(0)] + this.distances[actuellCourses.get(2)][actuellCourses.get(1)];
			
			//setze die kürzeste Route aus den oben berechneten Distanzen zusammen
			//min = Index des Hauptspeiseteams in der actuellCourse Liste
			if(distance0 < distance1 && distance0 < distance2) min = 0;
			else if (distance1 < distance0 && distance1 < distance2) min = 1;
			else if (distance2 < distance1 && distance2 < distance0) min = 2;
			else if (distance0 == distance1 && distance0 < distance2) min = 1;
			else if (distance0 == distance1 && distance0 > distance2) min = 2;
			else if (distance1 == distance2 && distance1 < distance0) min = 1;
			else if (distance1 == distance2 && distance1 > distance0) min = 0;
			else if (distance2 == distance0 && distance2 < distance1) min = 2;
			else if (distance2 == distance0 && distance2 > distance1) min = 1;
			else min = 1;
			
			//set mainCourse team and mainTeamList
			mainCourseId = actuellCourses.get(min);
			//setze Parameter im Hauptspeiseateam
			teamList.get( actuellCourses.get(min) ).course = Course.MAIN_COURSE;
			teamList.get( actuellCourses.get(min) ).mainCourse = actuellCourses.get(min);
			//Füge Hauptspeiseteam zur Hauptspeiseteamliste hinzu
			this.mainTeamList.add( actuellCourses.get(min) );
			
			//lösche Hauptspeiseort aus der Liste
			actuellCourses.remove(min);
			//setze die beiden nahesten Orte zufällig auf Vor- und Nachspeiseort
			Collections.shuffle(actuellCourses);
			
			//set dessert, starter teams and teamLists
			teamList.get(actuellCourses.get(0)).course = Course.DESSERT;
			teamList.get(actuellCourses.get(1)).course = Course.STARTER;
			teamList.get(actuellCourses.get(0)).dessert = actuellCourses.get(0);
			teamList.get(actuellCourses.get(1)).starter = actuellCourses.get(1);
			this.dessertTeamList.add( actuellCourses.get(0) );
			this.starterTeamList.add( actuellCourses.get(1) );
			//set dessert and starter for mainCourse team
			teamList.get(mainCourseId).dessert = actuellCourses.get(0);
			teamList.get(mainCourseId).starter = actuellCourses.get(1);
			//main course team was seen by dessert and starter team
			teamList.get(actuellCourses.get(0)).seen[mainCourseId] = true;
			teamList.get(actuellCourses.get(1)).seen[mainCourseId] = true;
			//main course teams were seen by themselves
			teamList.get(actuellCourses.get(0)).seen[actuellCourses.get(0)] = true;
			teamList.get(actuellCourses.get(1)).seen[actuellCourses.get(1)] = true;
			//dessert and starter teams are seen by mainCourse team
			teamList.get(mainCourseId).seen[actuellCourses.get(0)] = true;
			teamList.get(mainCourseId).seen[actuellCourses.get(1)] = true;
			teamList.get(mainCourseId).seen[mainCourseId] = true;
			
		}
		
	}
	
	/**
	 * Bestimmen der Routen für Vor- und Nachspeiseteams, Zuerst Route für Vorspeiseteams:
	 * von Vorspeieseort zum Hauptspeiseort START_TO_MAIN, vom Hauptspeiseort zum Nachspeiseort; danach für 
	 * Nachspeiseteams: von Nachspeiseort zu deren Hauptspeiseort, vom Hauptspeiseort zum
	 * Vorspeiseort; jeweils rekursiver Aufruf dieser Mehtode für jeden Teilschritt.
	 * @param teamListTemp
	 * @param mode aktueller Modus
	 */
	public void setTeams( ArrayList<Team> teamListTemp, Modus mode ){
		//where team has to go. List has to copy cause we will not maipulate attribute lists
		List<Integer> courseTeamListTemp = new ArrayList<Integer>(); //Liste mit Folgeorten
		//from where team goes to next Location
		List<Integer> courseTeamList = new ArrayList<Integer>(); //Liste mit Anfangsorten
		//copy of teamListTemp, List from last iteration should not be manipulated
		ArrayList<Team> actuellTeamList = new ArrayList<Team>(); //Liste mit verbleibenden Folgeorten
																//bereits bearbeitete Folgeorte werden aus dieser Liste gelöscht
	
		//mainTeamListTemp.addAll( this.mainTeamList );
		int temp, min, minTempList, permCount, homeCourse;
		boolean setMainTeam, permFail, courseWasSeen, routeFinished;
		/*
		for(int mainTeam : courseTeamList) System.out.println( Letter.getLetterByNumber(mainTeam)+"|");
		displayRoute(teamListTemp);
		for( Team team : teamListTemp){
				displayBooleanArray(team.seen);
		System.out.println();
		}
		System.out.println();
		*/
		permCount = 0; 
		permFail = true; 
		homeCourse = -1;
		routeFinished = false;
		
		//Anzahl von Neuberechnungen pro Teilschritt
		while( permCount < recursionDepth ){
			//System.out.println("Rekursion: "+permCount);
			permCount++;
			permFail = false;
			//reset Lists
			courseTeamListTemp.clear();
			actuellTeamList.clear();
			courseTeamList.clear();
			//set Lists
			copyTeamList( teamListTemp, actuellTeamList );
			//kopiere beteiligte Listen, um sie bei rekursivem Aufruf nicht zu überschreiben
			switch( mode ){
				case START_TO_MAIN : 	copyIntegerTeamList( this.mainTeamList, courseTeamListTemp );
										copyIntegerTeamList( this.starterTeamList, courseTeamList); break;
										
				case START_TO_DESSERT : copyIntegerTeamList( this.dessertTeamList, courseTeamListTemp );
										copyIntegerTeamList( this.mainTeamList, courseTeamList ); break;
				
				case DESSERT_TO_MAIN : 	copyIntegerTeamList( this.mainTeamList, courseTeamListTemp );
										copyIntegerTeamList( this.dessertTeamList, courseTeamList ); break;
				
				case DESSERT_TO_STARTER :	copyIntegerTeamList( this.starterTeamList, courseTeamListTemp );
											copyIntegerTeamList( this.mainTeamList, courseTeamList ); break;
			
			}
			//shuffle (random insertion)
			//zufälliger Durchlauf der Teams
			Collections.shuffle( courseTeamList );
			
			for( int course : courseTeamList ){
				
				if( mode == Modus.START_TO_DESSERT ) {
					for( int starter : this.starterTeamList ){
						if( actuellTeamList.get( starter ).mainCourse == course ){ 
							homeCourse = starter;
							break;
						}
					}
				}else if( mode == Modus.DESSERT_TO_STARTER ){
					for( int dessert : this.dessertTeamList ){
						if( actuellTeamList.get( dessert ).mainCourse == course ){
							homeCourse = dessert;
							break;
						}
					}
				}else homeCourse = course;
				//set start minimum value and indices
				temp = 1000000;
				min = -1;
				minTempList = -1;
				//finde Ort (Haupt-, Nach- oder Vorspeiseort) mit minimaler Distanz zum aktuellen Ort (Vor- oder Nachspeiseort)
				for( int k = 0; k < courseTeamListTemp.size(); k++ ){
					//if actuell starter team had seen main Team then this main team can not be successor, so search for next main team
					//if( this.teamList.get( starter ).seen[mainTeamListTemp.get(k)] ) continue; 
					//System.out.println(k);
					setMainTeam = true;
					for( int n = 0; n < actuellTeamList.size(); n++  ){
						
						//if( this.geoAddressList.get( mainTeamListTemp.get(k) ).teams[n] == true && this.teamList.get( starter ).seen[n] == true){
						courseWasSeen = false;
						switch( mode ){
							//if a team is at this mainCourse location and the starter team has seen him before then search for other main location
							case START_TO_MAIN : if( actuellTeamList.get(n).mainCourse == courseTeamListTemp.get(k) && actuellTeamList.get( course ).seen[n] == true && n != homeCourse ) courseWasSeen = true; break;
							case START_TO_DESSERT : if( actuellTeamList.get(n).dessert == courseTeamListTemp.get(k) && actuellTeamList.get( homeCourse ).seen[n] == true && n != homeCourse ) courseWasSeen = true; break;
							case DESSERT_TO_MAIN : 	if( actuellTeamList.get(n).mainCourse == courseTeamListTemp.get(k) && actuellTeamList.get( course ).seen[n] == true && n != homeCourse) courseWasSeen = true; break;
							case DESSERT_TO_STARTER : 	if( actuellTeamList.get(n).starter == courseTeamListTemp.get(k) && actuellTeamList.get( homeCourse ).seen[n] == true && n != homeCourse ) courseWasSeen = true; break;
						}
						//schaue alle Teams durch, welche haben am dessert ort ihr dessert, 
						if( courseWasSeen ){	
							//System.out.println("nix: "+ Letter.getLetterByNumber(courseTeamListTemp.get(k)));
							setMainTeam = false;
							break;
						}
						
					}
					if( !setMainTeam ) continue;
					
					if( temp > this.distances[course][courseTeamListTemp.get(k)] ){
						min = courseTeamListTemp.get(k);
						minTempList = k;
						temp = this.distances[course][courseTeamListTemp.get(k)];
					}
				}
				//kein Minimum => keine passende Route => backtracking
				if( min == -1 ){
					//System.out.println("Permutation failed!");
					permFail = true;
					break;
				}
				//System.out.println(Letter.getLetterByNumber(actuellTeamList.get(homeCourse).home)+" "+Letter.getLetterByNumber(actuellTeamList.get(course).home)+" "+Letter.getLetterByNumber(actuellTeamList.get(min).home));
				//setze Folgeort des Teams von welchem Berechnung ausgeht auf den minimalen Folgeort
				switch( mode ){
											//setze Hauptspeiseort des Vorspeiseteams auf den Hauptspeiseort mit minimaler Distanz
					case START_TO_MAIN : 	actuellTeamList.get( course ).mainCourse = min; break;
					case START_TO_DESSERT : 	actuellTeamList.get( homeCourse ).dessert = min; break;
					case DESSERT_TO_MAIN : 	actuellTeamList.get( course ).mainCourse = min; break;
					case DESSERT_TO_STARTER :	actuellTeamList.get( homeCourse ).starter = min; break;
				}
				
				for(int j = 0; j < actuellTeamList.size(); j++){
					switch( mode ){
												//Modus Vorspeiseteam zu Hauptspeiseteam
						case START_TO_MAIN : 	if( actuellTeamList.get(j).mainCourse == min && actuellTeamList.get(j).seen[min]){
													//wenn Team j als Hauptspeiseteam das minimale Team hat und Team j das minimale Team gesehen hat
													actuellTeamList.get( j ).seen[course] = true;	//dann hat Team j jetzt auch das Vorspeiseteam (course) gesehen
													actuellTeamList.get( course ).seen[j] = true;	//Vorspeiseteam hat Team j gesehen
												} break;
						
						case START_TO_DESSERT : 	if( actuellTeamList.get(j).dessert == min && actuellTeamList.get(j).seen[min] ){
														actuellTeamList.get( j ).seen[homeCourse] = true;
														actuellTeamList.get( homeCourse ).seen[j] = true;								
													} break;
														
						case DESSERT_TO_MAIN : 	if( actuellTeamList.get(j).mainCourse == min && actuellTeamList.get(j).seen[min]){
													actuellTeamList.get( j ).seen[course] = true;
													actuellTeamList.get( course ).seen[j] = true;
												} break; 
						
						case DESSERT_TO_STARTER :	if( actuellTeamList.get(j).starter == min && actuellTeamList.get(j).seen[min]){
														actuellTeamList.get( j ).seen[homeCourse] = true;
														actuellTeamList.get( homeCourse ).seen[j] = true;
													} break;
					}
				}
				//Lösche Kurs mit minimaler Entfernung aus Liste verbleibender Folgeorte
				courseTeamListTemp.remove( minTempList );
			}
			if( permFail ) continue;
			//rekursiver Aufruf jeweils im Folgemodus
			switch( mode ){
				case START_TO_MAIN : setTeams( actuellTeamList, Modus.START_TO_DESSERT ); break;
				case START_TO_DESSERT : setTeams( actuellTeamList, Modus.DESSERT_TO_MAIN ); break;
				case DESSERT_TO_MAIN : setTeams( actuellTeamList, Modus.DESSERT_TO_STARTER ); break;
				case DESSERT_TO_STARTER : 	
											if( isDifferentRoute( actuellTeamList ) ){ //Prüfe ob berechnete Lösung neu ist
												ArrayList<Team> copiedList = new ArrayList<Team>();
												copyTeamList( actuellTeamList, copiedList ); //actuellTeamList enthält die Routen aller Teams
												this.routeList.add( copiedList ); //füge berechnete Routenplanung zur Liste der Routenplanungen hinzu
												this.routeCounterPerMainCourse++;
												this.routeCounter++;
												if(this.routeCounter % 10 == 0) 
													System.out.println("Solution: "+this.routeCounter);
											}
											this.routeFound = true;
											routeFinished = true;
											break;
			}
			/*
			displayRoute( actuellTeamList );
			System.out.println();System.out.println();
			//break if there is a solution for actuell permutation
			//if( routeFinished ) break;
			//if( routeFinished ) break;
			if( this.routeFound ) {
				System.out.println( "------------------------Route has constucted successfull!---------------------------" );
				return;
			}*/
			//Prüfe, ob Anzahl benötigter Lösungen erreicht
			if( this.routeCounterPerMainCourse == this.solutions ) return;
			//Anzahl Lösungen nicht erreicht und alle Rekursionsstufen durchlaufen => suche beenden
			if( routeFinished ) break;
		}
		//wenn keine Route gefunden, kehre ohne Ergebnis zurück
		if( permFail == true ){
			//System.out.println("Fatal Error! Can't construct Routs!");
			//System.exit(1);
			return;
		}
		
		
	}
	
	/**
	 * Rekursive Funktion. Verbesserte Version. (noch nicht vollständig)
	 * Varianten, die berücksichtigt werden:
	 * -	Zwei Nachspeiseteams starten von demselben Vorspeiseort
	 * -	Zwei Vorspeiseteams enden in demselben Nachspeiseort
	 * -	Zwei Nachspeiseteams gehen zum gleichen Hauptspeiseort
	 * -	Zwei Vorspeiseteams gehen zum gleichen Hauptspeiseort
	 * @param teamListTemp
	 * @param mode
	 */
	/*
	public void setTeams( ArrayList<Team> teamListTemp, Modus mode ){
		//where team has to go. List has to copy cause we will not maipulate attribute lists
		List<Integer> courseTeamListTemp = new ArrayList<Integer>();
		//from where team goes to next Location
		List<Integer> courseTeamList = new ArrayList<Integer>();
		//copy of teamListTemp, List from last iteration should not be manipulated
		ArrayList<Team> actuellTeamList = new ArrayList<Team>();
		
		boolean[] doubleSetArray = new boolean[teamListTemp.size()];
		//Array, welches anzeigt von wo ein Team auf dem bisherigen Pfad gekommen ist. z.b. wird von einem Hauptspeiseort die vorspeiseorte bestimmt welche zu diesem Hauptspeiseort gehen.
		int[] homeCourseArray = new int[2];
		//mainTeamListTemp.addAll( this.mainTeamList );
		int temp, min, minTempList, permCount, homeCourse, homeCounter;
		boolean setMainTeam, permFail, courseWasSeen, routeFinished;
		
		for(int mainTeam : courseTeamList) System.out.println( Letter.getLetterByNumber(mainTeam)+"|");
		displayRoute(teamListTemp);
		for( Team team : teamListTemp){
				displayBooleanArray(team.seen);
		System.out.println();
		}
		System.out.println();
		permCount = 0; 
		permFail = true; 
		homeCourse = -1;
		routeFinished = false;
		
		while( permCount < REKURSION_DEPTH ){
			System.out.println("Rekursion: "+permCount);
			permCount++;
			permFail = false;
			//reset Lists
			courseTeamListTemp.clear();
			actuellTeamList.clear();
			courseTeamList.clear();
			//reset array
			Arrays.fill(doubleSetArray, false);
			//set Lists
			copyTeamList( teamListTemp, actuellTeamList );
			
			switch( mode ){
				case START_TO_MAIN : 	copyIntegerTeamList( this.mainTeamList, courseTeamListTemp );
										copyIntegerTeamList( this.starterTeamList, courseTeamList); break;
										
				case START_TO_DESSERT : copyIntegerTeamList( this.dessertTeamList, courseTeamListTemp );
										copyIntegerTeamList( this.mainTeamList, courseTeamList ); break;
				
				case DESSERT_TO_MAIN : 	copyIntegerTeamList( this.mainTeamList, courseTeamListTemp );
										copyIntegerTeamList( this.dessertTeamList, courseTeamList ); break;
				
				case DESSERT_TO_STARTER :	copyIntegerTeamList( this.starterTeamList, courseTeamListTemp );
											copyIntegerTeamList( this.mainTeamList, courseTeamList ); break;
			}
			//shuffle 
			Collections.shuffle( courseTeamList );
			
			for( int course : courseTeamList ){
				homeCounter = 0;
				Arrays.fill(homeCourseArray, -1);
				if( mode == Modus.START_TO_DESSERT ) {
					for( int starter : this.starterTeamList ){
						if( actuellTeamList.get( starter ).mainCourse == course ){ 
							homeCourseArray[homeCounter] = starter;
							homeCounter++;
							if(homeCounter == 2) break;
						}
					}
				}else if( mode == Modus.DESSERT_TO_STARTER ){
					for( int dessert : this.dessertTeamList ){
						if( actuellTeamList.get( dessert ).mainCourse == course ){
							homeCourseArray[homeCounter] = dessert;
							homeCounter++;
							if(homeCounter == 2) break;
						}
					}
				}else{ 
					homeCourseArray[homeCounter] = course;
					homeCounter++;
				}
				//es muss nicht immer einen homeCourse für jeden aktuell betrachteten Kurs geben, z.B. da ein Hauptspeiseort auch von zwei Vorspeiseteams besucht werden kann.
				//Somit bleibt am Ende ein Hauptspeiseort übrig, bei dem noch kein vorspeiseteam war.
				if(homeCounter == 0) continue; 
				if(homeCounter > 2){ System.out.println("Error homeCounter"); System.exit(1); }
				//Es sind beispielsweise zwei Vorspeiseteams die zum selben Hauptspeiseteam gehen möglich. homeCourseArray ist maximal 2, da max. zwei VST am selben Hauptspeiseort.
				for( int p = 0; p < homeCounter; p++ ){
					if( homeCourseArray[p] == -1 ) break;
					homeCourse = homeCourseArray[p];
				
					//set start minimum value and indices
					temp = 1000000;
					min = -1;
					minTempList = -1;
					//get minimum
					for( int k = 0; k < courseTeamListTemp.size(); k++ ){
						//if actuell starter team had seen main Team then this main team can not be successor, so search for next main team
						System.out.println(k);
						setMainTeam = true;
						for( int n = 0; n < actuellTeamList.size(); n++  ){
					//if a team is at this mainCourse location and the starter team has seen him before then search for other main location
						
							courseWasSeen = false;
							int count = 0;
							switch( mode ){
								case START_TO_MAIN : 
									if( actuellTeamList.get(n).mainCourse == courseTeamListTemp.get(k) && actuellTeamList.get( course ).seen[n] == true && n != homeCourse ) courseWasSeen = true; 
									for(Team team : actuellTeamList) if(team.mainCourse == courseTeamListTemp.get(k) ) count++;
									if( count == 3 ) courseWasSeen = true; 
									break;
								case START_TO_DESSERT : if( actuellTeamList.get(n).dessert == courseTeamListTemp.get(k) && actuellTeamList.get( homeCourse ).seen[n] == true && n != homeCourse ) courseWasSeen = true; 
									for(Team team : actuellTeamList) if(team.dessert == courseTeamListTemp.get(k)) count++;
									if( count == 3 ) courseWasSeen = true; 
									break;
								case DESSERT_TO_MAIN : 	if( actuellTeamList.get(n).mainCourse == courseTeamListTemp.get(k) && actuellTeamList.get( course ).seen[n] == true && n != homeCourse) courseWasSeen = true; 
									for(Team team : actuellTeamList) if(team.mainCourse == courseTeamListTemp.get(k)) count++;
									if( count == 3 ) courseWasSeen = true; 
									break;
								case DESSERT_TO_STARTER : 	if( actuellTeamList.get(n).starter == courseTeamListTemp.get(k) && actuellTeamList.get( homeCourse ).seen[n] == true && n != homeCourse ) courseWasSeen = true; 
									for(Team team : actuellTeamList) if(team.starter == courseTeamListTemp.get(k)) count++;
									if( count == 3 ) courseWasSeen = true; 
									break;
							}
							//schaue alle Teams durch, welche haben am dessert ort ihr dessert, 
							if( courseWasSeen ){	
								System.out.println("nix: "+ Letter.getLetterByNumber(courseTeamListTemp.get(k)));
								setMainTeam = false;
								break;
							}
							
						}
						if( !setMainTeam ) continue;
						
						if( temp > this.distances[course][courseTeamListTemp.get(k)] ){
							min = courseTeamListTemp.get(k);
							minTempList = k;
							temp = this.distances[course][courseTeamListTemp.get(k)];
						}
					}
					if( min == -1 ){
						System.out.println("Permutation failed!");
						permFail = true;
						break;
					}
					System.out.println(Letter.getLetterByNumber(actuellTeamList.get(homeCourse).home)+" "+Letter.getLetterByNumber(actuellTeamList.get(course).home)+" "+Letter.getLetterByNumber(actuellTeamList.get(min).home));
					
					switch( mode ){
						case START_TO_MAIN : 	actuellTeamList.get( course ).mainCourse = min; break;
						case START_TO_DESSERT : 	actuellTeamList.get( homeCourse ).dessert = min; break;
						case DESSERT_TO_MAIN : 	actuellTeamList.get( course ).mainCourse = min; break;
						case DESSERT_TO_STARTER :	actuellTeamList.get( homeCourse ).starter = min; break;
					}
					//this.teamList.get( starter ).seen[min] = true;
					for(int j = 0; j < actuellTeamList.size(); j++){
						switch( mode ){
							case START_TO_MAIN : 	if( actuellTeamList.get(j).mainCourse == min && actuellTeamList.get(j).seen[min]){
														actuellTeamList.get( j ).seen[course] = true;
														actuellTeamList.get( course ).seen[j] = true;
													} break;
							
							case START_TO_DESSERT : 	if( actuellTeamList.get(j).dessert == min && actuellTeamList.get(j).seen[min] ){
															actuellTeamList.get( j ).seen[homeCourse] = true;
															actuellTeamList.get( homeCourse ).seen[j] = true;								
														} break;
															
							case DESSERT_TO_MAIN : 	if( actuellTeamList.get(j).mainCourse == min && actuellTeamList.get(j).seen[min]){
														actuellTeamList.get( j ).seen[course] = true;
														actuellTeamList.get( course ).seen[j] = true;
													} break; 
							
							
							case DESSERT_TO_STARTER :	if( actuellTeamList.get(j).starter == min && actuellTeamList.get(j).seen[min]){
															actuellTeamList.get( j ).seen[homeCourse] = true;
															actuellTeamList.get( homeCourse ).seen[j] = true;
														} break;
						}
					}
					
					if( doubleSetArray[courseTeamListTemp.get(minTempList)] == true ){
						courseTeamListTemp.remove( minTempList );
					}else{
						doubleSetArray[courseTeamListTemp.get(minTempList)] = true;
					}
					
				}
				//courseTeamListTemp.remove( minTempList );
			}
			if( permFail ) continue;
			//rekursiver Aufruf
			switch( mode ){
				case START_TO_MAIN : setTeams( actuellTeamList, Modus.START_TO_DESSERT ); break;
				case START_TO_DESSERT : setTeams( actuellTeamList, Modus.DESSERT_TO_MAIN ); break;
				case DESSERT_TO_MAIN : setTeams( actuellTeamList, Modus.DESSERT_TO_STARTER ); break;
				case DESSERT_TO_STARTER : 	if( isDifferentRoute( actuellTeamList ) ){ 
												ArrayList<Team> copiedList = new ArrayList<Team>();
												copyTeamList( actuellTeamList, copiedList );
												this.routeList.add( copiedList );
												this.routeCounter++;	
											}
											this.routeFound = true;
											routeFinished = true;
											break;
			}
			/*
			displayRoute( actuellTeamList );
			System.out.println();System.out.println();*/
			//break if there is a solution for actuell permutation
			//if( routeFinished ) break;
			//if( routeFinished ) break;
			/*if( this.routeFound ) {
				System.out.println( "------------------------Route has constucted successfull!---------------------------" );
				return;
			}*/
	/* hier kommentar löschen	
			if( this.routeCounter == this.solutions ) return;
			if( routeFinished ) break;
		}
		
		if( permFail == true ){
			System.out.println("Fatal Error! Can't construct Routs!");
			//System.exit(1);
			return;
		}
	}
	*/
	
	/**
	 * @param distanceSums Distanzsummnen der einzelnen Routenplanungen (Referenz)
	 * @param course null entspricht der Distanzsumme über die Routen aller Teams,
	 * wird ein Gang angegeben so werden nur die Distanzsummen über Teams zurückgegeben
	 * die solch einen Gang anrichten.
	 * @return int gibt den Index der minimalen Route im obigen Array an.
	 */
	public int getDistanceSumMinimalRoutes( ArrayList<Integer> distanceSums, Course course ){
		
		
		int distance = 0; 
		int distanceTemp = Integer.MAX_VALUE;
		int min = 0;
		
		for( int j = 0; j < this.routeList.size(); j++ ){ //ArrayList<Team> teamList : this.routeList
			
			distance = 0;
			//Berechne Distanzsumme für aktuelle Routenplanung
			for( Team team : this.routeList.get(j) ){
				if(course != null && team.course != course) continue;
				distance += distances[team.starter][team.mainCourse];
				distance += distances[team.mainCourse][team.dessert];
			}
			distanceSums.add(distance);
			//Berechne minimale Gesamtstrecke und merke jeweilige Routenplanung
			if( distance < distanceTemp ){
				min = j;
				distanceTemp = distance;
			}
		}
		
		return min;
		
	}
	
	/**
	 * gibt ein Array mit der Distanz der längsten Entfernung zweier Orte einer jeden 
	 * Routenplanung zurück (Referenz), der return-Wert enthält den Index der Routenplanung,
	 * welche die kürzeste dieser Distanzen enhält, bei Gleichheit wird die Planung mit der
	 * kürzesten Gesamtstrecke zurückgegeben; Außerdem wird die Distanzsumme wie in
	 * getDistanceSumMinimalRoutes als (Referenz) zurückgegeben.
	 * @param shortestLongestRoutes längste Distanz einer jeden Routenplanung
	 * @param distanceSums Distanzsummen siehe getDistanceSumMinimalRoutes
	 * @param courseSum null entspricht der Distanzsumme über die Routen aller Teams,
	 * wird ein Gang angegeben so werden nur die Distanzsummen über Teams zurückgegeben
	 * die solch einen Gang anrichten.
	 * @param courseLongestRoute null entspricht der kürzesten Distanz über die Routen aller Teams,
	 * wird ein Gang angegeben so werden nur die längsten Distanzen über Teams zurückgegeben
	 * die solch einen Gang anrichten.
	 * @return Index kürzeste längste Strecke
	 */
	public int getShortestLongestRouteWithMinimalSum( ArrayList<Integer> shortestLongestRoutes, ArrayList<Integer> distanceSums, Course courseSum, Course courseLongestRoute ){
		
		getDistanceSumMinimalRoutes( distanceSums, courseSum );
		
		int distanceTemp = Integer.MAX_VALUE;
		//int min = 0;
		int distance_1 = 0;
		int distance_2 = 0;
		int longestDistanceRoute = 0;
		int longestDistanceTemp = 0;
		
		for( int j = 0; j < this.routeList.size(); j++ ){ //ArrayList<Team> teamList : this.routeList
			
			distance_1 = 0;
			distance_2 = 0;
			longestDistanceRoute = 0;
			longestDistanceTemp = 0;
			
			for( Team team : this.routeList.get(j) ){
				
				if( courseLongestRoute != null && team.course != courseLongestRoute ) continue;
				distance_1 = distances[team.starter][team.mainCourse];
				distance_2 = distances[team.mainCourse][team.dessert];
				
				//längste Route eines Resultats
				if( distance_1 <= distance_2 ){
					longestDistanceRoute = distance_2;
				}else{
					longestDistanceRoute = distance_1;
				}
				//longestDistanceTemp = längste aktuelle Strecke
				if( longestDistanceRoute > longestDistanceTemp ){
					longestDistanceTemp = longestDistanceRoute;
				}
			}
			//füge längste Strecke der aktuellen Routenplanung hinzu
			shortestLongestRoutes.add( longestDistanceTemp );
			//Berechne kürzeste längste Strecke
			if( longestDistanceTemp < distanceTemp ){
				//min = j;
				distanceTemp = longestDistanceTemp;
			}
		}
		
		int distanceTemp2 = Integer.MAX_VALUE;
		int min2 = 0;
		//kürzeste längste Route aller Resultate mit kürzester Gesamtstrecke
		for( int j = 0; j < shortestLongestRoutes.size(); j++ ){
			
			if(shortestLongestRoutes.get(j) == distanceTemp){
				
				if( distanceSums.get(j) < distanceTemp2 ){
					distanceTemp2 = distanceSums.get(j);
					min2 = j;
				}
				
			}
			
		}
		
		return min2;
	}
	
	/**
	 * gibt alle Routenplanungen auf dem Bildschirm in Form einer
	 * Tabelle  aus.
	 */
	public void displayAllRoutes(){
		int count = 1;
		for( ArrayList<Team> teamList : this.routeList ){
			System.out.println(testRoute(teamList));
			System.out.println( "Route planning nr. "+count );
			for(Team team : teamList){
			
				System.out.println( "Route Team "+Letter.getLetterByNumber(team.home)+": "+Letter.getLetterByNumber(team.starter)+" "+Letter.getLetterByNumber(team.mainCourse)+" "+Letter.getLetterByNumber(team.dessert) );
				displayBooleanArray(team.seen);
				System.out.println();
			}
			System.out.println();
			count++;
			
		}
	}
	
	/**
	 * Gibt an, ob sich zwei berechnete Routenplanungen voneinander unterscheiden.
	 * @param teamList Teamliste dieser Routenplanung (Team Objekte enthalten die jeweilige Route des Teams)
	 * @return true Routenplanungen gleich, false sonst.
	 */
	public boolean isDifferentRoute( ArrayList<Team> teamList){
		boolean different = false;
		for( ArrayList<Team> teamRouteList : this.routeList ){
			
			for( int j = 0; j < teamList.size(); j++ ){
					
				if( ! teamList.get(j).equals( teamRouteList.get(j) ) ){
					
					different = true;
					break;
				}
			}
			if( different == false ) return false;
			different = false;
		}
		return true;
	}
	
	/**
	 * Testet ob eine Route konsistent bezüglich der Nebenbedingungen ist,
	 * wichtige Parameter werden ausgegeben.
	 * @param route Routenplanung
	 * @return Fehlermeldung
	 */
	public String testRoute(ArrayList<Team> route){
		Set<Integer> coursesSeen = new TreeSet<Integer>();
		//boolean countCourses = false, countPerCourseBool = false;
		int countPerCourse = 0;
		for( Team testTeam : route ){
			//coursesSeen.add(testTeam.id);
			for( Team team : route ){
				if( testTeam.home == team.home ) continue;
				if( testTeam.starter == team.starter ){ coursesSeen.add(team.home); countPerCourse++; }
				if( testTeam.mainCourse == team.mainCourse ){ coursesSeen.add(team.home); countPerCourse++; }
				if( testTeam.dessert == team.dessert ){ coursesSeen.add(team.home); countPerCourse++; }
				if(countPerCourse > 1 ) return "One course references more than ones to the same Location";
				countPerCourse = 0;
			}
			
			if( coursesSeen.size() != 6 ) return "Size of seen courses not valid!"; //no double elements cause set is useing
			coursesSeen.clear();
			
		}
		boolean[][] seenAll = new boolean[route.size()][route.size()];
		for(int i = 0; i < route.size(); i++ ){
			seenAll[i] = route.get(i).seen;
		}
		int courseCount = 0;
		for(int row = 0; row < route.size(); row++ ){
			for( int column = 0; column < route.size(); column++ ){
				if( seenAll[row][column] ) courseCount++;
			}
			if( courseCount != 7 ) return "Size of seen courses not valid in 'seen' array!"; //own + 6 other teams
			courseCount = 0;
		}
		for(int row = 0; row < route.size(); row++ ){
			for( int column = 0; column < route.size(); column++ ){
				if( seenAll[column][row] ) courseCount++;
			}
			if( courseCount != 7 ) return "One team has been seen by more than 6 teams";
			courseCount = 0;
		}
		return "Route is valid!!!";
	}
	
	/**
	 * Gibt an, ob eine Routenplanung gefunden werden konnte.
	 * @return true Planung vorhanden, false sonst.
	 */
	public boolean routeFound(){
	
		return this.routeFound;
	}
	
	/**
	 * Gibt die Anzahl der gefundenen Routenplanungen zurück.
	 * @return Anzahl Routenplanungen
	 */
	public int sizeRoutes(){
	
		return this.routeList.size();
	}
	
	/**
	 * Gibt alle Routenplanungen zurück.
	 * @return Routenplanungen
	 */
	public ArrayList<ArrayList<Team>> getRoutes(){
	
		return this.routeList;
	}
	
	/**
	 * Anzahl gefundener Routenplanungen ohne Duplikate.
	 * @return Anzahl
	 */
	public int getRouteCounter() {
		return routeCounter;
	}
	
	/**
	 * erzeugt eine neue Liste copList, die diegleichen Werte wie die origList enhält.
	 * @param origList
	 * @param copList
	 */
	private void copyTeamList(ArrayList<Team> origList, ArrayList<Team> copList){
		for( Team team : origList ){
			Team newTeam = new Team(team);
			copList.add(newTeam);
		}
	}
	
	/**
	 * erzeugt eine neue Liste copList, die diegleichen Werte wie die origList enhält.
	 * @param origList
	 * @param copList
	 * @param copList
	 */
	private void copyIntegerTeamList( List<Integer> origList, List<Integer> copList ){
		for( Integer origListValue : origList ){
			copList.add( origListValue );
		}
	}
	
	/**
	 * Anzeige aller Vorspeiseteams in der Konsole.
	 */
	public void displayStarterCourses(){
	
		for(Team team : this.teamList){
		
			System.out.println( "Route Team "+Letter.getLetterByNumber(team.home)+": "+Letter.getLetterByNumber(team.starter)+" "+Letter.getLetterByNumber(team.mainCourse)+" "+Letter.getLetterByNumber(team.dessert) );
		}
	
	}
	
	/**
	 * Zurückgeben der List aller Teams
	 * @return teamList
	 */
	public ArrayList<Team> getTeamList(){
	
		return this.teamList;
	}
	
	/**
	 * Gibt die Route eines jeden Teams in der Konsole aus.
	 * @param teamList
	 */
	public void displayRoute( ArrayList<Team> teamList ){
	
		for(Team team : teamList){
		
			System.out.println( "Route Team "+Letter.getLetterByNumber(team.home)+": "+Letter.getLetterByNumber(team.starter)+" "+Letter.getLetterByNumber(team.mainCourse)+" "+Letter.getLetterByNumber(team.dessert) );
		}
	
	}
	
	/**
	 * Zeigt für jedes Team, den zugewiesenen Gang an.
	 * @param teamList
	 */
	public void displayCourseFromAddresses( ArrayList<Team> teamList){
	
		for( Team team : teamList){
		
			System.out.println( Letter.getLetterByNumber( team.home )+": "+ team.course );
		
		}
	
	}
	
	//proof two-dimensional boolean array whether there is an true value on column loc
	/**
	 * prüft das übergebene Array, ob ein wahrer Wert in der spalte loc existiert.
	 * @param array
	 * @param loc
	 * @return true bei wahrem Wert, false sonst.
	 */
	public boolean locationIsSet(boolean array[][], int loc){
		for(int i = 0; i < array.length; i++){
			if(array[i][loc]) return true;
		}
		return false;
	}
	//get min value of an int array
	/**
	 * Gibt den indes des kleinsten Wertes dieses Integer-Arrays zurück.
	 * @param array
	 * @return
	 */
	public int getArrayMinValue( int[] array ){
	
		int temp = array[0];
		int min = 0;
		for(int l = 1; l < array.length; l++){
			
			if(temp > array[l]){
				min = l;
				temp = array[l];
			}
		}
		return min;
	}
	
	/**
	 * zeigt das übergebene boolean Array als Tabelle in der Konsole an.
	 * @param array
	 */
	public void displayBooleanArray(boolean[] array){
		for(int k= 0;k < array.length; k++ ){
			System.out.print(" "+Letter.getLetterByNumber(k)+"|");
		}
		System.out.println();
		for(int j= 0;j < array.length; j++ ){
			if(array[j] == true) System.out.print(" 1|");
			else System.out.print(" 0|");
		}
		System.out.println();
	}
	
	//display two-dimensional boolean arrays with labeling on x-axis
	/**
	 * zeigt das übergebene boolean Array als Tabelle in der Konsole an,
	 * mit Beschriftung auf x-Achse
	 * @param array
	 */
	public void displayArray(boolean[][] array){
		for(int k= 0;k < array.length; k++ ){
			System.out.print(" "+Letter.getLetterByNumber(k)+"|");
			/*
			number = Integer.toString(k);
			length = number.length();
			switch(length){
				case 1: System.out.print(" "+k+"|"); break;
				default: System.out.print(k+"|");
			}*/
		}
		System.out.println();
		for(int i= 0;i < array.length; i++ ){
		
			for(int j = 0; j < array.length; j++ ){
				
				if(array[i][j] == true) System.out.print(" 1|");
				else System.out.print(" 0|");
			}
			System.out.println();
		}
	
	}
	
	/**
	 * zeigt das übergebene Integer-Array als Tabelle in der Konsole an.
	 * @param array
	 */
	public void displayArray(int[][] array){
		System.out.println();
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

}
/**
 * Klasse die ein Team definiert.
 * @author Björn Buchwald
 *
 */
class Team{

	/**
	 * ID des Teams (entspricht der ID des Heimatortes dieses Teams).
	 */
	public int home;
	/**
	 * Array in dem die Namen der Teammitglieder stehen.
	 */
	public String[] members;
	/**
	 * Gibt des Gang an, den dieses Team anrichtet.
	 */
	public Course course; //Gang
	/**
	 * Gibt an welches Teams dieses Team bereits gesehen hat, Index entspricht der Team-ID
	 */
	public boolean[] seen;
	/**
	 * Gibt an bei welchem Vorspeiseteam (-ort) dieses Team beginnt.
	 */
	public int starter;	
	/**
	 * Gibt das Hauptspeiseteam (-ort) an zu welchem dieses Team geht.
	 */
	public int mainCourse;
	/**
	 * Gibt das Nachspeiseteam (-ort) an zu welchem dieses Team geht.
	 */
	public int dessert;
	
	/*
	public String name[];
	public String mail;
	public String phone;
	public String aditional;
	*/
	
	/**
	 * Erzeugt ein neues Team.
	 * @param id ID des Teams
	 * @param members Mitgliedernamen
	 * @param sizeLocations Anzahl aller Teams bzw. Orte
	 */
	public Team(int id, String[] members, int sizeLocations){
	
		this.members = new String[]{members[0], members[1]};
		this.home = id;
		this.seen = new boolean[sizeLocations];
		this.starter = -1;
		this.mainCourse = -1;
		this.dessert = -1;
		//this.name = new String[2];
	}
	//Kopierkonstruktor
	/**
	 * Kopierkonstruktor
	 * @param team Team
	 */
	public Team(Team team){
		this.home = team.home;
		this.seen = new boolean[team.seen.length];
		for( int i = 0; i < team.seen.length; i++ ){
			this.seen[i] = team.seen[i];
		}
		this.members = new String[]{ team.members[0], team.members[1] };
		this.starter = team.starter;
		this.mainCourse = team.mainCourse;
		this.dessert = team.dessert;
		this.course = team.course;
	
	}
	
	/**
	 * ermöglicht zwei Teams auf Gleichheit zu testen.
	 * @param compTeam zu vergleichendes Team
	 * @return true, wenn alle Attribute der Teams gleich sind, false sonst.
	 */
	public boolean equals( Team compTeam ){
	
		if( this.home == compTeam.home && this.course == compTeam.course && this.starter == compTeam.starter && this.mainCourse == compTeam.mainCourse && this.dessert == compTeam.dessert ) return true;
		else return false;
	
	}
	
}
//Collections
import java.util.ArrayList;
//Datei Handling
import java.io.File;
//Speichern von Java Properties
import java.util.Properties;
//osm routing service
import de.cm.osm2po.model.RoutingResultSegment;
import de.cm.osm2po.routing.DefaultRouter;
import de.cm.osm2po.routing.Graph;

/**
 * Berechnet die Distanz von jedem geokodierten Ort zu jedem anderen mit Hilfe des Osm2po Dienstes.
 * @author Björn Buchwald
 *
 */
class Osm2poDistanceCalculator implements DistanceCalculator{
	
	/**
	 * Label dieses Distanzkalkulators.
	 */
	public String label = "Osm2po Distance";

	/**
	 * Nutzen des Osm2po-Dienstes zur Distanzbestimmung.
	 */
	public Osm2poDistanceCalculator(){
		
	}
	
	/* (non-Javadoc)
	 * @see DistanceCalculator#calculateDistances(java.util.ArrayList)
	 */
	public int[][] calculateDistances( ArrayList<GeoAddress> geoAddressList ){
	
		int[][] distances = new int[geoAddressList.size()][geoAddressList.size()];
		
		System.out.println("-----------Osm2po Distance Calculation------------");
		System.out.println();
		//Erzeuge Objekt des Graph-Files
		File graphFile = new File("../lib/sn/sn_2po.gph");
		String firstPath = graphFile.getPath();
		if( !graphFile.exists() ){
			graphFile = new File("sn/sn_2po.gph");
		}
		
		//Prüfe ob Graph File gefunden wurde
		if( !graphFile.exists() ){
			System.out.println("Osm2po Graph-File not found in "+firstPath+" and "+graphFile.getPath()+"!");
			return null;
		}
		
		//lade den Graphen in den Speicher
        Graph graph = new Graph(graphFile); 
        
        //Wähle den DefaultRouter
        DefaultRouter router = new DefaultRouter(); // Dijkstra with specials
        
        // Mögliche Parameter für den DefaultRouter
        Properties params = new Properties();
        params.put("findShortestPath", false); //kürzester Pfad
        params.put("ignoreRestrictions", false); 
        params.put("ignoreOneWays", false); //keine Einbahnstraßen
        params.put("heuristicFactor", "1.0"); // 0.0 Dijkstra, 1.0 good A*
        
        int distCounter = 0;
        int distSum = ( geoAddressList.size()* (geoAddressList.size()-1) ) / 2; //zu berechnende Distanzen
        
        //Distanzen zwischen allen Orten
		for(int j = 0;j < geoAddressList.size(); j++){
		
		//test1: 100 Anfragen(mit selben Daten) in 47s, test2 : 27s, test3 : 27s
			for(int i = 0; i < geoAddressList.size() ; i++){
				if(i <= j) continue;
				distCounter++;
				//Finde am nahesten gelegendes Vertex zur Geokoordinate (Quelle und Ziel)
				int sourceId = graph.findClosestVertexId(geoAddressList.get(i).getLat(), geoAddressList.get(i).getLng());
				int targetId = graph.findClosestVertexId(geoAddressList.get(j).getLat(), geoAddressList.get(j).getLng());

				router.reset(); // Use the cached graph more than once
				
				//Suche nach einer Route
				router.traverse(graph, sourceId, targetId, Float.MAX_VALUE, params);
				
				double totalKm = 0.0;
				
				if (router.isVisited(targetId)) { // Zielvertex gefunden!
					//Erzeuge den Pfad bestehend aus IDs der einzelnen Segmente 
					int[] path = router.makePath(targetId);
					
					//Summiere Distanzen der einzelnen Segmente der Route
					for (int k = 0; k < path.length; k++) {
						
						//Segmente mit aktueller ID
						RoutingResultSegment rrs = graph.lookupSegment(path[k]);
						//Länge des aktuellen Segmentes
						double km = rrs.getKm(); 
						
						totalKm += km;
						
					}
					
				}
				//Füge Länge in symmetrische Distanzmatrix ein
				distances[j][i] = (int) ( totalKm * 1000 );
				distances[i][j] = (int) ( totalKm * 1000 );
				
				if(distCounter % 5 == 0) System.out.println(distCounter+" of "+ distSum );
			}
			
		}
		System.out.println(distCounter+" of "+ distSum+". Finished!");
		
		return distances;
	}
}
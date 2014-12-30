//Collections
import java.util.ArrayList;
import java.util.HashSet;
//Exception
import java.io.IOException;
//AWT
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
//SWING
import javax.swing.JFrame;
import javax.imageio.ImageIO;
//JXMapViewer
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
//Painter
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;

/**
 * Kartenbetrachter des Routenplanners, realisiert mit dem JXMapKit;
 * Einzeichnen von Waypoints mit Label und Routen. 
 * @author Björn Buchwald
 *
 */
public class MapViewer {
	
	/**
	 * Frame der Kartenansicht.
	 */
	private JFrame frame;
	
	/**
	 * JXMapKit Kartenbetrachtung in Java
	 */
	private JXMapKit kit;
	
	/**
	 * Zusammenfügen mehrerer Painter in einem CompoundPainter.
	 * Das Kit kann nur einen Painter darstellen.
	 */
	private CompoundPainter<WaypointPainter<JXMapViewer>> cp = null;
	
	/**
	 * Liste die alle anzuzeigenden Routen (Folge von Ids) enthält.
	 * Eine Route besteht immer aus 3 Veranstaltungsorten (Ids).
	 */
	private ArrayList<ArrayList<Integer>> routesToView = null;

	/**
	 * Übergebene Liste mit Orten bzw. Teams (deren id) von welchen die Routen angezeigt werden sollen.
	 */
	private ArrayList<Integer> idList = null;

	/**
	 * Fertige Routenplanung des RouteConstructors mit allen Teams
	 */
	private ArrayList<Team> routeTeamList = null;

	/**
	 * Geokoordinaten aller Teams
	 */
	private ArrayList<GeoAddress> geoAddressList = null;
	
	/**
	 * Kennzeichnung der Instanz
	 */
	private int instance;
	
	/**
	 * Setzen des Frames, initialisieren des MapKits und der Attribute.
	 * @param instance Instanz-ID dieses MapViewers
	 */
	public MapViewer( int instance ){
		
		System.out.println();
		System.out.println("Open JXMapViewer!");
		System.out.println();
		System.out.println("----------------JXMapViewer---------------");
		System.out.println();
		
		frame = new JFrame();
		this.kit = new JXMapKit();
		this.cp = new CompoundPainter<WaypointPainter<JXMapViewer>>();
		this.routesToView = new ArrayList<ArrayList<Integer>>();
		this.idList = new ArrayList<Integer>();
		this.routeTeamList = new ArrayList<Team>();
		this.geoAddressList = new ArrayList<GeoAddress>();
		this.instance = instance;
		
		//Setze Kartenanbieter (für andere Anbieter sind eventuell Provider Klassen selbst zu erstellen)
		kit.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
		kit.setDataProviderCreditShown(true);
		//Zentrieren der Karte
		kit.getMainMap().setCenterPosition( new GeoPosition( 51.33969550, 12.37307470 ) );
		//initiales Zoomlevel
		kit.setZoom(5);
		
		frame.getContentPane().add(kit);
		frame.setSize(512, 512);
		frame.setResizable(true);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);
		frame.setTitle("Dinner Hopping Viewer v1.0 ( Instance "+this.instance+" )");
		
		Dimension d = Toolkit.getDefaultToolkit().  getScreenSize();
		frame.setLocation( (d.width- frame.getSize().width ) / 2, (d.height- frame.getSize().height) / 2 );
		
		cp.setCacheable(false);
		
	}
	
	/**
	 * Schließen des MapViewer-Frames.
	 */
	public void dispose(){
		this.frame.dispose();
	}
	
	/**
	 * Sezten einer neuen Routenplanung, die angezeigt werden soll.
	 * @param routeTeamList neue Routenplanung
	 * @param geoAddressList neue Veranstaltungsorte
	 */
	public void init(ArrayList<Team> routeTeamList, ArrayList<GeoAddress> geoAddressList){
		
		this.routeTeamList = routeTeamList;
		this.geoAddressList = geoAddressList;
		
		/*
		kit.getMainMap().invalidate();
		kit.repaint();
		*/
	}
	
	/**
	 * Zeige die Routen der Vorspeiseteams im Viewer
	 * @param waypointOnly zeige ausschließlich Orte
	 */
	public void setStarterTeamRoutes( boolean waypointOnly ){
		
		ArrayList<Integer> starterTeams = new ArrayList<Integer>();
		for( Team team : this.routeTeamList ){
			if( team.course == Course.STARTER ) starterTeams.add( team.home );
			
		}
		if( waypointOnly ) setWaypoints( starterTeams );
		else setTeamRoute( starterTeams, waypointOnly );
	}
	
	/**
	 * Zeige die Routen der Hauptspeiseteams im Viewer.
	 * @param waypointOnly zeige ausschließlich Orte
	 */
	public void setMainTeamRoutes( boolean waypointOnly ){

		ArrayList<Integer> mainTeams = new ArrayList<Integer>();
		for( Team team : this.routeTeamList ){
			if( team.course == Course.MAIN_COURSE ) mainTeams.add( team.home );
			
		}
		if( waypointOnly ) setWaypoints( mainTeams );
		else setTeamRoute( mainTeams, waypointOnly );
	}
	
	/**
	 * Zeige die Routen der Nachspeiseteams im Viewer.
	 * @param waypointOnly zeige ausschließlich Orte
	 */
	public void setDessertTeamRoutes( boolean waypointOnly ){

		ArrayList<Integer> dessertTeams = new ArrayList<Integer>();
		for(Team team : this.routeTeamList){
			if( team.course == Course.DESSERT ) dessertTeams.add( team.home );
			
		}
		if( waypointOnly ) setWaypoints( dessertTeams );
		else setTeamRoute( dessertTeams, waypointOnly );
	}
	
	/**
	 * Zeige Routen der ausgewählten Teams.
	 * @param teamIds IDs (Indices) der Teams, die angezeigt werden sollen.
	 * @param waypointOnly zeige ausschließlich Orte
	 */
	public void setTeamRoute( ArrayList<Integer> teamIds, boolean waypointOnly ){
		
		//Liste mit Routen, die im Moment angezeigt werden.
		//eine Route besteht aus drei Veranstaltungsorten
		this.routesToView.clear();
		
		//Liste mit Team Ids, deren Routen angezeigt werden sollen.
		this.idList = teamIds;
		
		//Route des Team mit jeweiliger ID wird ausgelesen
		for( int id : teamIds ){
			
			ArrayList<Integer> actuellRoute = new ArrayList<Integer>();
			//Vorspeiseort wird ausgelesen zur aktuellen Route hinzugefügt
			actuellRoute.add( this.routeTeamList.get(id).starter );
			//Hauptspeiseort wird ausgelesen
			actuellRoute.add( this.routeTeamList.get(id).mainCourse );
			//Nachspeiseort wird ausgelesen
			actuellRoute.add( this.routeTeamList.get(id).dessert );
			//Hinzufügen der aktuellen Route zum Routen-Array
			routesToView.add(actuellRoute);
		}
		
		//zeichne aktuelle Wegpunkte, Routen und Label
		//zeige nur Orte => Overlay der Orte + Overlay der Ortsbezeichnungen setzen
		//cp = CompoundPainter fügt mehrere Painter zu einem zusammen
		if( waypointOnly )	cp.setPainters( waypointOverlay, labelOverlay );
		
		//zeige Routen => Overlay der Orte + Overlay der Ortsbezeichnungen +
		//Overlay der Orte
		else	cp.setPainters( lineOverlay, waypointOverlay, labelOverlay );
		
		//Füge Painter zum JXMapKit hinzu
		kit.getMainMap().setOverlayPainter(cp);
		
	}
	
	/**
	 * Zeige alle Routen der Teams die über die angegebenen Orte verlaufen
	 * @param locIds IDs der Orte
	 * @param waypointOnly zeige ausschließlich Orte
	 */
	public void setLocationRoute(ArrayList<Integer> locIds, boolean  waypointOnly){
		
		ArrayList<Integer> teamIds = new ArrayList<Integer>();
		//setze Routen für Teams
		for( int id : locIds ){
			
			switch( this.routeTeamList.get(id).course ){
				case STARTER:
					for( Team team : this.routeTeamList ){
						if( team.starter == id ){
							teamIds.add(team.home);
						}
					}; break;
					
				case MAIN_COURSE:
					for( Team team : this.routeTeamList ){
						if( team.mainCourse == id ){
							teamIds.add(team.home);
						}
					}; break;
					
				case DESSERT:
					for( Team team : this.routeTeamList ){
						if( team.dessert == id ){
							teamIds.add(team.home);
						}
					}; break;
					
			}
			
		}
		setTeamRoute(teamIds, waypointOnly);
		
	}
	
	/**
	 * Zeige die Routen aller Teams.
	 * @param waypointOnly zeige ausschließlich Orte
	 */
	public void setAllRoutes( boolean waypointOnly ){
		
		this.routesToView.clear();
		this.idList.clear();
		for(Team team : this.routeTeamList){
			
			ArrayList<Integer> actuellRoute = new ArrayList<Integer>();
			actuellRoute.add( team.starter ); 
			actuellRoute.add( team.mainCourse );
			actuellRoute.add( team.dessert );
			
			this.routesToView.add(actuellRoute);
			this.idList.add(team.home);
			
		}
		
		//zeichne aktuelle Wegpunkte, Routen und Label
		if(waypointOnly)	cp.setPainters( waypointOverlay, labelOverlay );
		else	cp.setPainters( lineOverlay, waypointOverlay, labelOverlay );
		
		kit.getMainMap().setOverlayPainter(cp);
	}
	
	/**
	 * Setze die übergebenen Orte im Viewer.
	 * @param intWaypointList IDs der Orte
	 */
	public void setWaypoints(ArrayList<Integer> intWaypointList){
		
		this.routesToView.clear();
		this.routesToView.add(intWaypointList);
		
		cp.setPainters(waypointOverlay, labelOverlay);
		kit.getMainMap().setOverlayPainter(cp);
	}
	
	/**
	 * Setze alle Orte im Viewer.
	 */
	public void setAllWaypoints(){
		
		ArrayList<Integer> actuellRoute = new ArrayList<Integer>();
		
		for(GeoAddress addr : this.geoAddressList){
			actuellRoute.add(addr.getId());
		}
		
		setWaypoints(actuellRoute);
		
	}

	/**
	 * Kartenoverlay zum einzeichnen der Routen.
	 */
	private Painter<JXMapViewer> lineOverlay = new Painter<JXMapViewer>(){
	
		public void paint(Graphics2D g, JXMapViewer map, int w, int h){
			g = (Graphics2D) g.create();
		    //konvertiere von viewport zu world bitmap
		    Rectangle rect = kit.getMainMap().getViewportBounds();
		    g.translate(-rect.x, -rect.y);
	
		    //zeichne Routen
		    //g.setColor(RouteColor);
		    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g.setStroke(new BasicStroke(2));
		    
		    Color routeColor = null;
		    int count = 0;
		    for(ArrayList<Integer> routeIds : routesToView ){
		    	
		    	int lastX = -1;
			    int lastY = -1;		
			    //setze Farbe der Route
			    switch( routeTeamList.get( idList.get(count) ).course ){
		    	
			    	case STARTER: routeColor = Color.blue; break;
			    	case MAIN_COURSE: routeColor = Color.green; break;
			    	case DESSERT: routeColor = Color.red; break;
	    	
			    }
	    	
			    g.setColor(routeColor);
		    	//Ids sind bereits in korrekter Reihenfolge (oben bereits geordnet) starter->main->dessert
			    for (Integer id : routeIds){
			    	
			    	GeoAddress addr = geoAddressList.get(id);
			    	
			    	GeoPosition gp = new GeoPosition(addr.getLat(), addr.getLng());
			    	//Konvertiere geo to world bitmap pixel
			        Point2D pt = kit.getMainMap().getTileFactory().geoToPixel(gp, kit.getMainMap().getZoom());
			        if (lastX != -1 && lastY != -1){
			        	g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
			        }
			        lastX = (int) pt.getX();
			        lastY = (int) pt.getY();
			        
			    }
			    count++;
		    }
		   
	
		    g.dispose();
		}
	};
	
	/**
	 * Kartenoverlay zum einzeichnen der Labels der Orte.
	 */
	private Painter<JXMapViewer> labelOverlay = new Painter<JXMapViewer>(){
		
		public void paint(Graphics2D g, JXMapViewer map, int w, int h){
			g = (Graphics2D) g.create();
			//konvertiere von viewport zu world bitmap
		    Rectangle rect = kit.getMainMap().getViewportBounds();
		    g.translate(-rect.x, -rect.y);
	
		    //zeichne Routen
		    g.setColor(Color.black);
		    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g.setStroke(new BasicStroke(2));
		    
		    for(ArrayList<Integer> routeIds : routesToView){
		    	for(int id : routeIds){
		    		
		    		GeoAddress addr = geoAddressList.get(id);
		    
			    	GeoPosition gp = new GeoPosition(addr.getLat(), addr.getLng());
			    	//konvertiere geo to world bitmap pixel
			        Point2D pt = kit.getMainMap().getTileFactory().geoToPixel(gp, kit.getMainMap().getZoom());
			        
			        //zeichne Label (Ortsbezeichnung)
		        	if( addr.getId() <= 9){
			        	
		        		//g.drawString( String.valueOf( Letter.getLetterByNumber(addr.getId()) ),(int) pt.getX()-5, (int) pt.getY()-17);
		        		g.drawString( String.valueOf( addr.getId() ),(int) pt.getX()-4, (int) pt.getY()-17);
		        	}else{
		        		
		        		//g.drawString( String.valueOf( Letter.getLetterByNumber(addr.getId()) ),(int) pt.getX()-7, (int) pt.getY()-20);
		        		g.drawString( String.valueOf( addr.getId() ),(int) pt.getX()-7, (int) pt.getY()-17);
		        	}
			        
		    	}
		        
		    }
		    
		   
	
		    g.dispose();
		}
	};
	
	
	/**
	 * Kartenoverlay zum Einzeichnen der Orte.
	 */
	private Painter<JXMapViewer> waypointOverlay = new Painter<JXMapViewer>(){
		
		public void paint(Graphics2D g, JXMapViewer map, int w, int h){
			
			BufferedImage img = null;
			HashSet<Integer> set  = new HashSet<Integer>();
			g = (Graphics2D) g.create();
			
			//konvertiere von viewport zu world bitmap
		    Rectangle rect = kit.getMainMap().getViewportBounds();
		    g.translate(-rect.x, -rect.y);
	
		    //zeichne Routen
		    g.setColor(Color.black);
		    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g.setStroke(new BasicStroke(2));
		    
		    //eliminiere Duplikate
		    for(ArrayList<Integer> routeIds : routesToView){
		    	for(int id : routeIds){
		    		set.add(id);
		    	}
		    }
		    
		    for(int id : set){
	    		try{
	    			//zeichne Map-Marker in entsprechender Farbe
					switch(routeTeamList.get(id).course){
						case STARTER: img = ImageIO.read(getClass().getResource("resources/Map-Marker-Bubble-Azure-icon.png")); break;
						case MAIN_COURSE: img = ImageIO.read(getClass().getResource("resources/Map-Marker-Bubble-Chartreuse-icon.png")); break;
						case DESSERT: img = ImageIO.read(getClass().getResource("resources/Map-Marker-Bubble-Pink-icon.png")); break;
					}
	    				
				}catch (IOException e) {
					System.out.println("couldn't read waypoint.png");
		            System.out.println(e.getMessage());
		            e.printStackTrace();
				}
	    		
	    		GeoAddress addr = geoAddressList.get(id);
			    
		    	GeoPosition gp = new GeoPosition( addr.getLat(), addr.getLng() );
		    	//konvertiere geo to world bitmap pixel
		        Point2D pt = kit.getMainMap().getTileFactory().geoToPixel( gp, kit.getMainMap().getZoom() );
	    		//zeichne Map-Marker falls image gefunden wird
	    		if(img != null) {
	                g.drawImage( img,(int) pt.getX()-img.getWidth()/2, (int) pt.getY()-img.getHeight(), null );
	            } else {
	                g.setStroke( new BasicStroke(3f) );
	                g.setColor( Color.BLACK );
	                g.drawOval( (int) pt.getX()-10, (int) pt.getY()-10, 20, 20 );
	                g.setStroke( new BasicStroke(1f) );
	                g.drawLine( (int) pt.getX()-10, (int) pt.getY()+0, (int) pt.getX()+10, (int) pt.getY()+0 );
	                g.drawLine( (int) pt.getX()+0, (int) pt.getY()-10, (int) pt.getX()+0, (int) pt.getY()+10 );
	            }
			        
		    }
	
		    g.dispose();
		}
	};
	
}

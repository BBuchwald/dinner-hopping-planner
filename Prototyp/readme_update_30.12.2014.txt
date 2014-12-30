In dieser Datei steht alles was mir auf die Schnelle beim Ausf�hren aufgefallen ist und Probleme
verursachen k�nnten. Alles weitere bitte dem Benutzer-Leitfaden entnehmen.

1. Starten.

Zu Beginn muss eine csv Datei der Adressen im Format der beigef�gten Beispieldateien (addr.csv enth�lt Adressen
aus Leipzig) vorhanden sein.
a. Es ist m�glich eine Datei ohne Geokoordinaten der einzelnen Orte anzugeben (Angabe unter Properties).
    Hier m�ssen die Attribute lat und lng fehlen. 
    Bei dieser M�glichkeit bitte die "Dinner_Hopping_Planner_v.1.0.jar" in der Konsole �ffnen. "java -jar Dinner_Hopping_Planner_v.1.0.jar"
    Klick auf "New Calculation".
    Jetzt wird der Geokodierer gestartet (nur per Konsolenausgabe + Browser).
    Er listet die f�r eine Adresse gefundenen Geokoordinaten auf und zeigt sie im Browser an. (Ein Stra�enname kann eventuell mehrfach existieren.)
    Die richtige Koordinaten muss ausgew�hlt werden.
    Dieser Vorgang muss nur einmal ausgef�hrt werden, da daraufhin eine csv Datei mit den entsprechenden
    Geokoordinaten erstellt wird. "addrWithGeocodes.csv"
b. Ist eine csv Datei mit den Spalten lat und lng (Geokoordinaten) vorhanden, so wird die Geokodierung �bersprungen.
    In diesem Fall kann die "Dinner_Hopping_Planner_v.1.0.jar" per Doppelklick ge�ffnet werden. 
    Alles Weitere funktioniert �ber die GUI.

F�r die Geokodierung gibt es den Google Service und den CloudMade Service(basierend auf OpenStreetMap)
Der Google Service funktioniert ohne Probleme.
Der Cloud Made Service funktioniert im Moment nicht. Wahrscheinlich ist der API Key abgelaufen. Gleiches
gilt f�r den CloudMade Routing Service!!
Bei Interesse einfach mal einen eigenen API Key generieren und unter Properties -> Geocoder ->
Cloud Made API Key -> Apply Key ausprobieren.

2. Routing Service (Entfernungen)
a. Um den osm2po-RoutingService f�r Nordrhein Westfalen nutzen zu k�nnen, 
m�ssen die entsprechenden Kartendaten in den Ordner "sn" heruntergeladen werden.

Daf�r bitte die Datei "Dinner_Hopping_Planner_v.1.0.jar" entpacken. 
Hier befindet sich die Datei "osm2po-core-4.5.2-signed.jar".
Den Befehl 
�java -jar osm2po-core-4.5.2-signed.jar prefix=sn http://download.geofabrik.de/europe/germany/nordrhein-westfalen-latest.osm.pbf�
ausf�hren. (ca. 478 MB) 
Den entstandenen Ordner "sn" in das Oberverzeichnis kopieren (mit dem leeren "sn" Ordner ersetzen).

b. Der Cloud Made Routing Service funktioniert aus mir unbekanntem Grund nicht mehr. (API Key abgelaufen?)
Bei Properties -> Distance Service also bitte entweder 
"linear distance" (Entfernungen entsprechen der Luftlinie) oder 
"osm2po" (Entfernungen entsprechen den Routeninformationen von OperStreetMap, was
realistischer ist) ausw�hlen!!!! 

c. Linear Distance funktioniert nat�rlich immer und nutzt die reine Luftlinie (ungenau).
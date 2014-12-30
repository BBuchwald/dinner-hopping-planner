In dieser Datei sind einige Details zum Ausführen und zu Problemen beschrieben, die beim Einrichten der Software 
hilfreich sind. Alles weitere bitte dem Benutzer-Leitfaden entnehmen.

1. Starten.

Zu Beginn muss eine csv Datei der Adressen im Format der beigefügten Beispieldateien (addr.csv enthält Adressen
aus Leipzig) vorhanden sein.
a. Es ist möglich eine Datei ohne Geokoordinaten der einzelnen Orte anzugeben (Angabe unter Properties).
    Hier müssen die Attribute lat und lng fehlen. 
    Bei dieser Möglichkeit bitte die "Dinner_Hopping_Planner_v.1.0.jar" in der Konsole öffnen. "java -jar Dinner_Hopping_Planner_v.1.0.jar"
    Klick auf "New Calculation".
    Jetzt wird der Geokodierer gestartet (nur per Konsolenausgabe + Browser).
    Er listet die für eine Adresse gefundenen Geokoordinaten auf und zeigt sie im Browser an. (Ein Straßenname kann eventuell mehrfach existieren.)
    Die richtige Koordinaten muss ausgewählt werden.
    Dieser Vorgang muss nur einmal ausgeführt werden, da daraufhin eine csv Datei mit den entsprechenden
    Geokoordinaten erstellt wird. "addrWithGeocodes.csv"
b. Ist eine csv Datei mit den Spalten lat und lng (Geokoordinaten) vorhanden, so wird die Geokodierung übersprungen.
    In diesem Fall kann die "Dinner_Hopping_Planner_v.1.0.jar" per Doppelklick geöffnet werden. 
    Alles Weitere funktioniert über die GUI.

Für die Geokodierung gibt es den Google Service und den CloudMade Service(basierend auf OpenStreetMap)
Der Google Service funktioniert ohne Probleme.
Der Cloud Made Service funktioniert im Moment nicht. Wahrscheinlich ist der API Key abgelaufen. Gleiches
gilt für den CloudMade Routing Service!!
Bei Interesse einfach mal einen eigenen API Key generieren und unter Properties -> Geocoder ->
Cloud Made API Key -> Apply Key ausprobieren.

2. Routing Service (Entfernungen)
a. Um den osm2po-RoutingService für Nordrhein Westfalen nutzen zu können, 
müssen die entsprechenden Kartendaten in den Ordner "sn" heruntergeladen werden.

Dafür bitte die Datei "Dinner_Hopping_Planner_v.1.0.jar" entpacken. 
Hier befindet sich die Datei "osm2po-core-4.5.2-signed.jar".
Den Befehl 
“java -jar osm2po-core-4.5.2-signed.jar prefix=sn http://download.geofabrik.de/europe/germany/nordrhein-westfalen-latest.osm.pbf”
ausführen. (ca. 478 MB) 
Den entstandenen Ordner "sn" in das Oberverzeichnis kopieren (mit dem leeren "sn" Ordner ersetzen).

b. Der Cloud Made Routing Service funktioniert aus mir unbekanntem Grund nicht mehr. (API Key abgelaufen?)
Bei Properties -> Distance Service also bitte entweder 
"linear distance" (Entfernungen entsprechen der Luftlinie) oder 
"osm2po" (Entfernungen entsprechen den Routeninformationen von OperStreetMap, was
realistischer ist) auswählen!!!! 

c. Linear Distance funktioniert natürlich immer und nutzt die reine Luftlinie (ungenau).

# Wireless Climate Netwrok

Im Kern des Wireless Climate Networks stehen Luftfeuchtigkeits- und Temperatursensoren gepart mit einem Bluetooth Low Engery Transmitter. Die Sensoren werden im Rahmen eines [Projektes](https://www.hs-kl.de/hochschule/aktuelles/menschen-und-projekte/sensors-and-data-acquisition-for-smart-textiles) der Hochschule Kaiserslautern in Zusammenarbeit mit dem Adidas FUTURE TEAM entwickelt.

In diesem Repository sind drei Android Projekte zu finden. Unter `wnc2_sensors` sind allgemeine Funktionalitäten zu finden, welche zum Empfangen der Sensordaten benötigt werden. Diese Bibliothek wird in die beiden Apps [WCN2 - Measurement](https://play.google.com/store/apps/details?id=de.hs_kl.wcn2) und [WCN2 - Alarm](https://play.google.com/store/apps/details?id=de.hs_kl.wcn2_alarm) eingebunden, welche unter `wcn2` und `wcn2_alarm` zu finden sind. Beide Apps sind über den Google Play Store verfügbar.

## WCN2 - Measurement

Mit [WCN2 - Measurement](https://play.google.com/store/apps/details?id=de.hs_kl.wcn2) App können mehrere Sensoren ausgewählt werden, um mit ihnen Messungen durchzuführen. Hierzu können vor der Messung verschiedene Aktionen definiert und den Sensoren Mnemonics zugeordnet werden. Während den Messungen werden die empfangenen Daten mit diesen Informationen getaggt. Das Adidas FUTURE TEAM nutzt die [WCN2 - Measurement](https://play.google.com/store/apps/details?id=de.hs_kl.wcn2) App bei der Entwicklung neuer Sportbekleidung, indem Atlethen mit den Sensoren bestückt werden und in Klimakammern verschiedene Übungen durchführen.

## WCN2 - Alarm

In der [WCN2 - Alarm](https://play.google.com/store/apps/details?id=de.hs_kl.wcn2_alarm) App können verschiedene Thresholds bezüglich der Temperatur, Luftfeuchtigkeit und Abwesenheit der Sensoren definiert werden. Sobald diese Thresholds überschritten werden, wird auf dem Smartphone ein Alarm ausgelöst. So kann zum Beispiel die Abwesenheit einer Handtasche festgestellt werden, wenn sich in ihr ein Sensor befindet, oder eine Warnung ausgelöst werden, wenn die Temperatur in einem Kinderwagen zu sehr gestiegen ist.

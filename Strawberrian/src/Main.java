public class Main {

    public static void main(String[] args) {

//        HTTPServer webBerrian = new HTTPServer("/home/Gooseberrian/ProjektArbete/Cranberrian", 8080);
//        webBerrian.run("/home/Gooseberrian/ProjektArbete/root/MikaelA", "MikaelA" );
//        webBerrian.run("/home/Gooseberrian/ProjektArbete/root/DanskeS", "DanskeS" );

//        HTTPServer webBerrian = new HTTPServer( "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\Cranberrian", 8081);
//        webBerrian.run("C:\\Users\\Mikael Andersson\\Documents\\TEMPMAP\\MikaelA", "MikaelA" );
//        webBerrian.run("C:\\Users\\Mikael Andersson\\Documents\\TEMPMAP\\DanskeS", "DanskeS" );
        HTTPServer webBerrian = new HTTPServer( "/Users/mikaelandersson/Documents/Skolarbeten/Projekt_Datorteknik/ProjektArbete/Cranberrian", 8080);
        webBerrian.run("/Users/mikaelandersson/Documents/Skolarbeten/Projekt_Datorteknik/root/MikaelA", "MikaelA" );
        webBerrian.run("/Users/mikaelandersson/Documents/Skolarbeten/Projekt_Datorteknik/root/DanskeS", "DanskeS" );
    }
}

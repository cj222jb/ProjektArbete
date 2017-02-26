public class Main {

    public static void main(String[] args) {
//	HTTPServer webServer = new HTTPServer("C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\root",
//            "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\Cranberrian");
//	HTTPServer webServer = new HTTPServer("/home/Gooseberrian/ProjektArbete/root",
// "/home/Gooseberrian/ProjektArbete/Cranberrian");

        HTTPServer webBerrian = new HTTPServer( "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\Cranberrian");
        webBerrian.run("C:\\Users\\Mikael Andersson\\Documents\\TEMPMAP\\MikaelA", "MikaelA" );
        webBerrian.run("C:\\Users\\Mikael Andersson\\Documents\\TEMPMAP\\DanskeS", "DanskeS" );

    }
}

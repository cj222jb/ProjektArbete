public class Main {

    public static void main(String[] args) {

        HTTPServer webBerrian = new HTTPServer("/home/Gooseberrian/ProjektArbete/Cranberrian");
        webBerrian.run("/home/Gooseberrian/ProjektArbete/root", "MikaelA" );
        webBerrian.run("/home/Gooseberrian/ProjektArbete/root", "DanskeS" );

//        HTTPServer webBerrian = new HTTPServer( "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\Cranberrian");
//        webBerrian.run("C:\\Users\\Mikael Andersson\\Documents\\TEMPMAP\\MikaelA", "MikaelA" );
//        webBerrian.run("C:\\Users\\Mikael Andersson\\Documents\\TEMPMAP\\DanskeS", "DanskeS" );

    }
}

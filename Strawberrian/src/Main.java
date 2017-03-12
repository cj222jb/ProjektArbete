public class Main {

    public static void main(String[] args) {
        Authentication authenticate = new Authentication();

//        HTTPServer webBerrian = new HTTPServer("/home/Gooseberrian/ProjektArbete/Cranberrian","/home/Gooseberrian/ProjektArbete/root/", 8080);


        HTTPServer webBerrian = new HTTPServer( "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete-master\\Cranberrian","C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete-master\\root\\",8081);
//        webBerrian.run("C:\\Users\\Mikael Andersson\\Documents\\TEMPMAP\\MikaelA", "MikaelA" );
//        webBerrian.run("C:\\Users\\Mikael Andersson\\Documents\\TEMPMAP\\DanskeS", "DanskeS" );

    }
}

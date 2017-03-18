public class Main {

    public static void main(String[] args) {
        DBHandler authenticate = new DBHandler();

//        HTTPServer webBerrian = new HTTPServer("/home/Gooseberrian/ProjektArbete/Cranberrian","/home/Gooseberrian/ProjektArbete/root/", 8080);
        HTTPServer webBerrian = new HTTPServer("/Users/mikaelandersson/Documents/Skolarbeten/Projekt_Datorteknik/ProjektArbete/Cranberrian",
                "/Users/mikaelandersson/Documents/Skolarbeten/Projekt_Datorteknik/ProjektArbete/root/", 8888);

    }
}

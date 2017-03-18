/**
 * Created by mikaelandersson on 2017-02-02.
 */

import com.sun.net.httpserver.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.InetSocketAddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HTTPServer {

    private  String folder_Root;
    private  String server_WebFolder;
    private  String server_RootFolder;
    private  File html_ClientContext;
    private  File htmlIndex;
    private  HttpServer http_Server;
    private  int port;
    private  File[] folder_Files;
    private  String folder_Current;
    private final int limitSize = 10000000;

    /**
     * Creates http_Server. Binds filepath to client content,
     * binds filepath to rootfolder of media content and binds http_Server to port.
     * @param server_WebFolder
     * @param server_RootFolder
     * @param server_Port
     */
    public HTTPServer(String server_WebFolder,String server_RootFolder ,int server_Port){
        this.port = server_Port;
        this.server_RootFolder = server_RootFolder;
        this.server_WebFolder = server_WebFolder;
        try {
            DBHandler db_Handler = new DBHandler();
            ArrayList<String> userList = db_Handler.getAll();    //Iterates over all users from db and stores file_Name in list
            http_Server = HttpServer.create(new InetSocketAddress(server_Port), 0);
            System.out.println("[SERVER UP, RUNNING ON PORT: "+server_Port+"]");
            /*Makes CSS files and JavaScript files attainable from client*/
            http_Server.createContext("/SPAjs", new StaticFileHandler("/SPABerrian.js"));
            http_Server.createContext("/indexjs", new StaticFileHandler("/indexBerrian.js"));
            http_Server.createContext("/css", new StaticFileHandler("/fancyBerrian.css"));
            /*Makes all icons and logos avaiable for client*/
            pushPictures();
            http_Server.setExecutor(null); // creates a default executor
            http_Server.start();
            http_Server.createContext("/", new HTMLIndexHandler());
            for(int i = 0; i < userList.size(); i++){
                run(this.server_RootFolder+db_Handler.getUserInformation(userList.get(i))[2], userList.get(i) );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates all context for a User
     * @param rootFolder
     * @param user
     * @throws IOException
     */
    public void run(String rootFolder, String user) throws IOException {
        DBHandler authenticate = new DBHandler();
        ArrayList<String> userNames = authenticate.getAll();
        this.folder_Root = rootFolder;
        System.out.println("[USER CONNECTED, ROOT FOLDER: "+rootFolder+"]");
        html_ClientContext = new File (server_WebFolder +"/Dir.html");
        htmlIndex = new File(server_WebFolder +"/index.html");
        folder_Current = rootFolder;
        String userURL = "/"+user+"/";  //First part of URL

        HttpContext hc1 = http_Server.createContext(userURL, new ContextHandler(rootFolder, ""));
        hc1.setAuthenticator(new BasicAuthenticator(user) {
            @Override
            public boolean checkCredentials(String user, String pwd) {
                return user.equals(authenticate.getUserInformation(user)[0]) && pwd.equals(authenticate.getUserInformation(user)[1]);
            }
        });
        displayUsersToHTML(userNames);
        /*Creates all context for users files and folders.*/
        http_Server.createContext(userURL + "download", new GETFolderHandler(user));
        http_Server.createContext(userURL + "upload", new POSTFileHandler());
        http_Server.createContext(userURL+"addfolder", new ADDFolderHandler(user));
        iterateFolders(rootFolder,"/"+user+"/","");
    }

    /**
     * Creates context for all icons and logos for the client
     */
    private void pushPictures() {
        File[] imgArr = folder_Files = new File (server_WebFolder +"/images").listFiles();
        for (File imgFile : imgArr) {
            http_Server.createContext("/images/"+imgFile.getName(), new StaticFileHandler("/images/"+imgFile.getName()));
        }
    }
    private void displayUsersToHTML(ArrayList<String> userNames) throws IOException {
        Document doc = Jsoup.parse(htmlIndex,"UTF-8","");
        Element userContent = doc.getElementById("userDiv");
        userContent.empty();
        for (String userName : userNames) {
            Element li = doc.createElement("li");
            Element aTag = doc.createElement("a");
            li.attr("class","userLi");
            aTag.append(userName);
            li.appendChild(aTag);
            userContent.appendChild(li);
        }
        PrintWriter out = new PrintWriter(new FileWriter(htmlIndex));
        out.print(doc);
        out.close();
    }

    /**
     * Iterates over a users folders, subfolders and files. Creates context for all files and folders
     * depending of type.
     * @param rootFolder
     * @param folderURL
     * @param nextURL
     */
    private void iterateFolders(String rootFolder,String folderURL, String nextURL){
        /*Creates an array of files and folder from current directory*/
        System.out.println(rootFolder+"/"+nextURL);
        File[] file_Array = new File(rootFolder+"/"+nextURL).listFiles();
        String folder_Name;
        /*Iterates all files and creates handlers depending of type of file*/
        for (File file : file_Array) {
            /*If file is a folder*/
            if (file.isDirectory()) {
                folder_Name = nextURL+file.getName()+"/";
                /*Creates context for folders*/
                http_Server.createContext(folderURL+folder_Name, new ContextHandler(rootFolder,folder_Name));
                http_Server.createContext(folderURL+folder_Name + "download", new GETFolderHandler(file.getName()));
                http_Server.createContext(folderURL+ folder_Name + "upload", new POSTFileHandler());
                http_Server.createContext(folderURL+folder_Name+"deletefolder", new DELETEFolderHandler(file.getName()));
                http_Server.createContext(folderURL+folder_Name+"addfolder", new ADDFolderHandler(file.getName()));
                iterateFolders(rootFolder,folderURL,folder_Name); //Recursive call to iterate subfolder.
            }
            else {
                /*If file is a file*/
                if(!file.getName().equals(".DS_Store")){ //Type of file created in MacOS in all directories, not wanted on the webserver
                    /*Creates context for files*/
                    http_Server.createContext(folderURL+nextURL+file.getName(), new GETFileHandler(file.getName(), rootFolder+"/"+nextURL));
                    http_Server.createContext(folderURL+nextURL+file.getName()+"/deletefile", new DELETEFileHandler(file.getName()));
                }
            }
        }
    }

    /**
     * Reads html file and appends files and folders links to the body
     * @param file_Array
     * @throws IOException
     */
    private void addFileDirToHTML(File[] file_Array) throws IOException {
        /*Reads html document, get containers for files and folders*/
        Document jsoup_Document = Jsoup.parse(html_ClientContext,"UTF-8","");
        Element folder_Content = jsoup_Document.getElementById("folders");
        Element file_Content = jsoup_Document.getElementById("files");
        folder_Content.empty();
        file_Content.empty();
        for (File file_Temp : file_Array) {
            if (file_Temp.isDirectory()) {
                /*If folder, create and append content to folder container*/
                Element html_ListTag = jsoup_Document.createElement("li");
                Element html_aTag = jsoup_Document.createElement("a");
                html_ListTag.attr("class","folder");
                html_aTag.append(file_Temp.getName());
                html_ListTag.appendChild(html_aTag);
                folder_Content.appendChild(html_ListTag);
            }
        }
        for (File file_temp : file_Array) {
            if (!file_temp.isDirectory()) {
                if(!file_temp.getName().equals(".DS_Store")) {
                /*If file, create and append content to file container*/
                    Element html_ListTag = jsoup_Document.createElement("li");
                    Element html_aTag = jsoup_Document.createElement("a");
                    Element file_SizeTag = jsoup_Document.createElement("div");
                    Element file_Delete = jsoup_Document.createElement("div");
                    html_aTag.append(file_temp.getName());
                    file_SizeTag.append(getfileSize(file_temp.length()));
                    file_SizeTag.attr("class", "fileSize");
                    file_Delete.attr("class", "deleteFile");
                    html_ListTag.appendChild(file_SizeTag);
                    html_ListTag.appendChild(html_aTag);
                    html_ListTag.appendChild(file_Delete);
                    file_Content.appendChild(html_ListTag);
                }
            }
        }
        PrintWriter out = new PrintWriter(new FileWriter(html_ClientContext));
        out.print(jsoup_Document);
        out.close();
    }

    /**
     * Returns the size of the file in byte format
     * @param file_Length
     * @return
     */
    private String getfileSize(long file_Length) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        float temp_Float = file_Length;
        if(!(temp_Float>1024)){
            return decimalFormat.format(temp_Float)+"B";
        }
        temp_Float = temp_Float/1024;
        if(!(temp_Float>1024)){
            return decimalFormat.format(temp_Float)+"KB";
        }
        temp_Float = temp_Float/1024;
        if(!(temp_Float>1024)){
            return decimalFormat.format(temp_Float)+"MB";
        }
        temp_Float = temp_Float/1024;
        if(!(temp_Float>1024)){
            return decimalFormat.format(temp_Float)+"GB";
        }
        return null;
    }

    /**
     * Compresses files in a directory to a zip file
     * @param file_Path
     * @throws IOException
     */
    private void zipFolder(String file_Path) throws IOException {
        File[] file_Array = new File(file_Path).listFiles();
        File file_Zip = new File(file_Path+"/folder.zip");
        ZipOutputStream zip_Output = new ZipOutputStream(new FileOutputStream(file_Zip));
        FileInputStream zip_Input;
        byte[] byte_Array = new byte[1024]; //Creates a buffer
        for (File file : file_Array) {
            if (!file.isDirectory()) {
                /*If file write to zip_Output*/
                ZipEntry e = new ZipEntry(file.getName());
                zip_Output.putNextEntry(e);
                zip_Input = new FileInputStream(file_Path+"/"+file.getName());
                int len;
                while ((len = zip_Input.read(byte_Array)) > 0) {
                    zip_Output.write(byte_Array, 0, len);
                }
                zip_Input.close();
            }
        }
        zip_Output.closeEntry();
        zip_Output.close();
    }

    /**
     * Deletes folder
     * @param folder_Name
     * @return
     */
    private boolean deleteDir(File folder_Name) {
        if (folder_Name.isDirectory()) {
            /*If folder read all files in folder to array*/
            String[] folder_Files = folder_Name.list();
            for (int i=0; i<folder_Files.length; i++) {
                boolean success = deleteDir(new File(folder_Name, folder_Files[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return folder_Name.delete();
    }

    /**
     * If any error has occured, http_Server sends error responce to client
     * @param http_Exchange
     * @param message_Responce
     * @param error_Code
     */
    private void errorHandler(HttpExchange http_Exchange, String message_Responce, int error_Code){
        try {
            http_Exchange.sendResponseHeaders(error_Code, message_Responce.length());
            OutputStream output = http_Exchange.getResponseBody();
            output.write(message_Responce.getBytes());
            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Returns html context to client
     * @param http_Exchange
     * @param file
     */
    private void htmlHandler(HttpExchange http_Exchange, File file){
        try {
            /*Starts responce, first header afterwards output stream from html file*/
            http_Exchange.sendResponseHeaders(200, 0);
            OutputStream output_Stream = http_Exchange.getResponseBody();
            FileInputStream file_InputStream = new FileInputStream(file);
            final byte[] byte_Array = new byte[0x10000];
            int temp_Count = 0;
            while ((temp_Count = file_InputStream.read(byte_Array)) >= 0) {
                output_Stream.write(byte_Array, 0, temp_Count);
            }
            output_Stream.flush();
            output_Stream.close();
            file_InputStream.close();
        } catch (IOException e) {
            String response = "Error 500: Internal Server Error.";
            errorHandler(http_Exchange, response, 500);
        }
    }

    /**
     * Searches string for key, returns value.
     * @param var
     * @param data
     * @return
     */
    private String searchString(String var, String data) {
        int startIndex = data.indexOf(var);
        int endIndex = data.indexOf("\r\n", startIndex);

        //Check if variable is found:
        if (startIndex == -1)
            return null;

        //Check if variable is on last line:
        if (endIndex == -1)
            endIndex = data.length();

        return data.substring(startIndex + var.length(), endIndex);
    }
    /**
     * Returns URL Path from URI of request
     * @param referer
     * @return
     */
    private String getURLFromRequest(String referer){
        try {
            URI uri = new URI(referer);
            referer = uri.getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return referer;
    }
    /**
     *Manages the context the client will see when inside a folder
     */
    private class ContextHandler implements HttpHandler {
        private final String folder_Name;
        private final String folder_Root;
        public ContextHandler(String rootFolder, String name) {
            folder_Root = rootFolder;
            folder_Name = name;
        }
        @Override
        public void handle(HttpExchange http_Exchange) throws IOException {
            folder_Current = folder_Root + "/" + folder_Name;
            folder_Files = new File (folder_Current).listFiles();
            /*Iterates over all files and folders and adds returns them to client*/
            addFileDirToHTML(folder_Files);
            htmlHandler(http_Exchange, html_ClientContext);
        }
    }
    /**
     * Manages request for index file
     */
    private class HTMLIndexHandler implements HttpHandler{
        public HTMLIndexHandler(){

        }
        @Override
        public void handle(HttpExchange http_Exchange) throws IOException {
            htmlHandler(http_Exchange, htmlIndex);
        }
    }

    /**
     * Manages GET request to files from client
     */
    private class GETFileHandler implements HttpHandler {
        private final String file_Name;
        private final String folder_Current;
        public GETFileHandler(String name, String currentFolder) {
            file_Name = name;
            folder_Current = currentFolder;
        }
        public void handle(HttpExchange http_Exchange) throws IOException {
            System.out.println("Client requested: "+ folder_Current + file_Name);
            Headers header = http_Exchange.getResponseHeaders();
            /*Writes header content*/
            header.add("Content-Disposition", "attachment; filename=\""+ file_Name +"\"");
            header.add("Content-Type", "application/force-download");
            header.add("Content-Transfer-Encoding", "binary");
            try {
                File file_Requested = new File (folder_Current + file_Name);
                /*If client requests invalid file request is denied*/
                if(!file_Requested.exists())
                    throw new Exception();
                /*Reads file and write it in the output stream to client*/
                byte [] byte_Array  = new byte [(int)file_Requested.length()];
                FileInputStream file_InputStream = new FileInputStream(file_Requested);
                BufferedInputStream buffered_InputStream = new BufferedInputStream(file_InputStream);
                buffered_InputStream.read(byte_Array, 0, byte_Array.length);
                http_Exchange.sendResponseHeaders(200, file_Requested.length());
                OutputStream output_stream = http_Exchange.getResponseBody();
                output_stream.write(byte_Array,0,byte_Array.length);

                file_InputStream.close();
                buffered_InputStream.close();
                output_stream.close();
            } catch (Exception e) {
                String response = "Error 404: File not found.";
                errorHandler(http_Exchange, response, 404);
            }
        }
    }

    /**
     *Manages GET request to folders from client
     */
    private class GETFolderHandler implements HttpHandler {
        private final String folder_name;
        public GETFolderHandler(String name) {
            folder_name = name;
        }

        public void handle(HttpExchange http_Exchange){
            System.out.println("Client requested   : "+ folder_Current);
            Headers header = http_Exchange.getResponseHeaders();
              /*Writes header content*/
            header.add("Content-Disposition", "attachment; filename=\""+ folder_name +".zip\"");
            header.add("Content-Type", "application/force-download");
            header.add("Content-Transfer-Encoding", "binary");

            try {
                /*Compresses files inside folder*/
                zipFolder(folder_Current);
                File folder = new File (folder_Current +"folder.zip");

                /*Reads compressed file and write in output stream to client*/
                byte [] byte_Array  = new byte [(int)folder.length()];
                FileInputStream file_InputStream = new FileInputStream(folder);
                BufferedInputStream buffered_InputStream = new BufferedInputStream(file_InputStream);

                buffered_InputStream.read(byte_Array, 0, byte_Array.length);
                http_Exchange.sendResponseHeaders(200, folder.length());
                OutputStream output_Stream = http_Exchange.getResponseBody();

                output_Stream.write(byte_Array,0,byte_Array.length);
                file_InputStream.close();
                buffered_InputStream.close();
                output_Stream.close();
                folder.delete();
            } catch (IOException e) {
                String response = "Error 500: Internal Server Error.";
                errorHandler(http_Exchange, response, 500);
            }
        }


    }

    /**
     * Manages request to static files from client, such as CSS/JavaScript/Icons
     */
    private class StaticFileHandler implements HttpHandler {

        private  String file_Name;
        public StaticFileHandler(String fileName) {
            file_Name = fileName;
        }

        @Override
        public void handle(HttpExchange http_Exchange){
            try {
                /*Reads static file from serverpath*/
                File file = new File(server_WebFolder + file_Name);
                if (file == null) {
                    String response = "Error 404: File not found.";
                    errorHandler(http_Exchange, response, 404);
                } else {
                    htmlHandler(http_Exchange, file);
                }
            }catch (Exception e){
                String response = "Error 500: Internal Server Error.";
                errorHandler(http_Exchange, response, 500);
            }
        }

    }

    /**
     *Manages POST request of files from client
     */
    private class POSTFileHandler implements HttpHandler {
        public void handle(HttpExchange http_Exchange){
            try{
                Headers header = http_Exchange.getRequestHeaders();
                String request_Referer = header.getFirst("Referer");
                request_Referer = getURLFromRequest(request_Referer);
                String input_Recieved = "";
                /*Creates buffer*/
                final byte[] byte_Array = new byte[64000];
                int temp_Counter;
                while ((temp_Counter = http_Exchange.getRequestBody().read(byte_Array)) >= 0) {
                    input_Recieved += new String(byte_Array, 0, temp_Counter, "ISO-8859-1");
                }
                String input_parts[] = input_Recieved.split("\r\n\r\n", 2);
                String input_Header = input_parts[0];
                String file_Name = searchString("filename=", input_Header);

                file_Name = file_Name.replaceAll("\"", "");
                input_parts = input_parts[1].split("------", 2);
                String input_Payload = input_parts[0];
                /*If file is to large, deny request from client*/
                if (input_Recieved.length() > limitSize) {
                    throw new Exception();
                }
                /*Creates new file in clients folder*/
                File file = new File(folder_Current + file_Name);
                /*Writes payload to file*/
                FileOutputStream file_OutputStream = new FileOutputStream(file);
                file_OutputStream.write(input_Payload.getBytes("ISO-8859-1"));
                file_OutputStream.close();

                String file_Size = getfileSize(input_Payload.length());
                System.out.println("Client posted: " + file_Name + ", size: " + file_Size);

                http_Server.createContext(request_Referer + file_Name, new GETFileHandler(file_Name, folder_Current));
                http_Server.createContext(request_Referer + file.getName() + "/deletefile", new DELETEFileHandler(file_Name));
                htmlHandler(http_Exchange, html_ClientContext);
            }catch (Exception e){
                String response = "Error 413: Payload to Large";
                errorHandler(http_Exchange, response, 413);
            }
        }
    }

    /**
     * Manages request to delete a file on the server
     */
    private class DELETEFileHandler implements HttpHandler {
        private final String file_Name;
        public DELETEFileHandler(String name) {

            file_Name = name;
        }
        public void handle(HttpExchange http_Exchange) {
            try {
                System.out.println("[Client requested removal of file: "+ file_Name +"]");
                Headers header = http_Exchange.getRequestHeaders();
                String request_Referer = header.getFirst("Referer");
                File file = new File (folder_Current + file_Name);
                if(file.exists()){
                    /*If file is valid, delete file and context from server*/
                    request_Referer = getURLFromRequest(request_Referer);
                    http_Server.removeContext(request_Referer+file.getName());
                    file.delete();
                    htmlHandler(http_Exchange, html_ClientContext);
                }
                else{
                    throw new Exception();
                }
            } catch (Exception e) {
                String response = "Error 404: File not found.";
                errorHandler(http_Exchange, response, 404);
            }

        }
    }

    /**
     * Manages request to delete a folder on the server
     */
    private class DELETEFolderHandler implements HttpHandler {
        private final String folder_Name;
        public DELETEFolderHandler(String name) {

            folder_Name = name;
        }
        public void handle(HttpExchange http_Exchange){
            try {
                System.out.println("[Client requested removal of folder: "+ folder_Name +"]");
                Headers header = http_Exchange.getRequestHeaders();
                String request_Referer = header.getFirst("Referer");

                File folder = new File (folder_Current);
                if(folder.exists()){
                    /*If folder exists, delete folder and content from server*/
                    request_Referer = getURLFromRequest(request_Referer);
                    http_Server.removeContext(request_Referer);
                    deleteDir(folder);
                    htmlHandler(http_Exchange, html_ClientContext);
                }
                else{
                    throw new Exception();
                }
            } catch (Exception e) {
                String response = "Error 404: Folder not found.";
                errorHandler(http_Exchange, response, 404);
            }

        }
    }

    /**
     * Manages requests to add a new folder to the server
     */
    private class ADDFolderHandler implements HttpHandler {
        private final String folder_Name;
        public ADDFolderHandler(String name) {
            folder_Name = name;
        }
        public void handle(HttpExchange exchange){
            System.out.println("[Client requested to add folder at: " + folder_Name + "]");
            Headers header = exchange.getRequestHeaders();
            File folder = new File(folder_Current);
            try {
                if (!folder.exists()) {
                    throw new IOException();
                } else {
                    String request_Referer = header.getFirst("Referer");
                    request_Referer = getURLFromRequest(request_Referer);
                    String input_Recieved = "";
                    final byte[] byte_Array = new byte[6000];
                    int temp_Count;
                    while ((temp_Count = exchange.getRequestBody().read(byte_Array)) >= 0) {
                        input_Recieved += new String(byte_Array, 0, temp_Count, "ISO-8859-1");
                    }
                    String input_Parts[] = input_Recieved.split("\r\n\r\n", 2);
                    input_Parts = input_Parts[1].split("------", 2);
                    String folder_Name = input_Parts[0].substring(0, input_Parts[0].length() - 2);
                    File folder_New = new File(folder_Current + "/" + folder_Name);
                    /*If folder already exists no new folder will be created*/
                    if (!folder_New.exists()) {                        folder_New.mkdir();
                        String tempURL = request_Referer + folder_Name+"/";
                        /*Creates all contexts for the new folder*/
                        http_Server.createContext(tempURL, new ContextHandler(folder_Current, folder_Name+"/"));
                        http_Server.createContext(tempURL + "download", new GETFolderHandler(folder_Name));
                        http_Server.createContext(tempURL + "upload", new POSTFileHandler());
                        http_Server.createContext(tempURL + "addfolder", new ADDFolderHandler(folder_Name));
                        http_Server.createContext(tempURL + "deletefolder", new DELETEFolderHandler(folder_Name));
                        htmlHandler(exchange, html_ClientContext);

                    } else {
                        throw new Exception();
                    }
                }

            }catch (IOException e) {
                String response = "Error 500: Internal Server Error";
                errorHandler(exchange, response, 500);
            } catch (Exception e) {
                String response = "Error 403: Forbidden, Folder already exists";
                errorHandler(exchange, response, 403);
            }
        }
    }


}
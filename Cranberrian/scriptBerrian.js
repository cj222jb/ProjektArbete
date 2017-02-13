

function addFileListener() {

    var files = document.getElementById("Files").childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    files.forEach(function(file){
        file.addEventListener("dblclick",function () {
            console.log(file.firstChild.innerHTML);
            httpGetAsync(file.firstChild.innerHTML);
        });

    });
}
function addFolderListener() {
    var folders = document.getElementById("Folders").childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    folders.forEach(function(folder){
        folder.addEventListener("dblclick",function () {
            console.log(folder.firstChild.innerHTML);
        });

    });

}

function httpGetAsync(theUrl)
{
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = processRequest(xmlHttp);
    xmlHttp.open("GET", theUrl, true); // true for asynchronous
    xmlHttp.send(null);
}
function processRequest(xmlHttp) {

        if (xmlHttp.readyState == 4 && xmlHttp.status == 200){
            var responce = JSON.parse(xmlHttp.responseText);
            console.log(responce.ip);
        }



}
addFolderListener();
addFileListener();
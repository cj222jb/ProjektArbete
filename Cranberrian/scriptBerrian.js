
var url;

var files = document.getElementById("files");
var folders = document.getElementById("folders");

function currentURI() {
    url =  window.location.pathname;;
    if(url === "/"){
        url ="";
    }
}
function run() {
    headerListener();
    currentURI();
    addFolderListener();
    addFileListener();
    addBackButtonListener();
}
function addFileListener() {
    var allFiles = files.childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    allFiles.forEach(function(file){
        file.addEventListener("click",function () {
            window.location.href =url+file.firstChild.innerHTML;
        });

    });
}
function addFolderListener() {
    folders.childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    folders.childNodes.forEach(function(folder){
        folder.addEventListener("click",function () {
            if(url === "/"){
                window.location.href =url + folder.firstChild.innerHTML;
            }
            else{
                window.location.href =url + folder.firstChild.innerHTML+"/";
            }
        });

    });
}
function addBackButtonListener() {
    if(url != ""){
        var backButton = document.createElement('li');
        var aTag = document.createElement('a');
        aTag.innerHTML = '...';
        backButton.appendChild(aTag);
        folders.insertBefore(backButton, folders.firstChild);
        backButton.addEventListener("click", function () {
            url = url.slice(0,-1);
            var regex = new RegExp('/[^/]*$');
            url = url.replace(regex, '/');
            window.location.href = url;
        });
    }
}
function headerListener() {
    var header = document.getElementById('header');
    header.addEventListener("click", function () {
        window.location.href = "/";
    });
}
run();
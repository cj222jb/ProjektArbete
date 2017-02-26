
var url;
var root;
var files = document.getElementById("files");
var folders = document.getElementById("folders");
var header = document.getElementById('header');
var downloadFolder = document.getElementById("downloadFolder");
var postForm = document.getElementById("uploadFile");
function currentURI() {
    url =  window.location.pathname;
    root = url.split('/')[2]+"/";
    var href = window.location.href
    postForm.setAttribute("action", href+"upload");
}
function run() {

    headerListener();
    currentURI();
    addFolderListener();
    addFileListener();
    addBackButtonListener();
    addDownloadFolderListener();
    POSThandler();
}
function addFileListener() {
    NodeList.prototype.forEach = Array.prototype.forEach
    files.childNodes.forEach(function(file){
        file.addEventListener("click",function () {
            window.location.href =url+file.firstChild.innerHTML;
        });
    });
}
function addFolderListener() {
    NodeList.prototype.forEach = Array.prototype.forEach
    folders.childNodes.forEach(function(folder){
        folder.addEventListener("click",function () {
            if(url === "/"){
                window.location.href =url + folder.firstChild.innerHTML;
            }
            else{
                window.location.href =url + folder.firstChild.innerHTML+"/";
                console.log(url + folder.firstChild.innerHTML);
            }
        });

    });
}
function addBackButtonListener() {
    if(root != "/"){
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
    header.addEventListener("click", function () {
        window.location.href = "/";
    });
}
function addDownloadFolderListener() {
    downloadFolder.addEventListener("click", function () {
        console.log(url);
        if(url === "/"){
            window.location.href =url + "/download";
        }
        else{
            window.location.href =url + "download";
        }
    });
}
function POSThandler(){
    var temp1 = url.substr(url.lastIndexOf('/') + 1);
    var temp2 = url.slice(0, url.lastIndexOf('/') + 1);
    if(temp1 === "upload"){
        window.location.href = temp2;
    }
}
run();
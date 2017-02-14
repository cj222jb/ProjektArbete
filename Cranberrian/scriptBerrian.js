
var url;

var files = document.getElementById("files");
var folders = document.getElementById("folders");

function currentURI() {
    url =  window.location.pathname;;
    if(url === "/"){
        url ="";
    }

}
function addFileListener() {
    var allFiles = files.childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    allFiles.forEach(function(file){
        file.addEventListener("dblclick",function () {
            console.log(url +"/"+file.firstChild.innerHTML);
            window.location.href =url +"/"+file.firstChild.innerHTML;
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
        backButton.addEventListener("dblclick", function () {
            url = url.slice(0,-1);
            var regex = new RegExp('/[^/]*$');
            url = url.replace(regex, '/');
            window.location.href = url;
        })
    }
}
function addFolderListener() {
    folders.childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    folders.childNodes.forEach(function(folder){
        folder.addEventListener("dblclick",function () {
            if(url === "/"){
                console.log(window.location.href =url + folder.firstChild.innerHTML)
                window.location.href =url + folder.firstChild.innerHTML;
            }
            else{
                window.location.href =url + folder.firstChild.innerHTML+"/";
            }
        });

    });
}
currentURI();
console.log(url)
addFolderListener();
addFileListener();
addBackButtonListener();


function addFileListener() {

    var files = document.getElementById("Files").childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    files.forEach(function(file){
        file.addEventListener("dblclick",function () {
            console.log(file.firstChild.innerHTML);
           window.location.href =file.firstChild.innerHTML;
        });

    });
}
function addFolderListener() {
    var folders = document.getElementById("Folders").childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    folders.forEach(function(folder){
        folder.addEventListener("dblclick",function () {
            console.log(folder.firstChild.innerHTML);
            httpGetAsync("/"+file.firstChild.innerHTML);
        });

    });

}

addFolderListener();
addFileListener();
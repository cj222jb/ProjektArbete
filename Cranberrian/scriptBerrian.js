

function addFileListener() {

    var files = document.getElementById("Files").childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    files.forEach(function(file){
        file.addEventListener("click",function () {
            console.log(file.firstChild.innerHTML);
        });

    });
}
function addFolderListener() {
    var folders = document.getElementById("Folders").childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    folders.forEach(function(folder){
        folder.addEventListener("click",function () {
            console.log(folder.firstChild.innerHTML);
        });

    });

}
addFolderListener();
addFileListener();
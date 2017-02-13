

function addFileListener() {

    var files = document.getElementById("Files").childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    files.forEach(function(file){
        file.addEventListener("dblclick",function () {
            console.log(file.firstChild.innerHTML);
            window.location.href =window.location.pathname +"/"+file.firstChild.innerHTML;
        });

    });
}

function addFolderListener() {
    var folders = document.getElementById("Folders").childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    folders.forEach(function(folder){
        folder.addEventListener("dblclick",function () {
            console.log(folder.firstChild.innerHTML);
            var path = window.location.pathname;
            console.log(path);
            if(path === "/"){
            window.location.href =path + folder.firstChild.innerHTML;
            }
            else{
                window.location.href =path +"/"+ folder.firstChild.innerHTML;
            }
        });

    });

}
addFolderListener();
addFileListener();
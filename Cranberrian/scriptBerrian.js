

function addDirListener() {
    
}
function addFileListener() {

    var files = document.getElementById("Files").childNodes;
    NodeList.prototype.forEach = Array.prototype.forEach
    files.forEach(function(file){
        file.addEventListener("click",function () {
            console.log(file.firstChild.innerHTML);
        });
        file.addEventListener("right-click",function () {
            console.log(file.firstChild.innerHTML+" Heeeeeej");
        });
    });
}
addFileListener();
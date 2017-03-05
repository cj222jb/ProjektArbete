
var url;
var root;
var lastURL;
var files = document.getElementById("files");
var folders = document.getElementById("folders");
var header = document.getElementById("header");
var optionBar = document.getElementsByClassName("optionBar")[0];
var downloadFolder = document.getElementById("downloadFolder");
var deleteFolder = document.getElementById("deleteFolder");
var addFolder = document.getElementById("addFolder");
var postFormFile = document.getElementById("uploadFile");
var postFormFolder = document.getElementById("uploadFolder");
function currentURI() {
    url =  window.location.pathname;
    root = "/"+ url.split('/')[1]+"/";
    lastURL = url.substr(url.lastIndexOf('/') + 1);
    if(url != root){
        addDeleteFolderButton();
    }
    var href = window.location.href
    postFormFile.setAttribute("action", href+"upload");
    postFormFolder.setAttribute("action", href+"addfolder");
}
function run() {
    headerListener();
    currentURI();
    foldersListener();
    filesListener();
    addBackButton();
    addDownloadFolderListener();
    POSTHandler();
    DELETEFileHandler();
    deleteFileListener();
    ADDFolderHandler();
   // addFolderListener();
}
function addDeleteFolderButton() {

    if(deleteFolder === null){
        var tempLi = document.createElement('li');
        var tempA = document.createElement('a');
        tempA.innerHTML = 'Delete Folder';
        tempLi.appendChild(tempA);
        tempLi.setAttribute("id","deleteFolder");
        optionBar.appendChild(tempLi);

        deleteFolder = document.getElementById("deleteFolder");
        DELETEFolderHandler();
        deleteFolderListener();

    }

}
function filesListener() {
    files.childNodes.forEach(function(file){
        var aTag = file.childNodes[2];
        if(aTag != null){
            aTag.addEventListener("click",function () {
                window.location.href =url+aTag.innerHTML;
            });
        }
    });
}
function foldersListener() {
    folders.childNodes.forEach(function(folder){
        var aTag = folder.firstChild;
        if(aTag != null){
            aTag.addEventListener("click",function () {
                if(url === "/"){
                    window.location.href =url + folder.firstChild.innerHTML;
                }
                else{
                    window.location.href =url + folder.firstChild.innerHTML+"/";
                }
            });
        }
    });
}
function addBackButton() {
    if(url != root){
        var folderBack = document.createElement('li');
        var aTag = document.createElement('a');
        aTag.innerHTML = '. . .';
        folderBack.appendChild(aTag);
        folderBack.setAttribute("id","upDirectory")
        folders.insertBefore(folderBack, folders.firstChild);
        folderBack.addEventListener("click", function () {
            url = url.slice(0,-1);
            var regex = new RegExp('/[^/]*$');
            url = url.replace(regex, '/');
            window.location.href = url;
        });
    }
}
function headerListener() {
    header.addEventListener("click", function () {
        window.location.href = root ;
    });
}
function addDownloadFolderListener() {
    downloadFolder.addEventListener("click", function () {
        if(url === "/"){
            window.location.href =url + "/download";
        }
        else{
            window.location.href =url + "download";
        }
    });
}
function POSTHandler(){
    var temp2 = url.slice(0, url.lastIndexOf('/') + 1);
    if(lastURL === "upload"){
        window.location.href = temp2;
    }
}
function DELETEFileHandler(){
    var temp2 = url.slice(0, url.lastIndexOf('/'));
    temp2 = temp2.slice(0, temp2.lastIndexOf('/')+1);
    if(lastURL === "deletefile"){
        window.location.href = temp2;
    }
}
function DELETEFolderHandler(){
    var temp2 = url.slice(0, url.lastIndexOf('/'));
    temp2 = temp2.slice(0, temp2.lastIndexOf('/')+1);
    if(lastURL === "deletefolder"){
        window.location.href = temp2;
    }
}
function ADDFolderHandler(){
    var temp = url.slice(0, url.lastIndexOf('/')+1);
    if(lastURL === "addfolder"){
        window.location.href = temp;
    }
}


function deleteFileListener() {
    files.childNodes.forEach(function(file){
        var aTag = file.childNodes[2];
        var deleteTag = file.lastChild;
        if(deleteTag != null){
            deleteTag.addEventListener("click",function () {
                var result = confirm("Want to delete: "+aTag.innerHTML);
                if (result) {
                    window.location.href =url+aTag.innerHTML+"/deletefile";
                }
            });
        }
    });

}

function deleteFolderListener() {
    var temp = url.slice(0, url.lastIndexOf('/'));
    temp = temp.substr(temp.lastIndexOf('/')+1);

    deleteFolder.addEventListener("click",function () {
        var result = confirm("Want to delete: "+temp);
        if (result) {
            window.location.href =url+"deletefolder";
        }
    });
}
function addFolderListener() {
    addFolder.addEventListener("click",function () {
            window.location.href =url+"addfolder";
    });
}
run();
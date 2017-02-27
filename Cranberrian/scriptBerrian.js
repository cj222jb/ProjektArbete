
var url;
var root;
var files = document.getElementById("files");
var folders = document.getElementById("folders");
var header = document.getElementById('header');
var downloadFolder = document.getElementById("downloadFolder");
var postForm = document.getElementById("uploadFile");
function currentURI() {
    url =  window.location.pathname;
    root = "/"+ url.split('/')[1]+"/";
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
    deleteFileListener();
    listenToFileChooser();
}
function addFileListener() {
    files.childNodes.forEach(function(file){
        var aTag = file.childNodes[2];
        if(aTag != null){
            aTag.addEventListener("click",function () {
                window.location.href =url+aTag.innerHTML;
            });
        }
    });
}
function addFolderListener() {
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
function addBackButtonListener() {
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
function POSThandler(){
    var temp1 = url.substr(url.lastIndexOf('/') + 1);
    var temp2 = url.slice(0, url.lastIndexOf('/') + 1);
    if(temp1 === "upload"){
        window.location.href = temp2;
    }
}
function listenToFileChooser(){
    // var upload = document.getElementById("upload");
    // upload.addEventListener('change', function () {
    //  document.forms["uploadFile"].submit;
    // });

}

function deleteFileListener() {
    files.childNodes.forEach(function(file){
        var aTag = file.childNodes[2];
        var deleteTag = file.lastChild;
        if(deleteTag != null){
            deleteTag.addEventListener("click",function () {
                var result = confirm("Want to delete: "+aTag.innerHTML);
                if (result) {
                }
            });
        }
    });

}
run();
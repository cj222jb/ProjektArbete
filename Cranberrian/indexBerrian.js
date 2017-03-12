/**
 * Created by Mikael Andersson on 2017-03-10.
 */

var userList= document.getElementById("userDiv");;


function run() {
allUsersListener();
}
function allUsersListener() {
    userList.childNodes.forEach(function(user){
        var aTag = user.childNodes[0];

        if(aTag != null){
            aTag.addEventListener("click",function () {
                window.location.href =aTag.innerHTML+"/";
            });
        }
    });
}
run();
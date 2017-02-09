

    console.log("hejsan")
    var element = document.createElement("Input");
    element.setAttribute("type", "button");
    element.setAttribute("value", "button");
    element.setAttribute("name", "button");

    var div = document.createElement("div");
    div.innerHTML = "topdiv";
    div.appendChild(element);
    document.body.appendChild(div);

    element.addEventListener("click",function () {
       console.log("Penis")
    });
/**
 * Created by mikaelandersson on 2017-02-02.
 */
/**
 * Created by mikaelandersson on 30/09/16.
 */

    console.log("hej")
    var cssId = 'myCss';  // you could encode the css path itself to generate id..
    if (!document.getElementById(cssId))
    {
        var head  = document.getElementsByTagName('head')[0];
        var link  = document.createElement('link');
        link.id   = cssId;
        link.rel  = 'stylesheet';
        link.type = 'text/css';
        link.href = 'style.css';
        link.media = 'all';
        head.appendChild(link);
    }

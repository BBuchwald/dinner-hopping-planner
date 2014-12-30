var browser_fix = function (){
    if (/MSIE (\d+\.\d+);/.test(navigator.userAgent)){
        var ie_version = new Number(RegExp.$1);
        if (ie_version <= 6){
            alert("The version of Microsoft Internet Explorer (version " + ie_version + ") you are using is no longer supported by CloudMade.\n\nIf you have any more questions, please contact CloudMade's support team - support@cloudmade.com.")
        }
    }
}

window.onload = function(){
    browser_fix();
}
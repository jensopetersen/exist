xquery version "1.0";

(:~ Retrieve package list from server. Needed to work around cross-site-scripting browser restrictions. :)
declare option exist:serialize "method=html media-type=text/html";

let $url := request:get-parameter("url", "http://192.168.2.104:8080/exist/apps/public-repo/retrieve.html")
return
    if ($url) then
        httpclient:get($url, false(), ())//httpclient:body/*
    else
        response:set-status-code(404)
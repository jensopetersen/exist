xquery version "1.0" encoding "UTF-8";

declare namespace t="http://xproc.org/ns/testsuite";
declare namespace c="http://www.w3.org/ns/xproc-step";
declare namespace file="http://exist-db.org/xquery/util";

import module namespace const = "http://xproc.net/xproc/const";
import module namespace xproc = "http://xproc.net/xproc";
import module namespace u = "http://xproc.net/xproc/util";
import module namespace naming = "http://xproc.net/xproc/naming";

declare option exist:serialize "method=html media-type=text/html omit-xml-declaration=yes indent=yes";

let $not :=  request:get-parameter("not", ())
let $type :=  request:get-parameter("type", ())
let $dir := concat('/Users/jimfuller/Source/Thirdparty/eXist/extensions/xprocxq/main/test/tests.xproc.org/',$type)
let $file :=  request:get-parameter("file", ())
let $files := file:directory-list($dir,$file)
let $result := for $file in $files/*:file[not(contains(@name,$not))]

let $path := concat('file://',$dir,string($file/@name))

let $stdin1 :=doc($path)/t:test

let $stdin :=  if ($stdin1//t:pipeline/@href) then

<t:test xmlns:t="http://xproc.org/ns/testsuite"
        xmlns:p="http://www.w3.org/ns/xproc"
        xmlns:c="http://www.w3.org/ns/xproc-step"
        xmlns:err="http://www.w3.org/ns/xproc-error">

{$stdin1//t:input}

<t:pipeline>
{let $doc := doc(concat('file://',$dir,$stdin1//t:pipeline/@href))
return
    $doc
}
</t:pipeline>

{$stdin1//t:output}
</t:test>
               else
                 $stdin1

let $pipeline := if (count($stdin//t:input) = 0) then
        doc('/db/xproc/unit-test/test-runner2.xml')
    else if ( count($stdin//t:input) = 1) then
        doc('/db/xproc/unit-test/test-runner.xml')
    else
        doc('/db/xproc/unit-test/test-runner1.xml')

let $runtime-debug := request:get-parameter("dbg", ())
    return
    <test file="{$file/@name}">
        {
        if (contains($path,request:get-parameter("debug", ()))) then
           xproc:run($pipeline,u:serialize($stdin,$const:ESCAPE_SERIALIZE),$runtime-debug)
        else
            util:catch('*', xproc:run($pipeline,$stdin,$runtime-debug), 'test crashed')
         }
     </test>
return
let $final-result := <result pass="{count($result//c:result[.= 'true'])}" nopass="{count($result//c:result[.= 'false'])}" total="{count($result//test)}">
    {$result}
</result>
let	$params :=
		<parameters>
			<param name="now" value="{current-time()}"/>
		</parameters>
return
    if (request:get-parameter("format", ()) eq 'html') then
      transform:transform(document{$final-result}, xs:anyURI("xmldb:exist:///db/xproc/unit-test/test-result.xsl"), ())
    else
        $final-result
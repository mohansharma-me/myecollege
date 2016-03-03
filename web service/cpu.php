<?php
$stopKey="dontWriteJSON";
$outputArray=array();
$outputArray["success"]=false;
if(count($keys)>1) {
	$page="";
	switch($keys[1]) {
		case "get-data": $page="cpu.get_data.php"; break;
		case "login": $page="cpu.login.php"; break;
		case "activation": $page="cpu.activation.php"; break;
		case "account": $page="cpu.account.php"; break;
		case "update": $page="cpu.update.php"; break;
	}
	
	if(strlen($page)>0) {
		include_once $page;
	}
}
$outputArray["deviceId"]=session_id();
if(isset($outputArray[$stopKey]) && is_bool($outputArray[$stopKey]) && $outputArray[$stopKey]) {
	
} else {
	$json=json_encode($outputArray);
	if(isset($_POST["encodeBase64"]))
		$json=base64_encode($json);
	header("Content-Length: ".strlen($json));
	echo $json;
}
<?php
require "./inc.constants.php";
require "./inc.methods.php";

if(isset($_REQUEST["deviceId"])) {
	session_id($_REQUEST["deviceId"]);
} else {
	session_id(uniqid());
}
session_start();
//sleep(5);
$keys=getKeys();
if(count($keys)==0) {
	echo "This is homepage.<br/>";
	echo "Mohan: ".getPassword("mohan")."<br/>";
	echo session_id()."<br/><pre>";
	if(isset($_SESSION["count"]))
		$_SESSION["count"]++;
	print_r($_SESSION);
	echo "</pre><br/><br/>";
} else {
	$page=getPage($keys[0]);
	if(isset($page)) {
		require $page;
	} else {
		include "./error.php";
	}
}

$lastRequest=json_encode($_REQUEST);
file_put_contents("lastRequest.json",$lastRequest);

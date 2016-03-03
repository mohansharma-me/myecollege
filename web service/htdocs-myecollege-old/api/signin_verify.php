<?php
if(isset($_POST['user']) && isset($_POST['session'])) {
	$user=addslashes(strtolower(trim($_POST['user'])));
	$session=addslashes(strtolower(trim($_POST['session'])));
	
	include_once "..\\classes.php";
	mysql_query("select 1 from online where user LIKE '$user' and uid LIKE '$session'");
	if(mysql_affected_rows()==1) {
		echo "true";
	} else {
		echo "false";
	}
}
?>
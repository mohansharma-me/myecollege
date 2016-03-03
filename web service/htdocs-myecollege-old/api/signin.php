<?php
class Signin {
	public $isFac;
	public $isSigned;
	public $session;
	public $isActivated;
	public $data;
}
if(isset($_POST['user']) && isset($_POST['pass'])) {
	$user=addslashes(strtolower(trim($_POST['user'])));
	$pass=addslashes(strtolower(trim($_POST['pass'])));
	
	include_once "..\\classes.php";
	$isFac=false;
	$query="";
	if(is_email($user)) {
		$isFac=true;
		$query="select * from faculties where primary_email LIKE '$user' and (password LIKE '$pass' or isactivated=0)";
	} else if(is_numeric($user)) {
		$isFac=false;
		$query="select * from students where enrollment_no LIKE '$user' and (password LIKE '$pass' or isactivated=0)";
	}
	$si=new Signin();
	$si->isFac=$isFac;
	$si->isSigned=false;
	$si->isActivated=0;
	$q=mysql_query($query);
	if(mysql_affected_rows()==1) {
		$row=mysql_fetch_assoc($q);
		$row["password"]="";
		include_once "..\\funcs.images.php";
		$row["profile_picture"]=base64_encode(resizeImage3($row["profile_picture"],100,100,true));//base64_encode($row["profile_picture"]);
		$si->data=$row;
		$si->isActivated=$row["isactivated"];
		if($si->isActivated) {
			$uid="";
			$flag=true;
			while($flag) {
				$uid=uniqid();
				$result=mysql_query("select * from online where uid='$uid'");
				if(mysql_affected_rows()==0) {
					break;
				}
			}
			$si->session=$uid;
			mysql_query("delete from online where user LIKE '$user'");
			mysql_query("insert into online(user,uid,isfac) values('$user','$uid','".($isFac?"true":"false")."')");
			if(mysql_affected_rows()==1) {
				$si->isSigned=true;
			}
		} else {
			$si->isSigned=true;
			$si->data=null;
		}
	}
	echo json_encode($si);
}
?>

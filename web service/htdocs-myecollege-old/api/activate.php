<?php
class Activation {
	public $isSent;
	public $isAlready;
	public $isValid;
	public $email;
}
if(isset($_POST['user'])) {
	$user=addslashes(strtolower(trim($_POST['user'])));
	
	include_once "..\\classes.php";
	$isFac=false;
	$query="";
	if(is_email($user)) {
		$isFac=true;
		$query="select primary_email,isactivated from faculties where trim(lower(primary_email)) LIKE '$user'";
	} else if(is_numeric($user)) {
		$isFac=false;
		$query="select primary_email,isactivated from students where trim(lower(enrollment_no)) LIKE '$user'";
	}
	$si=new Activation();
	$si->isSent=false;
	$si->isAlready=false;
	$q=mysql_query($query);
	if(mysql_affected_rows()==1) {
		$si->isValid=true;
		$row=mysql_fetch_assoc($q);
		if($row["isactivated"]=="0") {
			$email=$row["primary_email"];
			$si->email=$email;
			$si->isAlready=false;
			$vcode=uniqid();
			$res=mysql_query("insert into verification(user,vcode,primary_email) values('$user','$vcode','$email')");
			if(mysql_affected_rows()==1) {
				$subject="Verification code for [$user]";
				$message="Verification code: $vcode";
				if(mail($email,$subject,$message,"From: no-reply@myecollege.com\n")) {
					$si->isSent=true;
				} else {
					$si->isSent=false;
				}
			} else {
				$si->isSent=false;
			}
		} else {
			$si->isAlready=true;
			$si->isSent=false;
		}
	} else {
		$si->isAlready=false;
		$si->isSent=false;
		$si->isValid=false;
	}
	echo json_encode($si);
}
?>
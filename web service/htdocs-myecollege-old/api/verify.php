<?php
class Verify {
	public $isVerified;
	public $email;
}
if(isset($_POST['vcode'])) {
	$vcode=addslashes(strtolower(trim($_POST['vcode'])));
	include_once "..\\classes.php";
	
	$si=new Verify();
	$si->isVerified=false;
	$q=mysql_query("select * from verification where lower(vcode) LIKE '$vcode'");
	if(mysql_affected_rows()>0) {
		$row=mysql_fetch_assoc($q);
		$user=$row["user"];
		$email=$row["primary_email"];
		$si->email=$email;
		$isfac=false;
		$query="";
		if(is_email($user)) {
			$query="update faculties set isactivated=1,password='$vcode' where lower(primary_email) LIKE '$user'";
		} else if(is_numeric($user)) {
			$query="update students set isactivated=1,password='$vcode' where lower(enrollment_no) LIKE '$user'";
		}	
		mysql_query($query);
		if(mysql_affected_rows()==1) {
			mysql_query("delete from verification where lower(user) LIKE '$user'");
			$si->isVerified=true;
		}
	}
	echo json_encode($si);
}
?>
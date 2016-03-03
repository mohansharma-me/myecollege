<?php
if(isset($keys[2])) {
	$data=$keys[2];
	switch($data) {
		case "forgot-password":
			
			$userId=filterInput(INPUT_POST,"userId",true,false,true);
			if(isset($userId)) {
				$userId=base64_decode($userId);
				$res=sql('select * from account where lower(account_user_id)="'.$userId.'"');
				if(mysql_affected_rows()>0) {
					$row=mysql_fetch_assoc($res);
					
					if(strlen(trim($row["account_email_address"]))>0) {
						if(isEmail($row["account_email_address"])) {
							$minRand=rand(0,55555);
							$maxRand=rand(55555,99999);
							$randPass=rand($minRand,$maxRand);
							$code=getPassword($randPass);
							
							$res=sql('update account set account_password="'.$code.'" where account_id='.$row["account_id"]);
							
							$html="<html><body>Dear ".$row["account_name"].",<br/>Your new password is:<br/><br/><b>$randPass</b><br/><br/>Thank you.</body></html>";
							if(sendMail($row["account_email_address"],"Your new password for \"".$row["account_name"]."\" account (My eCollege)",$html,"admin@myecollege.com")) {
								$outputArray["success"]=true;
								$outputArray["success_message"]="Thank you, your new password was sent to '".$row["account_email_address"]."'";
							} else {
								$outputArray["error_message"]="Unable to send mail to '".$row["account_email_address"]."'";
							}
						} else {
							$outputArray["error_message"]="Sorry, College updated invalid e-mail address.";
						}
					} else {
						$outputArray["error_message"]="Sorry, College didn't updated your e-mail address.";
					}
					
				} else if(mysql_affected_rows()==0) {
					$outputArray["error_message"]="Sorry, User ID is Invalid.";
				} else {
					$outputArray["error_message"]="Sorry, unable to interact with server.";
				}
			}
			
		break;
	}
}
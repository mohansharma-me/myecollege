<?php
if(isset($keys[2])) {
	$data=$keys[2];
	switch($data) {
		case "request-code":
			
			$userId=filterInput(INPUT_POST,"userId",true,false,true);
			if(isset($userId)) {
				$userId=base64_decode($userId);
				$res=sql('select * from account where lower(account_user_id)="'.$userId.'"');
				if(mysql_affected_rows()>0) {
					$row=mysql_fetch_assoc($res);
					
					if(strlen(trim($row["account_email_address"]))>0) {
						if(isEmail($row["account_email_address"])) {
							$code=$row["account_id"]."|".$row["account_user_id"];
							$code=getPrivateHash($code);
							$html="<html><body>Dear ".$row["account_name"].",<br/>Your Account Activation code is:<br/><br/><b>$code</b><br/><br/>Thank you.</body></html>";
							if(sendMail($row["account_email_address"],"Activation Code for \"".$row["account_name"]."\" (My eCollege)",$html,"admin@myecollege.com")) {
								$outputArray["success"]=true;
								$outputArray["success_message"]="Thank you, your activation code was sent to '".$row["account_email_address"]."'";
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
		
		case "activate-now":
		
			$code=filterInput(INPUT_POST,"activationCode",true,false,true);
			if(isset($code)) {
				$code=base64_decode($code);
				$res=sql('select account_id, account_name from account where account_activation_status="0" and account_activation_code="'.$code.'"');
				if(mysql_affected_rows()>0) {
					$row=mysql_fetch_assoc($res);
					$res=sql('update account set account_activation_status=1 where account_id='.$row["account_id"]);
					if(mysql_affected_rows()>0) {
						$outputArray["success"]=true;
						$outputArray["success_message"]="Congratulation ".$row["account_name"].", your account successfully activated. Now you can use same code as your password for first time login.";
					} else if(mysql_affected_rows()==0) {
						$outputArray["error_message"]="Sorry, unable to reflect activation process to server.";
					} else {
						$outputArray["error_message"]="Sorry, unable to interact with server.";
					}
				} else if(mysql_affected_rows()==0) {
					$outputArray["error_message"]="Sorry, Invalid Activation Code.";
				} else {
					$outputArray["error_message"]="Sorry, unable to interact with server.";
				}
			}
		
		break;
	}
}
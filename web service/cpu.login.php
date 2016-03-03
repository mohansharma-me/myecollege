<?php
if(count($keys)==2) {
	$userId=Input("userId");
	$userPassword=Input("userPassword");
	$deviceId=Input(DEVICE_ID);
	if(isset($deviceId, $userId,$userPassword)) {
		if(!isset($_GET["debug"])) {
			$userId=base64_decode($userId);
			$userPassword=base64_decode($userPassword);
		}
		//$userId=$username;
		//$userPassword=$password;
		
		if(is_string($userId) && is_string($userPassword)) {
			$res=sql('select * from account where lower(account_user_id)="'.addslashes($userId).'" limit 1');
			if(mysql_affected_rows()>0) {
				$row=mysql_fetch_assoc($res);
				if($row["account_activation_status"]!=0 && $row["account_activation_status"]>0) {
					if(strcmp($row["account_password"],getPassword($userPassword))==0) {
						unset($row["account_password"]);
						unset($row["account_activation_code"]);
						unset($row["account_avtar"]);
						//$row["account_avtar"]=encodeBinary(imageCompressIcon($row["account_avtar"],1));
						
						$outputArray["userData"]=$row;
						$outputArray["success"]=true;
						//session_destroy();
						session_unset();
						session_id($deviceId);
						$_SESSION["userId"]=$row["account_user_id"];
						//session_id($row["account_user_id"]);
						//$outputArray["session_id"]=session_id();
						//session_start();
						//$_SESSION["deviceId"]=$deviceId;
						$_SESSION["count"]=0;
					} else {
						$outputArray["errorFor"]="userPassword";
						$outputArray["error_message"]="Invalid Password!!";
					}
				} else {
					if($row["account_activation_status"]==0) {
						$outputArray["errorFor"]="noactivation";
						$outputArray["error_message"]="Your account isn't activated yet, Activate your account first.";
					} else {
						$outputArray["errorFor"]="deactivated";
						$outputArray["error_message"]="Your account is de-activated. Please contact your college.";
					}
				}
			} else if(mysql_affected_rows()==0) {
				$outputArray["errorFor"]="userId";
				$outputArray["error_message"]="Invalid User ID!!";
			} else if(mysql_affected_rows()<0) {
				$outputArray["error_message"]="There is problem while interacting with data server, please try again. #1";
			}
		}
	}
	
	if(!$outputArray["success"]) {
		session_unset();
		session_id(uniqid());
	}
} else if(count($keys)>2) {
	$page=$keys[2];
	switch($page) {
		case "verifyDevice":
			$deviceId=Input(DEVICE_ID);
			$userId=Input("userId");
			if(isset($deviceId,$userId)) {
				if(isset($_SESSION["userId"])) {
					if(strcmp($_SESSION["userId"],$userId)==0) {
						$outputArray["success"]=true;
					}
				}
			}
		break;
	}
}
<?php
if(!isset($_SESSION["userId"])) {
	$outputArray["error_message"]="Please login first to attempt updates.";
	return;
}

$userId=$_SESSION["userId"];
$res=sql('select * from account left join profile on profile_account_id=account_id where lower(account_user_id)='.$userId.' limit 1');
if(mysql_affected_rows()<=0) {
	$outputArray["error_message"]="Please login again to update details.";
	return;
}
$accountRow=mysql_fetch_assoc($res);
$accountId=$accountRow["account_id"];
$_profileId=null;
if(isset($accountRow["profile_id"])) {
	$_profileId=$accountRow["profile_id"];
}


$updateIn=Input("updateIn");
if(isset($updateIn)) {
	switch($updateIn) {

		case "put-chat":
			
			if(isset($_profileId)) {
			
				date_default_timezone_set("Asia/Calcutta");
				//echo date("Y-m-d H:i:s",time());
			
				$toId=Input("toId");
				$msg=Input("msg");
				if(isset($msg)) {
					$res=sql("select profile_first_name, profile_last_name from profile where profile_id=$_profileId");
					$tmpRow=mysql_fetch_assoc($res);
					$fromName=$tmpRow["profile_first_name"]." ".$tmpRow["profile_last_name"];
					
					$res=sql("select profile_first_name, profile_last_name from profile where profile_id=$toId");
					
					$tmpRow=mysql_fetch_assoc($res);
					$toName=$tmpRow["profile_first_name"]." ".$tmpRow["profile_last_name"];
					
					$msg1=array();
					$msg1["text"]=$msg;
					$dataText=mysql_escape_string(json_encode($msg1));
					
					$res=sql('insert into chat(chat_propagation_type,chat_from_id,chat_to_id,chat_timestamp,chat_message_type,chat_message,chat_status,chat_from_name,chat_to_name) values("'.PROPAGATION_UNICAST.'","'.$_profileId.'","'.$toId.'","'.date("Y-m-d H:i:s",time()).'","0","'.$dataText.'","'.CHAT_STATUS_NEW.'","'.$fromName.'","'.$toName.'")');
					
					$res=sql("select last_insert_id() as lastid");
					$tmpRow=mysql_fetch_assoc($res);
					$newId=$tmpRow["lastid"];
					
					$res=sql("select * from chat where chat_id=$newId");
					$outputArray["chat"]=mysql_fetch_assoc($res);
					
					$outputArray["success"]=mysql_affected_rows()>0;
				}
			
			}
		
		break;
	
		case "messages":
	
			if(isset($_profileId)) {
				$res=sql("update chat set chat_status=".CHAT_STATUS_OLD." where chat_to_id=$_profileId and chat_propagation_type=".PROPAGATION_UNICAST." and chat_status=".CHAT_STATUS_NEW);
				
				echo mysql_error();
				
				$outputArray["success"]=true;
			}
		
		break;
		
		case "timeline":
			//sleep(5);
			if(!isset($_profileId)) {
				$outputArray["error_message"]="You didn't completed your profile setup, please do your profile setup.\nYou can't post before profile setup.";
				return;
			}
		
			$timelineType=Input("timelineType");
			$timelineData=Input("timelineData",false);
			$encoded=Input("encoded");
			if(!isset($encoded)) $encoded=FALSE;
			
			if(isset($timelineType,$timelineData)) {
				try {
					if($encoded==TRUE) {
						$timelineData=decodeBinary($timelineData);
					}
					$json=json_decode($timelineData,true);
					//$row["timeline_data"]='{"data_raw":{"timelineText":"This is data which is displayed below the timeline heading.","timelineImageUri":"1312313123123","timelineImage":"'.base64_encode(file_get_contents("city.jpg")).'"},"data_heading":"Timeline Heading 1"}';
					
					$heading="";
					if(isset($json["heading"])) {
						$heading=$json["heading"];
					}
					
					$raw=array();
					
					// general timeline text
					if(isset($json["timelineText"])) {
						$raw["timelineText"]=$json["timelineText"];
					}
					
					if($timelineType==TIMELINE_IMAGE) {
						if(isset($json["timelineImage"])) {
							$imageData=decodeBinary($json["timelineImage"],true);
						}
					}
					
					if(strlen(trim($heading))==0) {
						$outputArray["error_message"]="No heading is given.";
					} else {
						$newJson=array();
						$newJson["data_heading"]=$heading;
						$newJson["data_raw"]=json_encode($raw);
						$newTimelineData=addslashes(json_encode($newJson));
						
						$res=sql('insert into timeline(timeline_profile_id,timeline_college_id,timeline_type,timeline_data) values("'.$_profileId.'","0","'.$timelineType.'","'.$newTimelineData.'")');
						echo mysql_error();
						if(mysql_affected_rows()==1) {
							$outputArray["success"]=true;
						} else {
							$outputArray["error_message"]="Error while interacting with data center.";
						}
					}
					
					
				} catch(Exception $e) {
					$outputArray["error_message"]="Error while parsing timeline data, please try again.";
				}
			}
			
		break;
		
		case "profile":
			
			$firstName=Input("firstName");
			$lastName=Input("lastName");
			$mobileNumber=Input("mobileNumber");
			$emailAddress=Input("emailAddress");
			$timelineStatus=Input("timelineStatus");
			$gender=Input("gender");
			$dateOfBirth=Input("dateOfBirth");
			$semester=Input("semester");
			$address=Input("address");
			$profilePic=Input("profilePic");
			
			if(isset($profilePic)) {
				$filepath="./".PROFILE_PICTURE_FOLDER."/".getPrivateHash($userId);
				file_put_contents($filepath,decodeBinary($profilePic,true));
			}
			
			$query="";
			
			sql("select * from profile where profile_account_id=$accountId limit 1");
			if(mysql_affected_rows()==0) { // insert new profile
				$outputArray["isNewProfile"]=true;
				sql('insert into profile(profile_account_id,profile_timeline_status) values('.$accountId.',"Hii! I\'m using My eCollege :).")');
			}
			
			$flagValues=(isset($firstName) || isset($lastName) || isset($mobileNumber) || isset($emailAddress) || isset($gender) || isset($dateOfBirth) || isset($semester) || isset($address) || isset($timelineStatus));
			
			if($flagValues) {
			
				$flagToAddComma=$flagValues?"":",";
				
				$query="update profile set ";
				if(isset($firstName)) $query.='profile_first_name="'.$firstName.'",';
				if(isset($lastName)) $query.='profile_last_name="'.$lastName.'",';
				if(isset($mobileNumber)) $query.='profile_mobile_number="'.$mobileNumber.'",';
				if(isset($emailAddress)) $query.='profile_email_address="'.$emailAddress.'",';
				if(isset($timelineStatus)) $query.='profile_timeline_status="'.$timelineStatus.'",';
				if(isset($gender)) $query.='profile_gender="'.$gender.'",';
				if(isset($dateOfBirth)) $query.='profile_dateofbirth="'.$dateOfBirth.'",';
				if(isset($semester)) $query.='profile_semester="'.$semester.'",';
				if(isset($address)) $query.='profile_address="'.$address.'",';
				$query.="$flagToAddComma profile_id=profile_id where profile_account_id=".$accountId;							
			
				$res=sql($query);
				if(mysql_affected_rows()>=0) {
					$res=sql("select * from profile where profile_account_id=$accountId limit 1");
					if(mysql_affected_rows()>=0) {
						$outputArray["success"]=true;
						if(isset($outputArray["isNewProfile"])) {
							$outputArray["success_message"]="Congratulation, your profile setup is completed.";
						} else {
							$outputArray["success_message"]="Profile saved.";
						}
						if(mysql_affected_rows()>0) {
							$row=mysql_fetch_assoc($res);
							$outputArray["profile"]=$row;
						}
					} else {
						$outputArray["error_message"]="Error while interacting with data center.";
					}
				} else {
					$outputArray["error_message"]="Error while interacting with data center.";
				}
				
			}
			
		break;
	}
}

end:
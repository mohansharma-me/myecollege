<?php
if(isset($keys[2])) {
	$data=$keys[2];
	switch($data) {
		
		case "users":
			
			$userId=isset($_SESSION["userId"])?$_SESSION["userId"]:null;
			if(isset($userId)) {
				$collegeId=null;
				$res=sql("select account_college_id from account where account_user_id='".$userId."'");
				if(mysql_affected_rows()>0) {
					$tmpRow=mysql_fetch_assoc($res);
					$collegeId=$tmpRow["account_college_id"];
				}
				
				if(isset($collegeId)) {
					$res=sql("select * from (select * from profile, account where profile_account_id=account_id and account_user_id!='".$userId."')a where account_activation_status=1 and account_college_id=$collegeId");
					
					$outputArray["success"]=true;
					if(mysql_affected_rows()>0) {
						$outputArray["users"]=array();
						while($row=mysql_fetch_assoc($res)) {
							$user=array();
							$user["account_id"]=$row["account_id"];
							$user["account_user_id"]=$row["account_user_id"];
							$user["profile_id"]=$row["profile_id"];
							$user["name"]=ucwords($row["profile_first_name"]." ".$row["profile_last_name"]);
							$outputArray["users"][]=$user;
						}
					}
				}
			}
			
		break;
		
		case "notifications":
		
			// get all upadtes pages and combine into one json object and sent it to application
				$myJson=array();
				$data=file("http://localhost/cpu/get-data/messages");
				$tmpJson=json_decode($data[0],true);
				if(isset($tmpJson) && is_array($tmpJson) && isset($tmpJson["chat"])) {
					$myJson["chat"]=$tmpJson["chat"];
				}
				$outputArray["updates"]=$myJson;
				$outputArray["success"]=true;			
				
		break;
		
		case "messages":
		
			$profileId=null;
			$userId=isset($_SESSION["userId"])?$_SESSION["userId"]:null;
			if(isset($userId)) {
				$res=sql('select profile_id from account, profile where profile_account_id=account_id and lower(account_user_id)="'.$userId.'"');
				if(mysql_affected_rows()>0) {
					$row=mysql_fetch_assoc($res);
					$profileId=$row["profile_id"];
				}
			}
		
			// personal messages
			if(isset($profileId)) {
				$whereFrom="";
				$fromId=Input("fromId");
				if(isset($fromId)) {
					$whereFrom="chat_from_id=$fromId and ";
				}
				
				$query="select * from chat where ".$whereFrom."chat_to_id=$profileId and chat_status=".CHAT_STATUS_NEW." and chat_propagation_type=".PROPAGATION_UNICAST;
				
				$res=sql($query);
				
				if(mysql_affected_rows()>0) {
					$outputArray["chat"]=array();
					while($row=mysql_fetch_assoc($res)) {
						$row["chat_timestamp"]=date("d M'y h:m A",strtotime($row["chat_timestamp"])); 
						$outputArray["chat"][]=$row;
					}
					$outputArray["success"]=true;
				} else if(mysql_affected_rows()==0) {
					$outputArray["zero_chat"]=true;
					$outputArray["success"]=true;
				} else {
					$outputArray["error_message"]="Error while interacting data center.";
				}
				
			} else {
				$outputArray["error_message"]="Your login session expired or never created, please try to login again.";
			}
		
		break;
		
		case "profiles":
		
			$profileId=Input("profileId");
			$accountId=Input("accountId");
			$flag=false;
			$sqlQuery="";
			if(isset($profileId)) {
				$flag=true;
				$sqlQuery="select * from profile where profile_id=$profileId limit 1";
			} else if($accountId) {
				$flag=true;
				$sqlQuery="select * from profile where profile_account_id=$accountId limit 1";
			}
			
			if($flag) {
				$res=sql($sqlQuery);
				if(mysql_affected_rows()>=0) {
					if(mysql_affected_rows()==0) {
						$outputArray["noProfile"]=true;
						$outputArray["error_message"]="Sorry no profile were found.";
					} else {
						$row=mysql_fetch_assoc($res);
						$outputArray["profile"]=$row;
						$outputArray["success"]=true;
					}
				} else {
					$outputArray["error_message"]="Error while interacting data center.";
				}
			}
		
		break;
		
		case "timelines":
			$profileId=Input("profileId");
			$collegeId=Input("collegeId");
			$timelineOffset=Input("timelineOffset");
			$timelineCount=Input("timelineCount");
			if(!isset($timelineCount))
				$timelineCount=10;
			//$timelineOffset=0;
			if(isset($timelineOffset)) {
				$timelineOffset--;
				
				if(!isset($collegeId)) $collegeId=0;
				if(!isset($profileId)) $profileId=0;
				
				if($collegeId==0 && $profileId==0 && !isset($_REQUEST["broadcastTimelines"])) {
					$userId=isset($_SESSION["userId"])?$_SESSION["userId"]:null;
					if(isset($userId)) {
						$res=sql('select profile_id from account, profile where profile_account_id=account_id and lower(account_user_id)="'.$userId.'"');
						if(mysql_affected_rows()>0) {
							$row=mysql_fetch_assoc($res);
							$profileId=$row["profile_id"];
						}
					}
				}
				
				if(isset($_REQUEST["broadcastTimelines"])) {
					$timelineOffset=0;
				}
				
				$res=sql("select * from timeline where timeline_college_id=$collegeId and timeline_profile_id=$profileId and ((timeline_updated=1 and timeline_id<=$timelineOffset) or timeline_id>$timelineOffset) order by timeline_id desc");
				if(mysql_affected_rows()>=0) {
					
					if(mysql_affected_rows()>0) {
						$outputArray["timelines"]=array();
						
						while($row=mysql_fetch_assoc($res)) {
							
							if($row["timeline_updated"]==1) {
								sql("update timeline set timeline_updated=0 where timeline_id=".$row["timeline_id"]);
								// make this statement triggered by application
							}
							
							//if($row["timeline_id"]==1)
								//$row["timeline_data"]='{"data_raw":{"timelineText":"This is data which is displayed below the timeline heading.","timelineImage":"'.base64_encode(file_get_contents("city.jpg")).'"},"data_heading":"Timeline Heading 1"}';
							
							$row["timeline_timestamp"]=date("d M'y h:m A",strtotime($row["timeline_timestamp"])); //strtotime($row["timeline_timestamp"])."000";
							$outputArray["timelines"][]=$row;
						}
						
						$outputArray["success"]=true;
					}
					
				} else {
					$outputArray["error_message"]="Unable to interact with data server, please try again.";
				}
				
			}
		
		break;
		
		case "network-parameters":
			$outputArray["network_read_timeout"]=10000;
			$outputArray["network_read_buffer"]=4*1024;
			$outputArray["network_connect_timeout"]=10000;
			$outputArray["success"]=true;
		break;
		
		case "colleges":
			// college data
			$collegeId=Input("collegeId");
			$query="select * from college";
			$jsonFieldName="colleges";
			if(isset($collegeId)) {
				$query.=" where college_id=$collegeId limit 1";
				$jsonFieldName="college";
			}
			$res=sql($query);
			if(mysql_affected_rows()>=0) {
				$outputArray[$jsonFieldName]=array();
				if(mysql_affected_rows()>0) {
					while($row=mysql_fetch_assoc($res)) {
						unset($row["college_administrator_password"]);
						unset($row["college_logo"]);
						
						if($jsonFieldName=="colleges")
							$outputArray[$jsonFieldName][]=$row;
						else if($jsonFieldName=="college")
							$outputArray[$jsonFieldName]=$row;
					}
				}
				$outputArray["success"]=true;
			} else {
				$outputArray["error_message"]="There is problem while interacting with data server, please try again. #1";
			}
			
		break;
		
		case "images":
			$outputArray[$stopKey]=true;
			$imgOf=Input("imgOf");
			$imgId=Input("imgId");
			$imgType=Input("imgType");
			if(!isset($imgType)) $imgType="original";
			$imgQuality=Input("imgQuality");
			if(!isset($imgQuality)) $imgQuality=5;
			$imageData=null;
							
			if(isset($imgOf)) { //imgId
				$flag=true;
				if($imgOf=="profile") {
					$imgOf="account";
					if(!isset($imgId)) {
						$imgId=isset($_SESSION["userId"])?$_SESSION["userId"]:null;
					} else {
						$res=sql("select account_user_id from account, profile where profile_account_id=account_id and profile_id=$imgId");
						if(mysql_affected_rows()>0) {
							$row=mysql_fetch_assoc($res);
							$imgId=$row["account_user_id"];
						}
					}
					
					if(isset($imgId)) {
						$filepath="./".PROFILE_PICTURE_FOLDER."/".getPrivateHash($imgId);
						if(file_exists($filepath) && is_file($filepath)) {
							$flag=false;
							$imageData=file_get_contents($filepath);
						} else {
							$res=sql('select account_id from account where lower(account_user_id)="'.$imgId.'"');
							if(mysql_affected_rows()>0) {
								$row=mysql_fetch_assoc($res);
								$imgId=$row["account_id"];
							} else  {
								$flag=false;
							}
						}
					} else {
						$flag=false;
					}
				}
				
				if($flag) {
				
					$imgData=null;
					$tableName=$imgOf;
					$tablePrimaryColName=$tableName."_id";
					$colName=null;
					
					switch($tableName) {
						case "college":
							$colName="college_logo";
						break;
						
						case "account":
							$colName="account_avtar";
						break;
					}
					
					$res=sql("select $colName from $tableName where $tablePrimaryColName=$imgId");
					if(mysql_affected_rows()>=0) {
						if(mysql_affected_rows()>0) {
							$row=mysql_fetch_assoc($res);
							$imageData=$row[$colName];
						}
					} else {
						$outputArray["error_message"]="There is problem while interacting with data server, please try again. #1";
					}
					
				}

				if($imageData!=null) {
					$outputArray["success"]=true;
					switch($imgType) {
						case "icon":
							$imageData=imageCompressIcon($imageData,$imgQuality);
						break;
						
						case "thumb":
							$imageData=imageCompressThumb($imageData,$imgQuality);
						break;
						
						case "full":
							$imageData=imageCompressFull($imageData,$imgQuality);
						break;
						
						case "poster":
							$imageData=imageCompressFull($imageData,$imgQuality);
						break;
						
						case "original":
							$imageData=imageCompressOriginal($imageData,$imgQuality);
						break;
						
						default:
							$imageData=imageCompressOriginal($imageData,$imgQuality);
					}
					
					header("HTTP/1.0 200 OK");
					header("Content-Type: image/png");
					header("Content-Length: ".strlen($imageData));
					print $imageData;
				} else {
					header("HTTP/1.0 404 Page Not Found");
				}
				
			}
		
		break;
	}
}
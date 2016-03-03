<?php

function sendMail($to,$subject,$html,$from) {
	$headers = "From: $from\r\n";
	$headers .= "Reply-To: $from\r\n";
	$headers .= "MIME-Version: 1.0\r\n";
	$headers .= "Content-Type: text/html; charset=ISO-8859-1\r\n";
	if(mail($to,$subject,$html,$headers)) {
	    return true;
	}
	return false;
}

function decodeBinary($file,$specialCase=false) {
	if($specialCase) {
		$file=str_replace(" ","+",$file);
		$file=str_replace("\n","",$file);
	}
	return base64_decode($file);
}

function encodeBinary($file) {
	return base64_encode($file);
}

function imageCompressIcon($imgData,$q=5) {
	return imageCompress($imgData,ECOLLEGE_IMAGESIZE_ICON,null,null,100,$q);
}

function imageCompressThumb($imgData,$q=5) {
	return imageCompress($imgData,ECOLLEGE_IMAGESIZE_THUMB,null,null,100,$q);
}

function imageCompressFull($imgData,$q=5) {
	return imageCompress($imgData,ECOLLEGE_IMAGESIZE_FULL,null,null,100,$q);
}

function imageCompressOriginal($imgData,$q=5) {
	return imageCompress($imgData,ECOLLEGE_IMAGESIZE_ORIGINAL,null,null,100,$q);
}

function imageCompress($imgData,$autoAdjust=null,$new_height=null,$new_width=null,$resizePerc=100,$quality=9,$saveFilename=null) {
	$data=null;
	if(strlen($imgData)==0) return null;
	$img=imagecreatefromstring($imgData);
	if($img!==false) {
		// resize code
		if($resizePerc!=100 || isset($autoAdjust) || (isset($new_height) && isset($new_width))) {
			list($wid,$hei)=getimagesizefromstring($imgData);
			$o_w=$wid;
			$o_h=$hei;

			if(isset($autoAdjust)) {
				if($wid>$hei) {
					$hei=($hei*$autoAdjust)/$wid;
					$wid=$autoAdjust;
				} else {
					$wid=($wid*$autoAdjust)/$hei;
					$hei=$autoAdjust;
				}
			} else if(isset($new_height) && isset($new_width)) {
				$wid=$new_width;
				$hei=$new_height;
			} else {
				$wid=($o_w*$resizePerc)/100;
				$hei=($o_h*$resizePerc)/100;
			}
			
			$thumb=imagecreatetruecolor($wid, $hei);
			//imagesavealpha($thumb,true);
			imagecopyresized($thumb, $img, 0, 0, 0, 0, $wid, $hei, $o_w, $o_h);
			ob_start();
			imagepng($thumb,$saveFilename,$quality,PNG_ALL_FILTERS);
			$data=ob_get_clean();
			imagedestroy($thumb);
		} else {
			ob_start();
			//imagesavealpha($img,true);
			imagepng($img,$saveFilename,$quality,PNG_ALL_FILTERS);
			$data=ob_get_clean();
		}
		imagedestroy($img);
	} else {
		return false;
	}
	return $data;
}

function getPage($firstKey) {
	$prefix="./";
	switch(strtolower(trim($firstKey))) {
		case "cpu": return $prefix."cpu.php";
		default:
			return $prefix."error.php";
	}
	return null;
}

function getKeys() {
	$k=array();
	$keys=filterInput(INPUT_GET,"__key",false,false,true);
	if(isset($keys) && strlen(trim($keys))!=0) {
		$_k=explode("/",$keys);
		foreach($_k as $i) {
			if(strlen(trim($i))>0)
				$k[]=$i;
		}
	}
	return $k;
}

function slug($text) {
	$text = preg_replace('~[^\\pL\d]+~u', '-', $text);
	$text = trim($text, '-');
	$text = iconv('utf-8', 'us-ascii//TRANSLIT', $text);
	$text = strtolower($text);
	$text = preg_replace('~[^-\w]+~', '', $text);
	if (empty($text))
	{
	return 'n-a';
	}  
	return $text;
}

function getPassword($pass) {
	return getIPHash($pass,"9722505033");
}

function destroySession() {
	session_destroy();
	session_id(uniqid());
	session_start();
}

function getPrivateHash($data) {
	return getIPHash($data,"9722505033");
}

function getIPHash($data=null,$key=null) {
    if($key==null)
	$key=session_id();
    if($data==null) {
        return hash_hmac("sha1", gethostbyname(""), $key);
    } else {
        return hash_hmac("sha1", $data."".gethostbyname(""), $key);
    }
}


function isMobile() {
    if(isset($_SERVER["HTTP_X_WAP_PROFILE"])) { // if mobile friendly
        return true;
    }
    
    if(isset($_SERVER["HTTP_ACCEPT"]) && preg_match("/wap\.|\.wap/i",$_SERVER["HTTP_ACCEPT"])) { // if support wap
        return true;
    }

    if(isset($_SERVER["HTTP_USER_AGENT"])){ // any of following mobile devices
        $user_agents = array("midp", "j2me", "avantg", "docomo", "novarra", "palmos", "palmsource", "240x320", "opwv", "chtml", "pda", "windows\ ce", "mmp\/", "blackberry", "mib\/", "symbian", "wireless", "nokia", "hand", "mobi", "phone", "cdm", "up\.b", "audio", "SIE\-", "SEC\-", "samsung", "HTC", "mot\-", "mitsu", "sagem", "sony", "alcatel", "lg", "erics", "vx", "NEC", "philips", "mmm", "xx", "panasonic", "sharp", "wap", "sch", "rover", "pocket", "benq", "java", "pt", "pg", "vox", "amoi", "bird", "compal", "kg", "voda", "sany", "kdd", "dbt", "sendo", "sgh", "gradi", "jb", "\d\d\di", "moto");
        foreach($user_agents as $user_string){
            if(preg_match("/".$user_string."/i",$_SERVER["HTTP_USER_AGENT"])) {
                return true;
            }
        }
    }
    
    if(isset($_SERVER["HTTP_USER_AGENT"])) {
        if(preg_match("/iphone/i",$_SERVER["HTTP_USER_AGENT"])) { // detect iphone
            return true;
        }
    }

    // if not mobile
    return false;
}

function isEmail($mail) {
    $flag=false;
    $arr=explode("@",$mail);
    if(count($arr)==2) {
        if(strlen(trim($arr[0]))>0 && strlen(trim($arr[1]))>0) {
            $flag=true;
        }
    }
    return $flag;
}

function Input($name,$sqlParam=true,$lowerit=false,$trim=true) {
	return filterInput(INPUT_REQUEST,$name,$sqlParam,$lowerit,$trim);
}

function filterInput($type,$name,$sqlParam=true,$lowerit=true,$trim=true) {
	$data=null;
	if($type==INPUT_REQUEST) {
		$data=isset($_REQUEST[$name])?$_REQUEST[$name]:null;
	} else {
		$data=filter_input($type,$name);
	}
	if(isset($data)) {
		if($lowerit) {
			$data=strtolower($data);
		} 
		if($trim) {
			$data=trim($data);
		}
		if($sqlParam) {
			return addslashes($data);
		} else {
			return $data;
		}
	} else {
		return $data;
	}
}

function sql($query) {
    if($_SERVER["HTTP_HOST"]=="localhost")  {
        $connection=mysql_connect("localhost","root","");
        $database=mysql_select_db("myecollege", $connection);
    } else {
        $connection=mysql_connect("localhost","root","");
        $database=mysql_select_db("myecollege", $connection);
    }
    $result=mysql_query($query);
    if(mysql_affected_rows()>0) {
        return $result;
    } else if(mysql_affected_rows()==0) {
        return 0;
    } else {
        return -1;
    }
}

function formatBytes($bytes, $precision = 2) { 
    $units = array('B', 'KB', 'MB', 'GB', 'TB'); 

    $bytes = max($bytes, 0); 
    $pow = floor(($bytes ? log($bytes) : 0) / log(1024)); 
    $pow = min($pow, count($units) - 1); 

    // Uncomment one of the following alternatives
     $bytes /= pow(1024, $pow);
    // $bytes /= (1 << (10 * $pow)); 

    return round($bytes, $precision) . ' ' . $units[$pow]; 
} 




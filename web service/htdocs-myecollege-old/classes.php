<?php
include_once "conn.php";
function isSigned() {
    if (isset($_SESSION['signed']) && is_bool($_SESSION['signed']) && $_SESSION['signed']==true) {
        if(isset($_SESSION['college']) && is_array($_SESSION['college'])) {
            return true;
        } else {
            return false;
        }
    } else {
        return false;
    }
}

function inputfilter($data) {
    return addslashes(strtolower(trim($data)));
}

function is_email($email) {
	$arr=explode("@",$email);
	if(is_array($arr) && count($arr)==2) {
		$arr=explode(".",$arr[1]);
		if(is_array($arr) && count($arr)==2)
			return true;
	}
	return false;
}

function csv_importfac($csvdata) {
	$fp=fopen("temp_csv.tmp","w+");
	fwrite($fp,$csvdata);
	fclose($fp);
	
	$faculty="";
	$department="";
	$result=mysql_query("select * from faculties");
	while(($row=mysql_fetch_assoc($result))) {
		$faculty[]=$row;
	}
	$result=mysql_query("select * from _departments");
	while(($row=mysql_fetch_assoc($result))) {
		$department[]=$row;
	}
	
	$fp=fopen("temp_csv.tmp","r");
	echo "<tr><td><table style='width:100%' border=1 cellspacing=0>";
	echo "<tr><td>FACULTY NAME</td><td>DEPARTMENT</td><td>PRI. EMAIL</td><td>STATUS</td></tr>";
	
	$added=0;
	$rejected=0;
	
	while(($array=fgetcsv($fp))) {
		$name=$array[0];
		$depart=$array[1];
		$depart_id="";
		$pemail=$array[2];
		$status="UNKNOWN";
		
		$flag=false;
		foreach($department as $dep) {
			if($dep["code"]==$depart) {
				$depart_id=$dep["id"];
				$depart=$dep["full_name"];
				$flag=true;
				break;
			}
		}
		if($flag) {
			$flag=false;
			foreach($faculty as $fac) {
				if(trim(strtolower($fac["primary_email"]))==trim(strtolower($pemail))) {
					$flag=true;
					break;
				}
			}
			if(!$flag) {
			
				mysql_query("insert into faculties(full_name,department_id,primary_email) values('$name','$depart_id','$pemail')");
				if(mysql_affected_rows()==1) {
					$array["primary_email"]=$pemail;
					$faculty[]=$array;
					$status="ADDED";
					$added++;
				} else {
					$status="ERROR";
					$rejected++;
				}
			} else {
				$status="P.EMAIL ALREADY IN.";
				$rejected++;
			}
		} else {
			$status="DEPARTMENT NOT KNOWN.";
			$rejected++;
		}
		
		
		echo "<tr><td>$name</td><td>$depart</td><td>$pemail</td><td>$status</td></tr>";
	}
	echo "</table></td></tr><br/>";
	echo "Added: $added, Rejected: $rejected";
	fclose($fp);
}
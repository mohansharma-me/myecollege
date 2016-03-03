<?php
class College {
	public $id;
	public $gtucode;
	public $common_name;
	public $full_name;
	public $address;
	public $city;
	public $phone;
	public $fax;
	public $email;
	public $website;
	public $college_type;
	public $estd_year;
	public $tution_fees;
	public $logo;
	
	function __construct($array) {
		if(isset($array)) {
			$this->id=$array["id"];
			$this->gtucode=$array["gtucode"];
			$this->common_name=$array["common_name"];
			$this->full_name=$array["full_name"];
			$this->address=$array["address"];
			$this->city=$array["city"];
			$this->phone=$array["phone"];
			$this->fax=$array["fax"];
			$this->email=$array["email"];
			$this->website=$array["website"];
			$this->college_type=$array["college_type"];
			$this->estd_year=$array["estd_year"];
			$this->tution_fees=$array["tution_fees"];
			include_once "..\\funcs.images.php";
			$this->logo=base64_encode(resizeImage3($array["logo"],100,100,true,50));
		}
	}
}
class CollegeList {
	public $college=array();
}
$clglist="";//new CollegeList();
include_once "..\\conn.php";
$result=mysql_query("select * from colleges");
if(mysql_affected_rows()>0)
while(($row=mysql_fetch_assoc($result))) {
	$clglist[]=new College($row);
}
echo json_encode($clglist,JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_QUOT | JSON_HEX_AMP | JSON_UNESCAPED_UNICODE);
?>
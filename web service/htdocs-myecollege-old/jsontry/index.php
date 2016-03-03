<?php
	class UserFile {
		public $userID;
		public $userRealName;
		public $emailAddress;
		public $displayPicture;
		public $isStudent;
	}
	
	$uf=new UserFile();
	$uf->userID="120203107017";
	$uf->userRealName=" Ashu Sharma";
	$uf->emailAddress="ashusharma@gmail.com";
	$uf->isStudent=false;
	
	echo json_encode($uf);

?>
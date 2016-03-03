<?php
$error="";
$error1="";
$btnAddFaculties=filter_input(INPUT_POST,"add_faculties");
$btnImportFaculties=filter_input(INPUT_POST,"import_faculties");
if($btnAddFaculties=="Submit") {
	$full_name=filter_input(INPUT_POST,"full_name");
	$department=filter_input(INPUT_POST,"department");
	$email_address=filter_input(INPUT_POST,"email_address");
	if(empty($full_name)) {
		$error="<font color='red'>* please enter faculty name</font>";
	} else if(empty($department) || !is_numeric($department)) {
		$error="<font color='red'>* please select department</font>";
	} else if(empty($email_address) || !is_email($email_address)) {
		$error="<font color='red'>* please enter valid email address</font>";
	} else {
		$result=mysql_query("select id from faculties where lower(primary_email)='$email_address'");
		if(mysql_affected_rows()==1) {
			$error="<font color='red'>This email address is already registered!!</font>";
		} else {
			mysql_query("insert into faculties(full_name,department_id,primary_email) values('$full_name','$department','$email_address')");
			if(mysql_affected_rows()==1) {
				$error="<font color='green'>Added!</font>";
			} else {
				$error="<font color='red'>Unable to add new record, error!!</font>";
			}
		}
	}
}
?>
<h1 class="title">Add faculties</h1>
<form class="" method="post">
<table style="width:100%" cellspacing="10" border="0">
    <tr>
		<td align="right" style="width:20%">Full name : *</td>
		<td align="left"><input type="text" name="full_name" placeholder="faculty name" /></td>
	</tr>
	<tr>
		<td align="right">Department : *</td>
		<td align="left">
			<select name="department">
				<?php
				$result=mysql_query("select * from _departments");
				while(($row=mysql_fetch_assoc($result))) {
					echo "<option value='".$row["id"]."'>".$row["full_name"]."</option>";
				}
				?>
			</select>
		</td>
	</tr>
	<tr>
		<td align="right">Email address : *</td>
		<td align="left"><input type="text" name="email_address" placeholder="primary email address" /></td>
	</tr>
	
    <tr><td></td><td align="left"> <input type="submit" name="add_faculties" value="Submit" /> <?=$error?></td></tr>
</table>
</form>
<br/><br/>
<h1 class="title">Import faculties</h1>
<form class="" method="post" enctype="multipart/form-data" style='height:300px;overflow:auto'>
<table style="width:100%" cellspacing="10" border="0">
<?php
if($btnImportFaculties=="Submit") {
	if(isset($_FILES["csvfile"])) {
		$name=$_FILES['csvfile']['name'];
		$type=$_FILES['csvfile']['type'];
		$tmpnm=$_FILES['csvfile']['tmp_name'];
		$error=$_FILES['csvfile']['error'];
		$size=$_FILES['csvfile']['size'];
		if($error=="0") {
			if(strcmp("application/vnd.ms-excel",$type)==0) {
				$csvdata=file_get_contents($tmpnm);
				csv_importfac($csvdata);
			} else {
				$error1="<font color='red'>* invalid csv file</font>";
			}
		} else {
			$error1="<font color='red'>* uploading error</font>";
		}
	} else {
		$error1="<font color='red'>* please select csv file</font>";
	}
} else {
?>
<tr>
	<td align="right" style="width:20%">Excel CSV : *</td>
	<td align="left"><input type="file" name="csvfile" /> <input type="submit" name="import_faculties" value="Submit" /> <?=$error1?></td>
</tr>
<tr>
	<td colspan=2>Note :<br/>Upload CSV file that contains faculty data in following column order without any header<br/>First column: full name of faculty (Mr. ABC XYZ),<br/>Second column: department code (07),<br/>Third column: primary email address (mrabcxyz@mailbox.com)</td>
</tr>
<?php 
}
?>	
</table>
</form>

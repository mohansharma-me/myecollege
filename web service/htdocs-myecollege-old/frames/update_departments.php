<?php
$error="";
$btnUpdateDepartments=filter_input(INPUT_POST,"update_departments");
if($btnUpdateDepartments=="Update") {
	if(isset($_POST["chk"])) {
		$codes=$_POST["chk"];
		if(is_array($codes)) {
			mysql_query("delete from departments where gtucode='".$college["gtucode"]."'");
			foreach($codes as $code) {
				mysql_query("insert into departments(gtucode,department_id) values('".$college["gtucode"]."',$code)");
			}
			$error="<font color='green'>Updated</font>";
		}
	}
}
?>
<h1 class="title">Update departments</h1>
<form class="full-width" method="post">
<table style="width:100%" cellspacing="10" border="0">
    <tr>
		<td>
			<?php
			$result=mysql_query("select * from departments where gtucode='".$college["gtucode"]."'");
			$selected="";
			while(($row=mysql_fetch_assoc($result))) {
				$selected[]=$row["department_id"];
			}
			$result=mysql_query("select * from _departments");
			while(($row=mysql_fetch_assoc($result))) {
				$name=$row["full_name"];
				$code=$row["code"];
				$flag=false;
				if(is_array($selected)) {
					foreach($selected as $sel) {
						if($sel===$row["id"])
						{
							$flag=true;
							break;
						}
					}
				}
				if($flag) {
					echo "<input type='checkbox' name='chk[]' value='".$row["id"]."' checked/> $name<br/>";
				} else {
					echo "<input type='checkbox' name='chk[]' value='".$row["id"]."' /> $name<br/>";
				}
			}
			?>
		</td>
	</tr>
    <tr><td> <input type="submit" name="update_departments" value="Update" /> <?=$error?></td></tr>
</table>
</form>
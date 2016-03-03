<?php
$save_message="";
$change_logo_btnsave=filter_input(INPUT_POST,"change_logo_btnsave");
if(isset($change_logo_btnsave)) {
    if(isset($_FILES['new_logo'])) {
        $name=$_FILES['new_logo']['name'];
        $type=$_FILES['new_logo']['type'];
        $tmpnm=$_FILES['new_logo']['tmp_name'];
        $error=$_FILES['new_logo']['error'];
        $size=$_FILES['new_logo']['size'];
        $maxsize=100*1024;
        if($error=="0") {
            if(substr($type, 0,5)=="image") {
                if($size<=$maxsize) {
                     $data=file_get_contents($tmpnm);
                     $res=mysql_query("update colleges set logo='".mysql_escape_string($data)."' where gtucode='".$college['gtucode']."'");
                     if(mysql_affected_rows()==1) {
                         $save_message="<font color='green'>Saved.</font>";
                     } else {
                         $save_message="<font color='red'>Not saved, errors.</font>";
                     }
                } else {
                    $save_message="<font color='red'>Not saved, max size is 100Kb.</font>";
                }
            } else {
                $save_message="<font color='red'>Not saved, invalid image.</font>";
            }
        } else {
            $save_message="<font color='red'>Not saved, error.</font>";
        }
    }
}
?>
<h1 class="title">Change logo - <?=$college['full_name']?></h1>
<form class="full-width" method="post" enctype="multipart/form-data">
<table style="width:100%" cellspacing="5">
    <tr><td style="width:15%"></td><td style="width:80%"></td></tr>
    <tr>
        <td align="right">Current logo : </td><td><img src="api_getcollegelogo.php?code=<?=$college['gtucode']?>" style="width:320px" /></td>
    </tr>
    <tr>
        <td align="right">New logo : </td><td><input type="file" name="new_logo" /></td>
    </tr>
    <tr>
        <td align="right">Note : </td><td>Max image size: 100 Kb</td>
    </tr>
    <tr>
        <td align="right"></td><td><input type="submit" name="change_logo_btnsave" value="Save" /> <?=$save_message?></td>
    </tr>
</table>
</form>
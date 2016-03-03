<?php
$save_message="";
$change_password_btnsave=filter_input(INPUT_POST,"change_password_btnsave");
if(isset($change_password_btnsave)) {
    $opassword=filter_input(INPUT_POST,"opassword");
    $npassword=filter_input(INPUT_POST,"npassword");
    $res=mysql_query("select * from colleges where gtucode='".$college['gtucode']."'");
    if(mysql_affected_rows()==1) {
        $row=mysql_fetch_assoc($res);
        if(inputfilter($row["password"])==inputfilter($opassword)) {
            mysql_query("update colleges set password='".inputfilter($npassword)."' where gtucode='".$college['gtucode']."'");
            if(mysql_affected_rows()==1) {
                $save_message="<font color='green'>Saved.</font>";
            } else {
                $save_message="<font color='red'>Not saved, error.</font>";
            }
        } else {
            $save_message="<font color='red'>Password not matched.</font>";
        }
    } else {
        $save_message="<font color='red'>Not saved, error.</font>";
    }
}
?>
<h1 class="title">Change password</h1>
<form class="full-width" method="post">
<table style="width:100%" cellspacing="5">
    <tr><td style="width:15%"></td><td style="width:80%"></td></tr>
    <tr>
        <td align="right">Old password : </td><td><input type="password" name="opassword" /></td>
    </tr>
    <tr>
        <td align="right">New password : </td><td><input type="password" name="npassword" /></td>
    </tr>
    <tr>
        <td align="right"></td><td><input type="submit" name="change_password_btnsave" value="Save" /> <?=$save_message?></td>
    </tr>
</table>
</form>
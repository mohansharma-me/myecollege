<?php 
$save_message="";
$edit_profile_btnsave=filter_input(INPUT_POST, "edit_profile_btnsave");
if(isset($edit_profile_btnsave) && $edit_profile_btnsave=="Save") {
   $fullname=filter_input(INPUT_POST,"fullname");
   $common_name=filter_input(INPUT_POST,"common_name");
   $address=filter_input(INPUT_POST,"address");
   $city=filter_input(INPUT_POST,"city");
   $phone=filter_input(INPUT_POST,"phone");
   $fax=filter_input(INPUT_POST,"fax");
   $email=filter_input(INPUT_POST,"email");
   $website=filter_input(INPUT_POST,"website");
   $college_type=filter_input(INPUT_POST,"college_type");
   $estd_year=filter_input(INPUT_POST,"estd_year");
   $tution_fees=filter_input(INPUT_POST,"tution_fees");
   
   if(isset($fullname) && isset($common_name) && isset($address) && isset($city) && isset($phone) && isset($fax) && isset($email) && isset($website) && isset($college_type) && isset($estd_year) && isset($tution_fees)) {
       $res=mysql_query("update colleges set full_name='$fullname', common_name='$common_name', address='$address', city='$city', phone='$phone', fax='$fax', email='$email', website='$website', college_type='$college_type', estd_year='$estd_year', tution_fees='$tution_fees' where gtucode='".$college['gtucode']."'");
       if(mysql_affected_rows()==1) {
           $res=mysql_query("select * from colleges where gtucode='".$college['gtucode']."'");
           $college=mysql_fetch_array($res);
           $_SESSION['college']=$college;
           $save_message="<font color='green'>Saved</font>";
       } else {
           $save_message="<font color='red'>Not saved.</font>";
       }
   }
}
?>
<h1 class="title">Edit college profile - <?=$college['full_name']?></h1>
<form class="full-width" method="post">
<table style="width:100%" cellspacing="5">
    <tr><td style="width:15%"></td><td style="width:80%"></td></tr>
    <tr>
        <td align="right">College name : </td><td><input type="text" name="fullname" value="<?=$college['full_name']?>" /></td>
    </tr>
    <tr>
        <td align="right">Common name : </td><td><input type="text" name="common_name" value="<?=$college['common_name']?>" /></td>
    </tr>
    <tr>
        <td align="right">GTU Code : </td><td><?=$college['gtucode']?></td>
    </tr>
    <tr>
        <td align="right">Address : </td><td><input type="text" name="address" value="<?=$college['address']?>"/></td>
    </tr>
    <tr>
        <td align="right">City : </td><td><input type="text" name="city" value="<?=$college['city']?>"/></td>
    </tr>
    <tr>
        <td align="right">Phone : </td><td><input type="text" name="phone" value="<?=$college['phone']?>"/></td>
    </tr>
    <tr>
        <td align="right">Fax : </td><td><input type="text" name="fax" value="<?=$college['fax']?>"/></td>
    </tr>
    <tr>
        <td align="right">Email : </td><td><input type="text" name="email" value="<?=$college['email']?>"/></td>
    </tr>
    <tr>
        <td align="right">Website : </td><td><input type="text" name="website" value="<?=$college['website']?>" /></td>
    </tr>
    <tr>
        <td align="right">College type : </td><td><input type="text" name="college_type" value="<?=$college['college_type']?>" /></td>
    </tr>
    <tr>
        <td align="right">Estd. year : </td><td><input type="text" name="estd_year" value="<?=$college['estd_year']?>"/></td>
    </tr>
    <tr>
        <td align="right">Tution fees : </td><td><input type="text" name="tution_fees" value="<?=$college['tution_fees']?>"/></td>
    </tr>
    <tr>
        <td align="right"></td><td><input type="submit" name="edit_profile_btnsave" value="Save" /> <?=$save_message?></td>
    </tr>
</table>
</form>
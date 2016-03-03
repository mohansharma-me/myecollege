<h1 class="title"><?=$college['college_full_name']?></h1>
<table style="width:100%" cellspacing="5">
    <tr><td style="width:15%"></td><td style="width:80%"></td></tr>
    <tr>
        <td align="right">Common name : </td><td><?=$college['college_short_name']?></td>
    </tr>
    <tr>
        <td align="right">GTU Code : </td><td>020</td>
    </tr>
    <tr>
        <td align="right">Address : </td><td></td>
    </tr>
    <tr>
        <td align="right">City : </td><td><?=$college['college_city']?></td>
    </tr>
    <tr>
        <td align="right">Phone : </td><td><?=$college['college_telephone_number']?></td>
    </tr>
    <tr>
        <td align="right">Fax : </td><td><?=$college['college_fax_number']?></td>
    </tr>
    <tr>
        <td align="right">Email : </td><td><?=$college['college_email_address']?></td>
    </tr>
    <tr>
        <td align="right">Website : </td><td><a target="_blank" href="http://<?=$college['college_website']?>"><?=$college['college_website']?></a></td>
    </tr>
    <tr>
        <td align="right">College type : </td><td></td>
    </tr>
    <tr>
        <td align="right">Estd. year : </td><td>2005</td>
    </tr>
    <tr>
        <td align="right">Tution fees : </td><td>Rs. 2,200/-</td>
    </tr>
    <tr>
        <td align="right"></td><td><form action="dashboard.php"><input type="hidden" name="page" value="edit_profile" /><input type="submit" value="Update" /></form></td>
    </tr>
</table>
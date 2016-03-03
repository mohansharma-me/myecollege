<?php
include_once "./classes.php";
$code=  filter_input(INPUT_GET, "code");
$error="ERROR";
if(isset($code) && is_numeric($code)) {
    $code=inputfilter($code);
    $res=  mysql_query("select logo from colleges where gtucode='$code'");
    if(mysql_affected_rows()==1) {
        $row=mysql_fetch_row($res);
        if(is_array($row) && isset($row[0])) {
            $error="";
            header("Cache-Control: no-cache, must-revalidate");
            header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
            header("Content-Disposition: attachment; filename=" . basename("$code"));
            header("Content-Type: image/jpeg");
            echo $row[0];
        }
    }
}
echo $error;
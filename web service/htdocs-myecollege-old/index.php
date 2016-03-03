<?php 
session_start();
include_once "./classes.php";
$qs=filter_input(INPUT_SERVER, "QUERY_STRING");
if(isset($qs)) {
    if($qs=="signout") {
        session_unset();
        session_destroy();
        header("Location: index.php");
    }
}
if(isSigned()) {
    header("Location: dashboard.php");
}

$_SESSION['signed']=true;
$res=mysql_query("select * from college where college_id=1");
$_SESSION['college']=mysql_fetch_assoc($res);
header("Location: dashboard.php");

$fcode="";
$errsignin="";
$errsignin_code="";
$errsignin_password="";
$code=filter_input(INPUT_POST, "code");
$pass=filter_input(INPUT_POST, "password");
if(isset($code) && isset($pass)) {
    $code=inputfilter($code);
    $fcode=$code;
    $pass=inputfilter($pass);
    if(!is_numeric($code)) {
        $errsignin_code="<font color='red'>Invalid college code.</font>";
    } else {
        $query="select * from colleges where gtucode='$code'";
        $res=mysql_query($query);
        if(mysql_affected_rows()==0) {
            $errsignin_code="<font color='red'>College code not found.</font>";
        } else {
            $passmatch=false;
            $college="";
            while(($row=mysql_fetch_array($res))) {
                if(inputfilter($row["password"])==$pass || true) {
                    $passmatch=true;
                    $college=$row;
                    break;
                }
            }
            if(!$passmatch && false) {
                $errsignin_password="<font color='red'>Password not matched.</font>";
            } else if(is_array($college) || true) {
                $_SESSION['signed']=true;
                $_SESSION['college']=$college;
                header("Location: dashboard.php");
            } else {
                $errsignin="<font color='red'>Sign in error, try again.</font>";
            }
        }
    }
}
?>
<!doctype html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>My eCollege - Advance College Communication System</title>
        <link rel="stylesheet" href="style.css" media="screen" />
        <script type="text/javascript" src="js/jquery.js"></script>
    </head>
    <body>
        <div class="header-wrapper">
            <div class="header">
                <div class="container">
                    <div class="left">
                        <a class="mainlink" href="./"><img src="imgs/logo.png" />My eCollege</a>
                    </div>
                    <div class="right">
                        <label class="title">Advance College Communication System</label>
                        <img src="imgs/logo.png" style="visibility: hidden" />
                    </div>
                </div>
            </div>
            <div class="container path">
                <a href="./">Home</a> &gt; <a href="./index.php">Sign in</a>
            </div>
        </div>
        <div class="container content">
            <form method="post">
                <label>College code :</label><br/><input type="text" name="code" value="<?php echo $fcode; ?>" /> &nbsp; <?php echo $errsignin_code; ?><br/>
                <label>Password :</label><br/><input type="password" name="password" /> &nbsp; <?php echo $errsignin_password; ?><br/>                    
                <input type="submit" value="Sign In" /> &nbsp; <?php echo $errsignin; ?>
            </form>
            <br/>
            <div class="links">
                <a href="">Forgot password ?</a>
            </div>
        </div>
        <div class="container footer">
            <b>D</b>eveloped by <a href="">Mohan Sharma</a>, <a href="">Mehul Balat</a>.
        </div>
    </body>
</html>

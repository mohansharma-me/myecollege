<?php
if(isset($_POST['subjs'])) {
	echo "<pre>";
	print_r($_POST['subjs']);
	echo "</pre>";
}
?>
<form method="post">
<?php
	$subjs=$_GET['no_pages'];
	for($i=0;$i<$subjs;$i++) {
		echo "<input type='text' name='subjs[]' value='' /><br/>";
	}
?>
<input type="submit" />
</form>
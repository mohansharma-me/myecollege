<?php
function resizeImage($input,$output,$wid,$hei,$auto=false,$quality=80)  {
	/*
	 * PHP GD
	 * resize an image using GD library
	 */

	// File and new size
	//the original image has 800x600
	$filename = $input;
	//the resize will be a percent of the original size
	$percent = 0.5;

	// Get new sizes
	list($width, $height) = getimagesize($filename);
	$newwidth = $wid;//$width * $percent;
	$newheight = $hei;//$height * $percent;
	if($auto) {
		if($width>$height) {
			$newheight=$wid*0.75;
		} else if($height>$width) {
			$newwidth=$hei*0.75;
		}
	}

	// Load
	$thumb = imagecreatetruecolor($newwidth, $newheight);
	$source = imagecreatefromjpeg($filename);

	// Resize
	imagecopyresized($thumb, $source, 0, 0, 0, 0, $newwidth, $newheight, $width, $height);

	// Output and free memory
	//the resized image will be 400x300
	imagejpeg($thumb,$output,$quality);
	imagedestroy($thumb);
}
function resizeImage2($input,$wid,$hei,$auto=false,$quality=80)  {
	$filename = $input;
	$percent = 0.5;

	list($width, $height) = getimagesize($filename);
	$newwidth = $wid;//$width * $percent;
	$newheight = $hei;//$height * $percent;
	if($auto) {
		if($width>$height) {
			$newheight=$wid*0.75;
		} else if($height>$width) {
			$newwidth=$hei*0.75;
		}
	}

	$thumb = imagecreatetruecolor($newwidth, $newheight);
	$source = imagecreatefromjpeg($filename);
	
	imagecopyresized($thumb, $source, 0, 0, 0, 0, $newwidth, $newheight, $width, $height);

	ob_start();
	imagejpeg($thumb,NULL,$quality);
	$imgdata=ob_get_contents();
	ob_end_clean();
	imagedestroy($thumb);
	return $imgdata;
}
function resizeImage3($input,$wid,$hei,$auto=false,$quality=80) {
	$filename = $input;
	$percent = 0.5;

	list($width, $height) = getimagesizefromstring($filename);
	
	$newwidth = $wid;//$width * $percent;
	$newheight = $hei;//$height * $percent;
	if($auto) {
		if($width>$height) {
			$newheight=$wid*0.75;
		} else if($height>$width) {
			$newwidth=$hei*0.75;
		}
	}

	$thumb = imagecreatetruecolor($newwidth, $newheight);
	$source = imagecreatefromstring($filename);
	
	imagecopyresized($thumb, $source, 0, 0, 0, 0, $newwidth, $newheight, $width, $height);

	ob_start();
	imagejpeg($thumb,NULL,$quality);
	$imgdata=ob_get_contents();
	ob_end_clean();
	imagedestroy($thumb);
	return $imgdata;
}
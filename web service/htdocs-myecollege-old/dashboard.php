<?php 
session_start();
include_once "./classes.php";
if(!isSigned()) {
    header("Location: index.php");
}
$college=$_SESSION['college'];
?>
<!doctype html>
<html>
    <head>
        <meta charset="UTF-8">
        <title><?=$college['college_full_name']." - "?>My eCollege</title>
        <link rel="stylesheet" href="style.css" media="screen" />
        <script type="text/javascript" src="js/jquery.js"></script>
        <script>
        $(document).ready(function() {
            $(".content .navigation ul li").click(function() {
                var _is=$(this).attr("is");
                var img=$(this).find("a img");
                
                $(".content .navigation ul li").removeClass("current");
                if(_is==="yes") {
                    $(this).find("ul").slideUp(300);
                    $(this).attr("is","no");
                    img.attr("src","imgs/expand.png");
                } else {
                    $(this).find("ul").slideDown(300);
                    $(this).attr("is","yes");
                    $(this).addClass("current");
                    img.attr("src","imgs/collapse.png");
                }
                return false;
            });
            $(".content .navigation ul li ul li").click(function() {
                var href=$(this).find("a").attr("href");
                document.location=href;
                return true;
            });
            
        });
        </script>
    </head>
    <body>
        <div class="header-wrapper">
            <div class="header">
                <div class="container">
                    <div class="left">
                        <a class="mainlink" href="./"><img src="/imgs/logo.png" /><?=$college['college_short_name']?></a>
                    </div>
                    <div class="right">
                        <label class="title"><a href="./index.php?signout">Sign Out</a></label>
                        <img src="imgs/logo.png" style="visibility: hidden" />
                    </div>
                </div>
            </div>
            <div class="container path">
                <a href="dashboard.php"><?=$college['college_short_name']?></a> &gt;
            </div>
        </div>
        <div class="container content">
            <div class="left left_sidebar">
                <div class="navigation">
                    <ul>
                        <li><a href=""><?=$college['college_short_name']?>  <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=edit_profile">College profile</a></li>
                                <li><a href="dashboard.php?page=change_logo">Change logo</a></li>
                                <li><a href="dashboard.php?page=change_password">Change password</a></li>
                            </ul>
                        </li>
                        <li><a href="">Streams <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=update_streams">Update streams</a></li>
                                <li><a href="dashboard.php?page=update_departments">Update departments</a></li>
                            </ul>
                        </li>
                        <li><a href="">Faculties <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=add_faculties">Add faculties</a></li>
                                <li><a href="dashboard.php?page=manage_faculties">Manage faculties</a></li>
                            </ul>
                        </li>
                        <li><a href="">Students <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=add_students">Add students</a></li>
                                <li><a href="dashboard.php?page=modify_students">Modify students</a></li>
                                <li><a href="dashboard.php?page=students_import">Import from CSV</a></li>
                                <li><a href="dashboard.php?page=students_export">Export to CSV</a></li>
                            </ul>
                        </li>
                        <li><a href="">Subjects <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=new_subject">New subject</a></li>
                                <li><a href="dashboard.php?page=update_subjects">Update subjects</a></li>
                                <li><a href="dashboard.php?page=subjects_import">Import from CSV</a></li>
                                <li><a href="dashboard.php?page=subjects_export">Export to CSV</a></li>
                            </ul>
                        </li>
                        <li><a href="">Results <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=new_result">New result</a></li>
                                <li><a href="dashboard.php?page=update_result">Update result</a></li>
                                <li><a href="dashboard.php?page=publish_result">Publish result</a></li>
                                <li><a href="dashboard.php?page=backup_results">Backup results</a></li>
                            </ul>
                        </li>
                        <li><a href="">Attendance <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=update_attendance">Update attedances</a></li>
                                <li><a href="dashboard.php?page=attendance_import">Import from CSV</a></li>
                                <li><a href="dashboard.php?page=attendance_export">Export to CSV</a></li>
                            </ul>
                        </li>
                        <li><a href="">Groups <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=add_group">Add group</a></li>
                                <li><a href="dashboard.php?page=edit_group">Edit group</a></li>
                                <li><a href="dashboard.php?page=view_groups">View groups</a></li>
                                <li><a href="dashboard.php?page=new_post">New Post</a></li>
                                
                            </ul>
                        </li>
                        <li><a href="">Activities <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=new_activity">New activity</a></li>
                                <li><a href="dashboard.php?page=update_activity">Update activity</a></li>
                                <li><a href="dashboard.php?page=view_activity">View activity</a></li>
                            </ul>
                        </li>
                        <li><a href="">Time tables <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=add_timetable">Add time table</a></li>
                                <li><a href="dashboard.php?page=update_timetable">Update time table</a></li>
                                <li><a href="dashboard.php?page=delete_timetable">Delete time table</a></li>
                            </ul>
                        </li>
                        <li><a href="">Notice board <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=add_notice">Add notice</a></li>
                                <li><a href="dashboard.php?page=update_notice">Update notice</a></li>
                                <li><a href="dashboard.php?page=delete_notice">Delete notice</a></li>
                            </ul>
                        </li>
                        <li><a href="">Messages <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=view_messages">View messages</a></li>
                                <li><a href="dashboard.php?page=new_messages">New message</a></li>
                            </ul>
                        </li>
                        <li><a href="">Placements <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=update_placement_data">Update placement data</a></li>
                                <li><a href="dashboard.php?page=placement_activity">Placement activity</a></li>
                            </ul>
                        </li>                        
                        <li><a href="">Permissions <img src="imgs/expand.png" /></a>
                            <ul>
                                <li><a href="dashboard.php?page=block_student">Block student</a></li>
                                <li><a href="dashboard.php?page=block_faculty">Block faculty</a></li>
                                <li><a href="dashboard.php?page=block_group">Block group</a></li>
                                <li><a href="dashboard.php?page=faculty_permissions">Faculty permissions</a></li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="left left_frame">
                <?php 
                $page=filter_input(INPUT_GET, "page");
                if(isset($page)) {
                    $file="frames/".$page.".php";
                    if(file_exists($file) && is_file($file)) {
                        include_once $file;
                    } else {
                        echo "Page not found";
                    }
                } else {
                    include_once "frames/collegeprofile.php";
                }
                ?>
            </div>
        </div>
        <div class="container footer">
            <b>D</b>eveloped by <a href="">Mohan Sharma</a>, <a href="">Mehul Balat</a>.
        </div>
    </body>
</html>

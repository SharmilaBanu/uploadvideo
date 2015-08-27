<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style1.css" rel="stylesheet" type="text/css" />
<script src="scripts/script.js"></script>
<script type="text/javascript"
	src="http://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		$('body').on('click', '.fileButton', function() {
			
	//		alert('Parameter: ' + this.id);
			var path1 = this.id;
			$.ajax({
				url : "/metadata",
				type : "get",
				datatype : "text",
				contentType : "application/json; charset=utf-8",
				data : {
					path : path1
				},
				
				
				success : function(data) {
					var link="<a id='l' href="+data+">preview</a>";
		//			alert(link);
					$('#preview').html(link);
				},
				
				error : function() {
					alert("failure man really");
				}
			});
		});
		$('body').on('click','#l',function(event){
			event.preventDefault();
			window.open($(this).attr("href"), "_blank");
		});
	});
</script>
<title>Main dropbox</title>
</head>
<body>
	<p>abcd</p>
	<div id="page-wrapper">
		<div id="heading">welcome ${name} to dropbox application</div>
		<div id="description">all dropbox action you can perform here</div>

		<div id="app">

			<div id="app-wrapper">
				<input type="button" value="Files" onclick="fun1();">
				<div id="files"></div>
				<div id="preview"></div>
			</div>

		</div>

	</div>

</body>
</html>
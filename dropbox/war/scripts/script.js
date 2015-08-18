function fun1() {
	$(document)
			.ready(
					function() {
						$
								.ajax({
									url : "/filesList",
									type : "get",
									datatype : "json",
									contentType : "application/json; charset=utf-8",
									success : function(data) {
							//			alert("success " + data);
										var response = $.parseJSON(data);
										var table = "<table><td><strong>FileName</strong></td>";
										jQuery
												.each(
														$(response),
														function(index, val) {
															table += "<tr><td><input type='button' class='fileButton' name='"
																	+ val
																	+ "'id='"
																	+ val
																	+ "'value='"
																	+ val
																	+ "'/></td></tr>";
														});
										table += "</table>";
										$('#files').html(table);

									},
									error : function() {
										alert("failure man");
									}
								});
					});
}
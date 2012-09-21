ShoutApp = new function() {
var tstatus =0;
	this.authPopUp = function() {
	
		ShoutAppUtil.makeRequest("/shout/twitter.jag", "", function(html) {
			var windowName = 'Twitter Auth'; 
			var popUp = window.open(html, windowName, 'width=1000, height=700, left=24, top=24, scrollbars, resizable');
			if (popUp == null || typeof(popUp)=='undefined') { 	
				alert('Please disable your pop-up blocker and click again.'); 
			} 
			else { 	
				popUp.focus();
				console.log("Called for popUp.focus PopUp");
			}
			$('#key').show();
			console.log("Called $('#key').show() Auth PopUp");
		});
		$.fancybox.close();
		console.log("Called for Auth PopUp");
		
	}

	this.sendVerificationCode = function() {
		if($('#authKey').val()==""){
			alert("Please enter the Verification Code")
		}else{
		ShoutAppUtil.makeRequest("/shout/twitter.jag?atoken="
				+ $('#authKey').val(), "", function(html) {
			$('#key').hide();
			ShoutApp.readXML(html);
			tstatus = 1; 
		});
		
	}
}
this.statusOfTwitter= function() {
return tstatus;
}
	this.readXML = function(xml) {
		$("#feed_scroll").html("");
		$(xml)
				.find("status")
				.each(
						function() {
							var imgUrl = "http"
									+ $(this).find("profile_image_url").text()
											.split('http')[1];
							$("#feed_scroll")
									.append(
											"<div class=\"feed_entry html\"><img src=\""
													+ imgUrl
													+ "\" class=\"user_thumb\"/>"
													+ "<span class=\"status\"><a href=\"#\" class=\"author\">"
													+ $(this).find("name")
															.text() +" ||"
													+ "</a> said <span class=\"status_content\">"
													+ "\""
													+ $(this).find("text")
															.text()
													+ "\" </span></span>"
													+ "</span>"
													+ "<span class=\"status_details\"><a href=\"#\"><img src=\"images/twitter_tiny.png\" alt=\"\" />"
													+ "</span>" + "</div>");
										tstatus = 2; 			
						});

	}

	this.postIt = function() {
		$("#feed_scroll").html("");
		if($('#shout_txt').val()==""){alert("Please type what to be share before click Shout.")}else{
		ShoutAppUtil.makeRequest("/shout/twitter.jag?share="
				+ $('#shout_txt').val(), "", function(html) {
			$("#feed_scroll").html("loading..");
			setInterval(ShoutApp.loadBack(),3000);
		});
		
		
		}
	}
	
	this.loadBack = function() {
		console.log("Loading back Time line");
		ShoutAppUtil.makeRequest("/shout/twitter.jag?loadback=load", "",
				function(html) {
					ShoutApp.readXML(html);
				});
	}
}

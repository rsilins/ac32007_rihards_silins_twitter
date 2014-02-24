$(function(){
	
	adjustFeedHeight(); // adjust feed container height according to sidebar
	attachDeleteOnClick(); // enable tweet delete buttons/links 
	updateStream();
	
	// init feed updates for my feed only
	if ( 	$("#viewing_user_feed").val() == "" && $("#viewing_specific_tweet").val() == "" ||
			$("#viewing_user_feed").val() == "null" && $("#viewing_specific_tweet").val() == "null"	) {
		setInterval(
			function(){ updateStream();},
			3000
		);
	}
	
	// enable spout button
	$(".easy-form-spout-btn").click(function(event){
		
		if ( $("#viewing_user_feed").val() != $("#logged_in_user").val() ) {
			event.preventDefault();
		} else {
			return;
		}
		
		var spout = $(".easy-form-spout-textarea").val();
		spout = $.trim(spout);

		var data = {
			spout: spout
        };

        var error = false;

        $(".easy-form-spout").removeClass("has-error");
        $(".easy-form-spout").removeClass("has-warning");
        $(".easy-form-spout").removeClass("has-success");
        $(this).hide(); 
        $(".easy-form-spout-loader").show();
        
		if ( spout == null || spout.length == 0 || spout.length > 140 ) {
			error = true;
		}

        if ( error ) {
        	$(".easy-form-spout").addClass("has-error");
        	$(this).show();
        	$(".easy-form-spout-loader").hide();
            return true;
        }

        $.ajax({
            type: "PUT",
            url: "/test1/Tweet/",
            data: data,
            cache: false,
            dataType: "html",
            success: function(data){
                //if(data === "true"){ 
                	$(".easy-form-spout").addClass("has-success");
                	$(".easy-form-spout-textarea").val("");
                	$(".easy-form-spout-btn").show();
                	$(".easy-form-spout-loader").hide();
                //}
            },
            error: function( jqXHR, textStatus, errorThrown) {
            	$(".easy-form-spout").addClass("has-warning");
            	$(".easy-form-spout-btn").show();
            	$(".easy-form-spout-loader").hide();
            }
        });
	});
	
	function adjustFeedHeight() {
		var h = $(".sidebar").height();
		h = h * 1.2;
		$(".tweet-list-continer").css({
			"height":+"px",
			"overflow":"auto"
		});
	}

	function parseTwitterJSON(twitter_json) {
		var html = "";
    	
    	for(var i=0; i<twitter_json.length; i++) {
    		
    		html += "<div class=\"panel panel-default\" data-timeuuid=\""+twitter_json[i].uuid+"\">"
						+"<div class=\"panel-body\">"
							+"<h4><a href=\"/test1/Tweet/"+twitter_json[i].uuid+"\" >"+twitter_json[i].message+"</a></h4>"
							+"<span class=\"help-block\">"
								+"by <span class=\"user\">"
									+"<a href=\"/test1/Tweet/"+twitter_json[i].user+"\">"
										+twitter_json[i].user
									+"</a>"
								+"</span> ~ "
								+"<span class=\"tweetTime\" data-tweet-time=\""+twitter_json[i].tweet_time_js_readable+"\">" +
									+""+twitter_json[i].tweet_time+""
								+"</span>&nbsp;";
    		
    		if ( twitter_json[i].user == $('#logged_in_user').val() ) {
    			html += "<form method=\"delete\" action=\"/test1/Tweet/delete\" class=\"delete-tweet-form\">"
    						+"<input type=\"hidden\" value=\""+twitter_json[i].uuid+"\" name=\"tweet_uuid\" class=\"tweet_uuid\" />"
    						+"<input type=\"submit\" value=\"delete\" class=\"delete-tweet-btn\" />"
    					+"</form>";
    		}
    		
							
    				html += "</span>"
						+"</div>"
					+"</div>";
    		if ( i == 0 ) {
    			tweet_last = twitter_json[i].uuid;
    		}
    	}
    	
    	$(".tweet-list-continer").prepend(html);
	}
	
	function deleteTweet(tweet_uuid) {
		
		var data = {
			"ajax":"true",
			"tweet_uuid":tweet_uuid
		};

        $.ajax({
            type: "DELETE",
            url: "/test1/Tweet/delete",
            data: data,
            cache: false,
            dataType: "html",
            success: function(data){
            	refreshStream();
            },
            error: function( jqXHR, textStatus, errorThrown) {
            }
        });
		
	}	
	
	function refreshStream() {	
		$(".tweet-list-continer").html("");
		updateStream();
		attachDeleteOnClick();
	}
	
	function attachDeleteOnClick() {
		$(".delete-tweet-btn").off();
		$(".delete-tweet-btn").on("click",function(event){
			event.preventDefault();
			var tweet_uuid = $(this).parent().children(".tweet_uuid").val();
			deleteTweet(tweet_uuid);
		});
	}
	
	function updateStream() {
		
		var from = "889c8c70-9ced-11e3-a562-a1c274363583";
		var p_from = $('.tweet-list-continer').children("div").first().attr("data-timeuuid");
		//console.log(p_from);
		if ( p_from != null && p_from != "" ) {
			//console.log("what but?");
			from = p_from;
		}
		
		var data = {
			"api":"true",
			"from":from
		};

        $.ajax({
            type: "GET",
            url: "/test1/Tweet/",
            data: data,
            cache: false,
            dataType: "json",
            success: function(data){
            	parseTwitterJSON(data);
            	$(".tweetTime").each(function(){
            		$(this).html(updateDateFormat($(this).attr("data-tweet-time")));
            	});
            	attachDeleteOnClick();
            },
            error: function( jqXHR, textStatus, errorThrown) {
            }
        });
		
	}
	
	function updateDateFormat(tweet_time){
		var now = new Date();
		var tweet_time_arr = tweet_time.split("-");
		date_tweet_time = new Date(
			tweet_time_arr[0],
			(tweet_time_arr[1]-1),
			tweet_time_arr[2],
			tweet_time_arr[3],
			tweet_time_arr[4],
			tweet_time_arr[5]
		);
		var difference = now.valueOf() - date_tweet_time.valueOf();
		var difference_in_seconds = parseInt(difference / 1000);

		if ( difference_in_seconds < 60 ) {
			return difference_in_seconds+" seconds ago";
		} else if ( difference_in_seconds < (60 * 60) ) {
			return parseInt(difference_in_seconds / 60)+" minutes ago";
		} else if ( difference_in_seconds < (60 * 60 * 12) ) {
			return parseInt(difference_in_seconds / (60 * 60))+" hours ago";
		} else {
			return date_tweet_time.format("HH:MM mmm dd");
		}
	}
});
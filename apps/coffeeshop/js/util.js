CoffeShopAppUtil = new function() {
    this.makeRequest = function(type, u, d, callback) {
    	  $.ajax({
            type: type,
            url: u,
            data: d,
            dataType: "json",
            success: callback
        });
        var requestOut = "Request URL: "+ window.location.host +""+ u+" \n"+"Request Method: "+type;
        if(d !=null){
        	requestOut += "\nRequest Data: "+d;        	
        }
        $("#request-textarea").val(requestOut);
    };
    

    this.makeJsonRequest = function(type, u, d, callback) {
        $.ajax({
            type: type,
            url: u,
            data: d,
            dataType: "json",
            success: callback
        });
    };
}



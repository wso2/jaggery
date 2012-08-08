CoffeeShopClient = new function() {

this.addOrder = function(orderName) {
CoffeShopAppUtil.makeRequest("POST","/coffeeshop/orders/", "order="+orderName,
function(html) {
$("#response-textarea").val(JSON.stringify(html));
CoffeeShopClient.viewOrders();
});
}

this.viewOrder = function(orderid) {
CoffeShopAppUtil.makeRequest("GET","/coffeeshop/orders/"+orderid+"", null ,
function(html) {
$("#response-textarea").val('JSON.stringify(html');
$("#request-textarea").val(orderid);
});

}

var initStatus=true;
this.viewOrders = function() {
	
	if(initStatus){		
		initStatus=false;
		CoffeeShopClient.initViewOrders();
	}else{
CoffeShopAppUtil.makeJsonRequest("GET","/coffeeshop/orders/", null ,function(html) {
//console.log(html);
CoffeeShop.loadOrders(html);
});
}
}
this.initViewOrders = function() {
CoffeShopAppUtil.makeRequest("GET","/coffeeshop/orders/", null ,function(html) {
//console.log(html);
CoffeeShop.loadOrders(html);
	$("#response-textarea").val(JSON.stringify(html));
	if(html.Infor != null){
	alert(html.Infor +"\n Click 'New Order' to add order");
 	}	
});
}

this.addAddittion = function(orderid, addition) {
var content = '{"addition":\''+addition+'\'}';
CoffeShopAppUtil.makeRequest("PUT","/coffeeshop/orders/"+orderid, content , function(html) {
$("#response-textarea").val(JSON.stringify(html)); 
CoffeeShopClient.viewOrders();
if(html.Infor != null){
alert(html.Infor);
}
});

}

this.updateStatus = function(orderid,status) {
var content = '{"status":'+status+'}';
CoffeShopAppUtil.makeRequest("PUT","/coffeeshop/orders/"+orderid, content , function(html) {
$("#response-textarea").val(JSON.stringify(html)); 
CoffeeShopClient.viewOrders();	
});

}

this.isPaidOrder = function() {
var orderid = $('#orderid').val();
CoffeShopAppUtil.makeRequest("GET","/coffeeshop/payments/"+orderid, null , function(html) {	});
}

this.payOrder = function(orderid) {
CoffeShopAppUtil.makeRequest("PUT","/coffeeshop/payments/"+orderid, null , function(html) {
$("#response-textarea").val(JSON.stringify(html)); CoffeeShopClient.viewOrders(); });
CoffeeShopClient.viewOrders();
}

this.deletOrder = function(orderid) {
CoffeShopAppUtil.makeRequest("DELETE","/coffeeshop/orders/"+orderid+"/", null , function(html) {
$("#response-textarea").val(JSON.stringify(html));	
CoffeeShopClient.viewOrders();
});
CoffeeShopClient.viewOrders();
}

}
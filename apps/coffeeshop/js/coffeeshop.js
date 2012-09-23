CoffeeShop = new function() {
$('.message-trace').hide();
$('.orderstatus').tooltip();

this.addNewOrder = function () {
//console.log("Click  on addNewOrder");
var bevType = $("#beverage-type").val();
CoffeeShopClient.addOrder(bevType);
$('#addOrder').modal('toggle');

}

this.markPaid = function (id) {
//console.log("Call markPaid" +id);
CoffeeShopClient.payOrder(id);
$("#" + id).children(".paid").show();
$("#" + id).children(".payment-action").hide();

}

this.markComplete =  function (id) {
//console.log("Call markComplete" +id);
CoffeeShopClient.updateStatus(id,'Complete');
$("#" + id).children(".completed").show();
$("#" + id).children(".order-status").hide();
}

this.markDeletOrder = function(id) {
//console.log("Call delet" +id);
CoffeeShopClient.deletOrder(id);
$("#" + id).children(".completed").show();
$("#" + id).children(".order-status").hide();
}

$('.minimize-button').click(function() {
$('.message-trace').toggle('slow');

});
this.showAddition = function(orderID) {
document.getElementById("modal-order-no").innerHTML = orderID;
$("input#orderAdditionId").val(orderID);
$('#addition').modal('toggle');
}

this.addAddition = function() {
var orderID = $("input#orderAdditionId").val();
var additional = $("#additional-toppings").val();
CoffeeShopClient.addAddittion(orderID,additional);
$('#addition').modal('toggle');
}

this.loadOrders = function(data) {
$.get('template/order.html', function(template) {
var html = Mustache.to_html(template, data);
$("#order-window").html(html);
});
}

}
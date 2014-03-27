$(function () {
	 		var ws;
            var log = function (data) {
                $('#console').val($('#console').val() + data + '\n');
            };

            var userCheck = function () {
            	var name = $('#username').val();            	
            	if(name== ""){ 
            		alert('Please Enter a name');
            		return false;
                }
            	$('#username').prop('disabled', true);
            	$('#connect').prop('disabled', true);
            	
            	if(typeof(Storage)!=="undefined")
            	  {
            		sessionStorage.setItem("lastname", name);
            	  }
            	else
            	  {
            	  // Sorry! No Web Storage support..
            		 alert('Storage is not supported by this browser.');
                     return false;
            	  }
            	return true;
            };
            
            $('#connect').click(function () {
            	if(!userCheck()){return;}
                var url = 'ws://localhost:9763/sample/chatroom/server.jag';

                if ('WebSocket' in window) {
                    ws = new WebSocket(url);
                } else if ('MozWebSocket' in window) {
                    ws = new MozWebSocket(url);
                } else {
                    alert('WebSocket is not supported by this browser.');
                    return;
                }

                ws.onopen = function () {                	
                    //log('Connected to the server.');
                    ws.send(sessionStorage.getItem("lastname") +" contected to chat room.");
                    $('#connect').addClass('disabled');
                     $('#disconnect').removeClass('disabled');
                };
                ws.onmessage = function (event) {
                    log(event.data);
                };
                ws.onclose = function () {
                    log('Chat room closed.');
                	// ws.send(sessionStorage.getItem("lastname") +" left the chat room.");
                	$('#connect').removeClass('disabled');
                     $('#disconnect').addClass('disabled');
                };
            });

            $('#disconnect').click(function () {
            	 ws.send(sessionStorage.getItem("lastname") +" left the chat room.");
                ws.close();
                $('#username').prop('disabled', false);
            	$('#connect').prop('disabled', false);
            	
            });

            $('#send').click(function () {
                if (!ws) {
                    alert('Please connect to the server fist');
                    return;
                }
                ws.send(sessionStorage.getItem("lastname") +": "+ $('#content').val());
                $('#content').val('');
                
            });
        });
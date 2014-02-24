$(document).ready(
  function() {
  	//autohidemode:false to show scrollbar  
    var niceScrollDark = {cursorwidth:'6px', cursorcolor:'#313335', cursorborder:'1px solid #313335', styler:"fb"};
	var niceScrollLight = {cursorwidth:'6px', cursorcolor:'#6D6D6D', cursorborder:'1px solid #6D6D6D', styler:"fb"};
	
	
    var adjustSidebar = function(){
    	var main = $('.main');
    	var w = $(window).width();
    	if(w < 979){
    		main.removeClass('offset2');
    	} else {
    		
    		
    			main.addClass('offset2');
    	
    	}	
    }
    
    $('a[data-toggle=tooltip]').tooltip();
    
    $('#codeinput').focus();

	adjustSidebar();
	
		
	
	$('.sidebar').niceScroll(niceScrollLight);
	$('html').niceScroll(niceScrollDark);
	
	$(window).bind('resize', adjustSidebar);
  }
);
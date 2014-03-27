$(document).ready(function() {
	$('#tryit').toggleClass('active')
});

$('#querybtn').click(function(e) {
	e.preventDefault();
	$('#queryblock').toggleClass('hidden');
	$('#querybtn').toggleClass('hidden');

});

$('#queryremove').click(function(e) {
	e.preventDefault();
	$('#queryblock').toggleClass('hidden');
	$('#querybtn').toggleClass('hidden');
	$('#qString').val('');
})
var editor = CodeMirror.fromTextArea(document.getElementById("codeinput"), {
	lineNumbers : true,
	matchBrackets : true,
	mode : "application/x-ejs",
	indentUnit : 4,
	theme : 'ambiance',
	indentWithTabs : true,
	enterMode : "keep",
	tabMode : "shift",
	extraKeys : {
		"Ctrl-Space" : function(cm) {
			CodeMirror.simpleHint(cm, CodeMirror.javascriptHint);
		}
	}
});

function populate() {
	var text = editor.getValue();
	alert(text);
	document.getElementById("cont").innerHTML = document.editor.getvalue();
}


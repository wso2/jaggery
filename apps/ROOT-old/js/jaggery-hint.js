(function () {
  function forEach(arr, f) {
    for (var i = 0, e = arr.length; i < e; ++i) f(arr[i]);
  }
  
  function arrayContains(arr, item) {
    if (!Array.prototype.indexOf) {
      var i = arr.length;
      while (i--) {
        if (arr[i] === item) {
          return true;
        }
      }
      return false;
    }
    return arr.indexOf(item) != -1;
  }

  function scriptHint(editor, keywords, getToken) {
    // Find the token at the cursor
    var cur = editor.getCursor(), token = getToken(editor, cur), tprop = token;
    // If it's not a 'word-style' token, ignore the token.
		if (!/^[\w$_]*$/.test(token.string)) {
      token = tprop = {start: cur.ch, end: cur.ch, string: "", state: token.state,
                       className: token.string == "." ? "property" : null};
    }
    // If it is a property, find out what it is a property of.
    while (tprop.className == "property") {
      tprop = getToken(editor, {line: cur.line, ch: tprop.start});
      if (tprop.string != ".") return;
      tprop = getToken(editor, {line: cur.line, ch: tprop.start});
      if (tprop.string == ')') {
        var level = 1;
        do {
          tprop = getToken(editor, {line: cur.line, ch: tprop.start});
          switch (tprop.string) {
          case ')': level++; break;
          case '(': level--; break;
          default: break;
          }
        } while (level > 0)
        tprop = getToken(editor, {line: cur.line, ch: tprop.start});
				if (tprop.className == 'variable')
					tprop.className = 'function';
				else return; // no clue
      }
      if (!context) var context = [];
      context.push(tprop);
    }
    return {list: getCompletions(token, context, keywords),
            from: {line: cur.line, ch: token.start},
            to: {line: cur.line, ch: token.end}};
  }

  CodeMirror.javascriptHint = function(editor) {
    return scriptHint(editor, javascriptKeywords,
                      function (e, cur) {return e.getTokenAt(cur);});
  }
  
    CodeMirror.javascriptHint = function(editor) {
    return scriptHint(editor, jaggeryKeywords,
                      function (e, cur) {return e.getTokenAt(cur);});
  }

  function getCoffeeScriptToken(editor, cur) {
  // This getToken, it is for coffeescript, imitates the behavior of
  // getTokenAt method in javascript.js, that is, returning "property"
  // type and treat "." as indepenent token.
    var token = editor.getTokenAt(cur);
    if (cur.ch == token.start + 1 && token.string.charAt(0) == '.') {
      token.end = token.start;
      token.string = '.';
      token.className = "property";
    }
    else if (/^\.[\w$_]*$/.test(token.string)) {
      token.className = "property";
      token.start++;
      token.string = token.string.replace(/\./, '');
    }
    return token;
  }

  CodeMirror.coffeescriptHint = function(editor) {
    return scriptHint(editor, coffeescriptKeywords, getCoffeeScriptToken);
  }

  //jaggery api methods 
   var requestProps = ("getMethod() getProtocol() getQueryString() getContent() getContentType() getContentLength() getRequestURI() " +
                     "getPathInfo() getContextPath() getLocalPort() getHeader(headerName) getParameter(paramname) getFile(formFeildName)").split(" ");
	
	var responseProps = ("status contentType content addHeader(key, value) sendRedirect(url) sendError(errCode)").split(" ");
					 
	var sessionProps = ("maxInactive getCreationTime() getLastAccessedTime() put(key, value) get(key) remove(key) invalidate() isNew()").split(" ");
	
	var applicationProps = ("get('propertyName') put('propertyName',value) remove('propertyName')").split(" ");
	
  //end of the jaggery api methods 	
  
  
  var stringProps = ("charAt charCodeAt indexOf lastIndexOf substring substr slice trim trimLeft trimRight " +
                     "toUpperCase toLowerCase split concat match replace search").split(" ");
  var arrayProps = ("length concat join splice push pop shift unshift slice reverse sort indexOf " +
                    "lastIndexOf every some filter forEach map reduce reduceRight ").split(" ");
  var funcProps = "prototype apply call bind".split(" ");
  var javascriptKeywords = ("break case catch continue debugger default delete do else false finally  for function " +
                  "if in instanceof new null return switch  throw true try typeof var void while with").split(" ");
  var jaggeryKeywords = ("Feed File post URIMatcher include request Entry Database MatadataStore parse stringify XML WSRequest WSStub " +
                  "require XMLHTTPRequest URIMatcher print log response session application get put del ").split(" ");
  var coffeescriptKeywords = ("and break catch class continue delete do else extends false finally for " +
                  "if in instanceof isnt new no not null of off on or return switch then throw true try typeof until void while with yes").split(" ");

  function getCompletions(token, context, keywords) {
 
    var found = [], start = token.string;
    function maybeAdd(str) {
	
      if (str.indexOf(start) == 0 && !arrayContains(found, str)) found.push(str);
    }
    function gatherCompletions(obj) {
	
	 if (obj == "require") forEach(stringProps, maybeAdd);
      if (typeof obj == "string") forEach(stringProps, maybeAdd);
	 //if (obj instanceof Object) forEach(arrayProps, maybeAdd);
	else if (obj.name == "request") forEach(requestProps, maybeAdd);
	else if (obj.name == "response") forEach(responseProps, maybeAdd);
		else if (obj.name == "session") forEach(sessionProps, maybeAdd);
	else if (obj.name == "application") forEach(applicationProps, maybeAdd);
	
      else if (obj instanceof Array) forEach(arrayProps, maybeAdd);
      else if (obj instanceof Function) forEach(funcProps, maybeAdd);
      for (var name in obj) maybeAdd(name);
    }

    if (context) {
      // If this is a property, see if it belongs to some object we can
      // find in the current environment.
	 
      var obj = context.pop(), base;
	
      if (obj.className == "variable"){
        base = window[obj.string];
	//	base = 1;
		
		}
      else if (obj.className == "keyword"){
	//  console.log("keyword obj.className::" +obj.className + ", obj.value::"+obj.string);
      base = window[obj.string];
	 
		}
      else if (obj.className == "string")
        base = "";
      else if (obj.className == "atom")
        base = 1;
      else if (obj.className == "function") {
        if (window.jQuery != null && (obj.string == '$' || obj.string == 'jQuery') &&
            (typeof window.jQuery == 'function'))
          base = window.jQuery();
        else if (window._ != null && (obj.string == '_') && (typeof window._ == 'function'))
          base = window._();
      }
      while (base != null && context.length)
        base = base[context.pop().string];
		
      if (base != null) {
	   
	  gatherCompletions(base);}
    }
    else {
      // If not, just look in the window object and any local scope
      // (reading into JS mode internals to get at the local variables)
      for (var v = token.state.localVars; v; v = v.next) {maybeAdd(v.name);
	  
	  }
      gatherCompletions(window);
      forEach(keywords, maybeAdd);
    }
    return found;
  }
})();

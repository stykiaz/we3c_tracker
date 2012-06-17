$('#playback').load(function(){
	var document = $('#playback').contents();
	console.log( document );
//	console.log( $('body',document ) );
//	alert("1" );
	function playbackCursor() {
		this.theDocument = $('#playback').contents();
		this.theBody = $('body', this.theDocument);
		this.cursorCode = '<div '+ 
		 '    style="display: block; position: fixed; z-index: 9999; width: 32px; height: 32px; border: 1px solid red; '+
		 '           background-image: url(\'/assets/images/site/cursors.png\'); background-position: 0 0;"></div>';
		this.initCode = function() {
			this.theBody.append( this.cursorCode );
		}
	}
	
	var playback = new playbackCursor();
	playback.initCode();
	
})